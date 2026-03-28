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

package platform.qa.factories;

import lombok.extern.log4j.Log4j2;
import platform.qa.entities.Service;
import platform.qa.k8s.K8sClient;
import platform.qa.oc.OkdClient;
import platform.qa.orchestrator.OrchestratorClient;

/**
 * Factory for creating orchestrator clients based on configuration
 */
@Log4j2
public class OrchestratorClientFactory {

    public enum OrchestratorType {
        OPENSHIFT,
        KUBERNETES
    }

    /**
     * Create an orchestrator client based on the specified type
     *
     * @param type      orchestrator type
     * @param service   service configuration for connection
     * @param namespace target namespace
     * @return appropriate orchestrator client implementation
     */
    public static OrchestratorClient createClient(OrchestratorType type, Service service, String namespace) {
        log.info("Creating {} client for namespace: {}", type.name().toLowerCase(), namespace);

        return switch (type) {
            case OPENSHIFT -> new OkdClient(service, namespace);
            case KUBERNETES -> new K8sClient(service, namespace);
        };
    }

    /**
     * Create an orchestrator client based on string configuration
     *
     * @param orchestratorType orchestrator type as string
     * @param service          service configuration for connection
     * @param namespace        target namespace
     * @return appropriate orchestrator client implementation
     */
    public static OrchestratorClient createClient(String orchestratorType, Service service, String namespace) {
        OrchestratorType type = parseOrchestratorType(orchestratorType);
        return createClient(type, service, namespace);
    }

    /**
     * Create a Kubernetes client using token-based authentication
     *
     * @param masterUrl Kubernetes API server URL
     * @param token     OAuth token for authentication
     * @param namespace target namespace
     * @return K8sClient configured for token authentication
     */
    public static OrchestratorClient createKubernetesClientWithToken(String masterUrl, String token, String namespace) {
        log.info("Creating Kubernetes client with token authentication for namespace: {}", namespace);
        return new K8sClient(masterUrl, token, namespace);
    }

    /**
     * Create an orchestrator client with enhanced configuration support
     *
     * @param orchestratorType orchestrator type as string
     * @param service          service configuration for connection
     * @param namespace        target namespace
     * @param token            optional token for Kubernetes authentication
     * @return appropriate orchestrator client implementation
     */
    public static OrchestratorClient createClient(String orchestratorType, Service service, String namespace,
            String token) {
        OrchestratorType type = parseOrchestratorType(orchestratorType);

        log.info("Creating orchestrator client - Type: {}, Namespace: {}, Token provided: {}",
                type, namespace, isTokenAuth(token));

        if (type == OrchestratorType.KUBERNETES && isTokenAuth(token)) {
            log.info("Using token-based authentication for Kubernetes client");
            log.debug("Service URL: {}, Token length: {}", service.getUrl(), token.length());
            return new K8sClient(service.getUrl(), token, namespace);
        }

        if (type == OrchestratorType.KUBERNETES) {
            log.warn("Kubernetes orchestrator requested but no token provided - falling back to service "
                    + "authentication");
        }

        return createClient(type, service, namespace);
    }

    /**
     * Parse orchestrator type from string
     *
     * @param orchestratorType orchestrator type string
     * @return OrchestratorType enum value
     */
    public static OrchestratorType parseOrchestratorType(String orchestratorType) {
        if (orchestratorType == null || orchestratorType.trim().isEmpty()) {
            log.warn("Orchestrator type not specified, defaulting to OpenShift");
            return OrchestratorType.OPENSHIFT;
        }

        String normalizedType = orchestratorType.trim().toUpperCase();

        return switch (normalizedType) {
            case "OPENSHIFT", "OKD", "OCP" -> OrchestratorType.OPENSHIFT;
            case "KUBERNETES", "K8S" -> OrchestratorType.KUBERNETES;
            default -> {
                log.warn("Unknown orchestrator type: {}, defaulting to OpenShift", orchestratorType);
                yield OrchestratorType.OPENSHIFT;
            }
        };
    }

    private static boolean isTokenAuth(String token) {
        return token != null && !token.isBlank();
    }
}
