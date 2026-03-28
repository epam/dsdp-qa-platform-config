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

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import platform.qa.entities.CentralConfiguration;
import platform.qa.entities.Configuration;
import platform.qa.entities.Service;
import platform.qa.entities.ServiceConfiguration;
import platform.qa.entities.User;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.oc.OkdClient;
import platform.qa.orchestrator.OrchestratorClient;
import platform.qa.providers.impl.PlatformUserProvider;
import platform.qa.utils.OpenshiftServiceProvider;
import platform.qa.utils.OrchestratorServiceProvider;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Initiate and store Central services.
 * Central services defined in {@link CentralConfiguration}
 */
@Log4j2
public final class CentralConfig {
    public static final String LOCALHOST_9999 = "http://localhost:9999/";
    private final CentralConfiguration configuration;

    private Service ceph;
    private Service kibana;
    private Service kiali;
    private Service jaeger;
    private Service defaultGrafana;
    private Service customGrafana;
    private Service jenkins;
    private Service gerrit;
    private Service keycloak;
    private Service wiremock;
    private Service controlPlane;
    private Service nexus;
    private Service email;

    private KeycloakClient keycloakClient;
    @Getter
    private Service ocService;
    @Getter
    private PlatformUserProvider platformUserProvider;

    private Service vaultService;

    private AtomicReference<User> controlPlaneUser = new AtomicReference<>();

    public CentralConfig(Configuration configuration, Service ocService) {
        this.configuration = configuration.getCentralConfiguration();
        this.ocService = ocService;
        platformUserProvider = new PlatformUserProvider(ocService, getKeycloakClient(),
                "properties/platform-users.json");
    }

    /**
     * Constructor with orchestrator client to avoid circular dependencies
     */
    public CentralConfig(Configuration configuration, Service ocService, OrchestratorClient orchestratorClient) {
        this.configuration = configuration.getCentralConfiguration();
        this.ocService = ocService;
        this.orchestratorClient = orchestratorClient;
        platformUserProvider = new PlatformUserProvider(ocService, getKeycloakClient(),
                "properties/platform-users.json");
    }

    private OrchestratorClient orchestratorClient;

    public Service getCeph() {
        if (ceph != null) {
            return ceph;
        }

        try {
            ceph = getService(configuration.getCeph());
            return ceph;
        } catch (RuntimeException e) {
            // Handle cases where Ceph is not available on cluster
            if (e.getMessage() != null && e.getMessage().contains("No pods found")) {
                log.warn("Ceph storage not available on cluster: {}", e.getMessage());
                ceph = new Service(LOCALHOST_9999);
                return ceph;
            }
            throw e;
        }
    }

