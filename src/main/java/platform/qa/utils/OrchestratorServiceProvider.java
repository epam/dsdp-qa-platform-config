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

package platform.qa.utils;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.fabric8.kubernetes.api.model.PodList;
import jodd.util.Base64;
import lombok.SneakyThrows;
import platform.qa.configuration.MasterConfig;
import platform.qa.entities.Ceph;
import platform.qa.entities.Db;
import platform.qa.entities.Redis;
import platform.qa.entities.Service;
import platform.qa.entities.ServiceConfiguration;
import platform.qa.entities.User;
import platform.qa.extension.SocketAnalyzer;
import platform.qa.orchestrator.OrchestratorClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.istack.Nullable;

/**
 * Generic service provider that works with any OrchestratorClient (OpenShift or Kubernetes)
 */
public final class OrchestratorServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorServiceProvider.class);

    /**
     * Create {@link Service} with route by provided configuration.
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * orchestrator routes
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param configuration      {@link ServiceConfiguration} for service that was provided
     * @return {@link Service} with route without user
     */
    @SneakyThrows
    public static Service getService(OrchestratorClient orchestratorClient, ServiceConfiguration configuration) {
        if (configuration.isPortForwarding() || !isRoutePresent(orchestratorClient, configuration.getRoute())) {
            int port = orchestratorClient.performPortForwarding(configuration.getPodLabel(), configuration.getRoute(),
                    configuration.getDefaultPort());
            return new Service("http://localhost:" + port + "/");
        }

        return new Service(getRoute(orchestratorClient, configuration.getRoute()));
    }

    /**
     * Create {@link Service} with route and user by provided configuration.
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * orchestrator routes
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param configuration      {@link ServiceConfiguration} for service that was provided
     * @param user               {@link User} user for service
     * @return {@link Service} with route and user
     */
    @SneakyThrows
    public static Service getService(OrchestratorClient orchestratorClient, ServiceConfiguration configuration,
            User user) {
        if (configuration.isPortForwarding() || !isRoutePresent(orchestratorClient, configuration.getRoute())) {
            int port = orchestratorClient.performPortForwarding(configuration.getPodLabel(), configuration.getRoute(),
                    configuration.getDefaultPort());
            return new Service("http://localhost:" + port + "/", user);
        }

        return new Service(getRoute(orchestratorClient, configuration.getRoute()), user);
    }

    /**
     * Initialize {@link Db} object for connection
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * orchestrator routes
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param configuration      {@link ServiceConfiguration} for service that was provided
     * @return {@link Db} with user and url
     */
    public static Db getDbService(OrchestratorClient orchestratorClient, ServiceConfiguration configuration) {
        Service citusService = getDbPodService(orchestratorClient, configuration);
        citusService.setUrl(citusService.getUrl().replace("http", "jdbc:postgresql"));
        var credentials = orchestratorClient.getCredentials(configuration.getSecret());

        return Db.builder()
                .user(credentials.getLogin())
                .password(credentials.getPassword())
                .url(citusService.getUrl())
                .build();
    }

    /**
     * Initialize {@link User} with username and password by secret name
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param secret             secret name
     * @param key                key for secret value
     * @return {@link User} with username and password
     */
    public static User getUserSecretsBySecretNameAndKey(OrchestratorClient orchestratorClient, String secret,
            String key) {
        Map<String, String> secrets = orchestratorClient.getSecretsByName(secret);
        String user = Base64.decodeToString(secrets.get(key + "Name"));
        String pwd = Base64.decodeToString(secrets.get(key + "Pass"));
        return new User(user, pwd);
    }

    /**
     * Provides decoded password by secret name and key
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param secretName         secret name
     * @param key                key for secret value
     * @return decoded password from secret
     */
    public static String getPasswordFromSecretByKey(OrchestratorClient orchestratorClient, String secretName,
            String key) {
        var secrets = orchestratorClient.getSecretsByName(secretName);
        return Base64.decodeToString(secrets.get(key));
    }

    /**
     * Initialize {@link Ceph} service with bucket name, secret keys and host
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param secretName         secret name
     * @param cephUrl            url for ceph if port forward
     * @return {@link Ceph} service with bucket name, secret keys and host
     */
    public static Ceph getCephService(OrchestratorClient orchestratorClient, String secretName,
            @Nullable String cephUrl) {
        Map<String, String> secret = orchestratorClient.getSecretsByName(secretName);
        Map<String, String> configurationMap = orchestratorClient.getConfigurationMap(secretName);

        ServiceConfiguration cephConfiguration =
                MasterConfig.getInstance().getConfiguration().getCentralConfiguration().getCeph();

        return Ceph.builder()
                .bucketName(configurationMap.get("BUCKET_NAME"))
                .accessKey(Base64.decodeToString(secret.get("AWS_ACCESS_KEY_ID")))
                .secretKey(Base64.decodeToString(secret.get("AWS_SECRET_ACCESS_KEY")))
                .host(cephConfiguration.isPortForwarding() ? cephUrl : configurationMap.get("BUCKET_HOST"))
                .build();
    }

    /**
     * Create {@link Service} with route by provided configuration.
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * orchestrator routes
     *
     * @param orchestratorClient {@link OrchestratorClient} client for orchestrator
     * @param configuration      {@link ServiceConfiguration} for service that was provided
     * @return {@link Service} with route without user
     */
    @SneakyThrows
    public static Service getDbPodService(OrchestratorClient orchestratorClient, ServiceConfiguration configuration) {
        String namespace = orchestratorClient.getNamespace();
        if (namespace == null || namespace.isBlank()) {
            throw new IllegalArgumentException("Namespace must not be null or blank in ServiceConfiguration");
        }

        try {
            log.info("Searching for DB pod in namespace '{}' with route '{}'", namespace, configuration.getRoute());

            String podName = Awaitility.await()
                    .atMost(1, TimeUnit.MINUTES)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .until(() -> {
                        var podList = orchestratorClient.getPodList(namespace);
                        if (podList == null || podList.getItems().isEmpty()) {
                            log.debug("No pods found yet in namespace '{}'", namespace);
                            return null;
                        }

                        return podList.getItems().stream()
                                .map(pod -> pod.getMetadata().getName())
                                .filter(name -> name.contains(configuration.getRoute()))
                                .findFirst()
                                .orElse(null);
                    }, Objects::nonNull);

            log.info("Found pod '{}', setting up port forwarding...", podName);

            int localPort = orchestratorClient.performPortForwarding(
                    configuration.getPodLabel(),
                    configuration.getRoute(),
                    configuration.getDefaultPort()
            );

            String serviceUrl = "http://localhost:" + localPort + "/";
            log.info("Port forwarding established to '{}'", serviceUrl);

            return new Service(serviceUrl);

        } catch (Exception e) {
            log.error("Failed to list pods and set up port forwarding in namespace '{}': {}", namespace,
                    e.getMessage(), e);
            throw new RuntimeException(
                    "Could not retrieve DB pod service after retries due to orchestrator client error", e
            );
        }
    }

    @SneakyThrows
    public static Redis getRedisService(OrchestratorClient orchestratorClient, ServiceConfiguration configuration,
            User user) {
        var podList = orchestratorClient.getPodList(configuration.getNamespace());
        var podToForward = podList.getItems()
                .stream()
                .filter(pod -> Objects.nonNull(pod.getMetadata()))
                .filter(pod -> Objects.nonNull(pod.getMetadata().getName()))
                .filter(pod -> pod.getMetadata().getName().contains(configuration.getPodLabel()))
                .sorted(Comparator.comparing(current -> current.getMetadata().getName()))
                .limit(1)
                .map(pod -> pod.getMetadata().getName())
                .findFirst()
                .orElse(null);
        if (configuration.isPortForwarding()) {
            int port = new SocketAnalyzer().getAvailablePort();
            orchestratorClient.performPortForwarding(configuration.getPodLabel(), podToForward,
                    configuration.getDefaultPort());
            return new Redis("http://localhost:" + port + "/", user.getPassword());
        }
        return null;
    }

    public static List<Redis> getRedisServices(OrchestratorClient orchestratorClient,
            ServiceConfiguration configuration, User user) {
        List<Redis> redisServices = new ArrayList<>();

        PodList podList = orchestratorClient.getPodList(orchestratorClient.getNamespace());

        List<String> sentinelNames = podList.getItems()
                .stream()
                .filter(pod -> pod.getMetadata() != null && pod.getMetadata().getName() != null)
                .map(pod -> pod.getMetadata().getName())
                .filter(name -> name.contains(configuration.getPodLabel()))
                .toList();

        sentinelNames.stream()
                .filter(podName -> configuration.isPortForwarding())
                .forEach(podName -> {
                    try {
                        String podLabel = "app.kubernetes.io/name=redis-sentinel";
                        int localPort = orchestratorClient.performPortForwarding(
                                podLabel,
                                podName,
                                configuration.getDefaultPort()
                        );

                        String redisUrl = "http://localhost:" + localPort + "/";
                        Redis redis = new Redis(redisUrl, user.getPassword());
                        redisServices.add(redis);

                        log.info("Redis service added: {}", redisUrl);

                    } catch (Exception e) {
                        log.error("Failed to port-forward Redis pod '{}': {}", podName, e.getMessage());
                        throw new RuntimeException(e);
                    }
                });

        return redisServices;
    }


    private static boolean isRoutePresent(OrchestratorClient orchestratorClient, String route) {
        return getRouteValue(orchestratorClient, route) != null;
    }

    private static String getRoute(OrchestratorClient orchestratorClient, String route) {
        var result = getRouteValue(orchestratorClient, route);

        assertThat(result).as(String.format("Route %s has not been found", route)).isNotNull();
        return result;
    }

    private static String getRouteValue(OrchestratorClient orchestratorClient, String route) {
        HashMap<String, String> routes = orchestratorClient.getRoutes();
        List<String> matchedRoutes =
                routes.keySet().stream().filter(r -> r.contains(route)).toList();
        return matchedRoutes.size() == 1 ? routes.get(matchedRoutes.get(0)) : routes.get(route);
    }
}
