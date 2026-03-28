/*
 * Copyright 2022 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package platform.qa.configuration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import jodd.util.Base64;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import platform.qa.entities.CentralConfiguration;
import platform.qa.entities.Configuration;
import platform.qa.entities.RegistryConfiguration;
import platform.qa.entities.Service;
import platform.qa.entities.User;
import platform.qa.exceptions.ConfigurationExceptions;
import platform.qa.factories.OrchestratorClientFactory;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.utils.ConfigurationUtils;

/**
 * Load initial configuration for Central and Registry services. Currently, supports only {@link
 * CentralConfiguration} and {@link RegistryConfiguration}. Example of usage :
 *
 * <p>{@code RegistryConfiguration regConfig =
 * MasterConfig.getInstance().getRegistryConfiguration("example-namespace"); GlobalConfiguration
 * globalConfig = MasterConfig.getInstance().getGlobalConfiguration(); }
 */
@Log4j2
public final class MasterConfig {
  public static final String OPENSHIFT = "openshift";
  private static MasterConfig instance;

  @Getter private final Configuration configuration;

  private final Service oc;
  private final String defaultNamespace;
  private final KeycloakClient keycloakClient;

  @Getter private final CentralConfig centralConfig;
  @Getter private final String cluster;
  @Getter private final String local;
  @Getter private final String release;
  @Getter private final String cicdTool;
  @Getter private final String region;
  @Getter private final String orchestratorType;
  @Getter private final String realm;
  @Getter private final String token;
  private final Map<String, RegistryConfig> registryConfigs = new HashMap<>();
  private final Properties properties;
  @Getter private String baseDomain;

  private MasterConfig() {
    configuration = ConfigurationUtils.uploadConfiguration("properties/platform.json");
    properties = ConfigurationUtils.uploadPropertiesConfiguration("properties/platform.properties");

    orchestratorType = getConfigValue("orchestrator", OPENSHIFT);
    defaultNamespace = getConfigValue("namespace");
    realm = getConfigValue("realm");
    cluster = getConfigValue("cluster");
    local = getConfigValue("local");
    release = getConfigValue("release");
    cicdTool = getConfigValue("cicdTool");
    region = getConfigValue("region");
    token = getConfigValue("token");

    String ocUrl;
    User ocUser;

    if (OPENSHIFT.equalsIgnoreCase(orchestratorType)) {
      baseDomain = getConfigValue("baseDomain");

      String urlPattern = getConfigValue("url", "https://api.%s.%s:6443");
      ocUrl = String.format(urlPattern, cluster, baseDomain);

      String username = getConfigValue("username");
      String password = getConfigValue("password");

      if (password != null && !password.isEmpty()) {
        password = Base64.decodeToString(password);
      }

      ocUser = new User(username, password);
      log.info("Configured OpenShift cluster: {}, user: {}", cluster, username);

    } else if ("kubernetes".equalsIgnoreCase(orchestratorType)) {
      ocUrl = getConfigValue("api.url");

      if (ocUrl == null || ocUrl.isEmpty()) {
        throw new IllegalStateException("api.url must be specified for Kubernetes orchestrator");
      }

      if (token == null || token.isEmpty()) {
        throw new IllegalStateException("Token must be provided for Kubernetes orchestrator");
      }

      ocUser = new User("token-user", token);
      log.info("Configured Kubernetes cluster: {}, URL: {}", cluster, ocUrl);
    } else {
      throw new IllegalArgumentException("Unknown orchestrator type: " + orchestratorType);
    }

    oc = new Service(ocUrl, ocUser);
    var orchestratorClient =
        OrchestratorClientFactory.createClient(orchestratorType, oc, "user-management", token);

    centralConfig = new CentralConfig(configuration, oc, orchestratorClient);
    keycloakClient = centralConfig.getKeycloakClient();
  }

  /**
   * Provides instance of {@link MasterConfig}
   *
   * @return {@link MasterConfig}
   */
  public static MasterConfig getInstance() {
    if (instance != null) {
      return instance;
    }

    instance = new MasterConfig();
    return instance;
  }

  public Map<String, RegistryConfig> setNamespaces(List<String> namespaces) {
    namespaces.forEach(
        namespace -> {
          try {
            registryConfigs.put(
                namespace,
                new RegistryConfig(
                    configuration,
                    namespace,
                    oc,
                    keycloakClient,
                    centralConfig.getCeph(),
                    local,
                    release,
                    cicdTool,
                    region,
                    orchestratorType));
          } catch (IOException e) {
            throw new UncheckedIOException("Failed to set namespaces", e);
          }
        });
    return new HashMap<>(registryConfigs);
  }

  public RegistryConfig getRegistryConfig() {
    if (!registryConfigs.containsKey(defaultNamespace)) {
      try {
        registryConfigs.put(
            defaultNamespace,
            new RegistryConfig(
                configuration,
                defaultNamespace,
                oc,
                keycloakClient,
                centralConfig.getCeph(),
                local,
                release,
                cicdTool,
                region,
                orchestratorType));
      } catch (IOException e) {
        throw new UncheckedIOException(
            "Failed to create RegistryConfig for namespace: " + defaultNamespace, e);
      }
    }

    return registryConfigs.get(defaultNamespace);
  }

  @SneakyThrows(ConfigurationExceptions.MissingNamespaceInConfiguration.class)
  public RegistryConfig getRegistryConfig(String namespace) {
    if (!registryConfigs.containsKey(namespace)) {
      throw new ConfigurationExceptions.MissingNamespaceInConfiguration(
          "Namespace " + namespace + " is missing" + " for registry configuration!");
    }
    return registryConfigs.get(namespace);
  }

  /**
   * Get master URL for Kubernetes/OpenShift cluster
   *
   * @return master URL
   */
  public String getMasterUrl() {
    return oc.getUrl();
  }

  private String getConfigValue(String key) {
    return getConfigValue(key, null);
  }

  private String getConfigValue(String key, String defaultValue) {
    return Optional.ofNullable(System.getProperty(key))
        .or(() -> Optional.ofNullable(properties.getProperty(key)))
        .or(() -> Optional.ofNullable(defaultValue))
        .map(String::trim)
        .orElse(null);
  }
}