    public Service getKibana() {
        if (kibana != null) {
            return kibana;
        }

        try {
            kibana = getService(configuration.getKibana());
            return kibana;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No pods found with label")) {
                log.warn("Kibana monitoring not available - this is common in Kubernetes clusters: {}", e.getMessage());
                kibana = new Service(LOCALHOST_9999); // Dummy service
                return kibana;
            }
            throw e;
        }
    }

    public Service getKiali() {
        if (kiali != null) {
            return kiali;
        }

        try {
            kiali = getService(configuration.getKiali());
            return kiali;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No pods found with label")) {
                log.warn("Kiali service mesh monitoring not available - this is common in Kubernetes clusters: {}",
                        e.getMessage());
                kiali = new Service(LOCALHOST_9999); // Dummy service
                return kiali;
            }
            throw e;
        }
    }

    public Service getJager() {
        if (jaeger != null) {
            return jaeger;
        }

        try {
            jaeger = getService(configuration.getJager());
            return jaeger;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No pods found with label")) {
                log.warn("Jaeger tracing not available - this is common in Kubernetes clusters: {}", e.getMessage());
                jaeger = new Service(LOCALHOST_9999); // Dummy service
                return jaeger;
            }
            throw e;
        }
    }

    public Service getDefaultGrafana() {
        if (defaultGrafana != null) {
            return defaultGrafana;
        }

        try {
            defaultGrafana = getService(configuration.getDefaultGrafana());
            return defaultGrafana;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No pods found with label")) {
                log.warn("Default Grafana monitoring not available - this is common in Kubernetes clusters: {}",
                        e.getMessage());
                defaultGrafana = new Service(LOCALHOST_9999); // Dummy service
                return defaultGrafana;
            }
            throw e;
        }
    }

    public Service getCustomGrafana() {
        if (customGrafana != null) {
            return customGrafana;
        }

        try {
            customGrafana = getService(configuration.getCustomGrafana());
            return customGrafana;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("No pods found with label")) {
                log.warn("Custom Grafana monitoring not available - this is common in Kubernetes clusters: {}",
                        e.getMessage());
                customGrafana = new Service(LOCALHOST_9999); // Dummy service
                return customGrafana;
            }
            throw e;
        }
    }

    public Service getJenkins() {
        if (jenkins != null) {
            return jenkins;
        }

        jenkins = getServiceWithUser(configuration.getJenkins());
        return jenkins;
    }

    public Service getGerrit() {
        if (gerrit != null) {
            return gerrit;
        }

        gerrit = getServiceWithUser(configuration.getGerrit());
        return gerrit;
    }

    public Service getKeycloak() {
        if (keycloak != null) {
            return keycloak;
        }

        keycloak = getServiceWithUser(configuration.getKeycloak());
        if ("kubernetes".equals(orchestratorClient.getType())) {
            keycloak.setUrl(keycloak.getUrl() + "/auth/");
        }
        return keycloak;
    }

    public Service getVaultService() {
        if (vaultService != null) {
            return vaultService;
        }

        vaultService = getServiceVaultWithToken(configuration.getVault());
        return vaultService;
    }

    public Service getWiremock() {
        if (wiremock != null) {
            return wiremock;
        }

        wiremock = getService(configuration.getWiremock());
        String wiremockUrl = wiremock.getUrl();
        wiremock.setUrl(wiremockUrl != null ? wiremockUrl.replaceAll("https://", "").replaceAll("/$", "") : null);
        return wiremock;
    }

    public Service getControlPlane() {
        if (controlPlane != null) {
            return controlPlane;
        }

        controlPlane = getService(configuration.getControlPlane());
        return controlPlane;
    }

    public Service getNexus() {
        if (nexus != null) {
            return nexus;
        }

        nexus = getServiceWithUser(configuration.getNexus());
        return nexus;
    }

    public Service getEmail() {
        if (email != null) {
            return email;
        }

        email = getService(configuration.getEmail());
        return email;
    }

    public KeycloakClient getKeycloakClient() {
        if (keycloakClient != null) {
            return keycloakClient;
        }

        keycloakClient = new KeycloakClient(getKeycloak());
        return keycloakClient;
    }

    public List<CatalogSource> getClusterSources() {
        if (orchestratorClient != null) {
            return orchestratorClient.getClusterSources();
        } else {
            return new OkdClient(ocService).getClusterSources();
        }
    }

    Service getServiceVaultWithToken(ServiceConfiguration configuration) {
        Service service = getService(configuration);
        return Service.builder()
                .url(service.getUrl())
                .token(getTokenForService(configuration))
                .build();
    }

    Service getServiceWithUser(ServiceConfiguration configuration) {
        Service service = getService(configuration);
        User user = getUserForService(configuration);

        return new Service(service.getUrl(), user);
    }

    private String getTokenForService(ServiceConfiguration configuration) {
        if (orchestratorClient != null) {
            return orchestratorClient.getTokenVault(configuration.getSecret());
        } else {
            return new OkdClient(ocService, configuration.getNamespace()).getTokenVault(configuration.getSecret());
        }
    }

    private User getUserForService(ServiceConfiguration configuration) {
        if (orchestratorClient != null) {
            return orchestratorClient.getCredentials(configuration.getSecret());
        } else {
            return new OkdClient(ocService, configuration.getNamespace()).getCredentials(configuration.getSecret());
        }
    }

    public Service getService(ServiceConfiguration configuration) {
        if (orchestratorClient != null) {
            orchestratorClient.setClientNamespace(configuration.getNamespace());
            return OrchestratorServiceProvider.getService(orchestratorClient, configuration);
        } else {
            return OpenshiftServiceProvider.getService(ocService, configuration);
        }
    }

}
