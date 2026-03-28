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
import platform.qa.oc.OkdClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.awaitility.Awaitility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.istack.Nullable;

public final class OpenshiftServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenshiftServiceProvider.class);

    /**
     * Create {@link Service} with route by provided configuration.
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * k8s routes
     *
     * @param ocService     {@link Service} for k8s connection
     * @param configuration {@link ServiceConfiguration} for service that was provided
     * @return {@link Service} with route without user
     */
    public static Service getService(Service ocService, ServiceConfiguration configuration) {
        var ocClient = new OkdClient(ocService, configuration.getNamespace());

        return getService(ocClient, configuration);
    }

    /**
     * Create {@link Service} with route by provided configuration.
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * k8s routes
     *
     * @param ocClient      {@link OkdClient} client for k8s
     * @param configuration {@link ServiceConfiguration} for service that was provided
     * @return {@link Service} with route without user
     */
    @SneakyThrows
    public static Service getService(OkdClient ocClient, ServiceConfiguration configuration) {
        if (configuration.isPortForwarding() || !isRoutePresent(ocClient, configuration.getRoute())) {
            int port = ocClient.performPortForwarding(configuration.getPodLabel(), configuration.getRoute(),
                    configuration.getDefaultPort());
            return new Service("http://localhost:" + port + "/");
        }

        return new Service(getRoute(ocClient, configuration.getRoute()));
    }

    /**
     * Create {@link Service} with route and user by provided configuration.
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * k8s routes
     *
     * @param ocClient      {@link OkdClient} client for k8s
     * @param configuration {@link ServiceConfiguration} for service that was provided
     * @param user          {@link User} user for service
     * @return {@link Service} with route and user
     */
    @SneakyThrows
    public static Service getService(OkdClient ocClient, ServiceConfiguration configuration, User user) {
        if (configuration.isPortForwarding() || !isRoutePresent(ocClient, configuration.getRoute())) {
            int port = ocClient.performPortForwarding(configuration.getPodLabel(), configuration.getRoute(),
                    configuration.getDefaultPort());
            return new Service("http://localhost:" + port + "/", user);
        }

        return new Service(getRoute(ocClient, configuration.getRoute()), user);
    }

    /**
     * Initialize {@link Db} object for connection
     * If {@link ServiceConfiguration#isPortForwarding()} true - forward ports for service, false - get route from
     * k8s routes
     *
     * @param ocClient      {@link OkdClient} client for k8s
     * @param configuration {@link ServiceConfiguration} for service that was provided
     * @return {@link Db} with user and url
     */
    public static Db getDbService(OkdClient ocClient, ServiceConfiguration configuration) {
        Service citusService = getDbPodService(ocClient, configuration);
        citusService.setUrl(citusService.getUrl().replace("http", "jdbc:postgresql"));
        var credentials = ocClient.getCredentials(configuration.getSecret());

        return Db.builder()
                .user(credentials.getLogin())
                .password(credentials.getPassword())
                .url(citusService.getUrl())
                .build();
    }

    /**
     * Initialize {@link User} with username and password by secret name
     *
     * @param ocClient {@link OkdClient} client for k8s
     * @param key      key for secret value
     * @param secret   secret name
     * @return {@link User} with username and password
     */
    public static User getUserSecretsBySecretNameAndKey(OkdClient ocClient, String secret, String key) {
        Map<String, String> secrets = ocClient.getSecretsByName(secret);
        String user = Base64.decodeToString(secrets.get(key + "Name"));
        String pwd = Base64.decodeToString(secrets.get(key + "Pass"));
        return new User(user, pwd);
    }

    /**
     * Provides decoded password by secret name and key
     *
     * @param ocClient   {@link OkdClient} client for k8s
     * @param secretName secret name
     * @param key        key for secret value
     * @return decoded password from secret
     */
    public static String getPasswordFromSecretByKey(OkdClient ocClient, String secretName, String key) {
        var secrets = ocClient.getSecretsByName(secretName);
        return Base64.decodeToString(secrets.get(key));
    }

    /**
     * Initialize {@link Ceph} service with bucket name, secret keys and host
     *
     * @param ocClient   {@link OkdClient} client for k8s
     * @param secretName secret name
     * @param cephUrl    url for ceph if port forward
     * @return {@link Ceph} service with bucket name, secret keys and host
     */
    public static Ceph getCephService(OkdClient ocClient, String secretName, @Nullable String cephUrl) {
        Map<String, String> secret = ocClient.getSecretsByName(secretName);
        Map<String, String> configurationMap = ocClient.getConfigurationMap(secretName);

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
     * k8s routes
     *
     * @param ocClient      {@link OkdClient} client for k8s
     * @param configuration {@link ServiceConfiguration} for service that was provided
     * @return {@link Service} with route without user
     */
    @SneakyThrows
    public static Service getDbPodService(OkdClient ocClient, ServiceConfiguration configuration) {
        try {
            String podName = Awaitility.await()
                    .atMost(3, TimeUnit.MINUTES)
                    .pollInterval(5, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .until(() -> {
                        var databasePods = ocClient
                                .getOsClient()
                                .pods()
                                .withLabel(configuration.getPodLabel())
                                .list()
                                .getItems();

                        return databasePods.stream()
                                .filter(pod -> pod.getMetadata().getName().contains(configuration.getRoute()))
                                .map(pod -> pod.getMetadata().getName())
                                .findFirst()
                                .orElse(null);
                    }, Objects::nonNull);

            int port = new SocketAnalyzer().getAvailablePort();
            ocClient.getOsClient().pods().withName(podName).portForward(configuration.getDefaultPort(), port);
            return new Service("http://localhost:" + port + "/");
        } catch (Exception e) {
            log.info("Failed to list pods and set up port forwarding in namespace 'abc-02': {}", e.getMessage());
            throw new RuntimeException("Could not retrieve DB pod service after retries due to Kubernetes client "
                    + "error", e);
        }
    }


    @SneakyThrows
    public static Redis getRedisService(OkdClient ocClient, ServiceConfiguration configuration, User user) {
        var podList = ocClient.getOsClient().pods().list();
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
            ocClient.getOsClient().pods().withName(podToForward).portForward(configuration.getDefaultPort(), port);
            return new Redis("http://localhost:" + port + "/", user.getPassword());
        }
        return null;
    }

    public static List<Redis> getRedisServices(OkdClient ocClient, ServiceConfiguration configuration, User user) {
        List<Redis> redisServices = new ArrayList<>();
        PodList podList = ocClient.getOsClient().pods().list();

        List<String> sentinelNames = podList.getItems()
                .stream()
                .filter(pod -> Objects.nonNull(pod.getMetadata()))
                .filter(pod -> Objects.nonNull(pod.getMetadata().getName()))
                .filter(pod -> pod.getMetadata().getName().contains(configuration.getPodLabel()))
                .map(pod -> pod.getMetadata().getName())
                .collect(Collectors.toList());

        for (String pod : sentinelNames) {
            if (configuration.isPortForwarding()) {
                try (SocketAnalyzer socketAnalyzer = new SocketAnalyzer()) {
                    int port = socketAnalyzer.getAvailablePort();
                    ocClient.getOsClient().pods().withName(pod).portForward(configuration.getDefaultPort(), port);

                    Redis redis = new Redis("http://localhost:" + port + "/", user.getPassword());
                    redisServices.add(redis);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return redisServices;
    }

    private static boolean isRoutePresent(OkdClient okdClient, String route) {
        return getRouteValue(okdClient, route) != null;
    }

    private static String getRoute(OkdClient ocClient, String route) {
        var result = getRouteValue(ocClient, route);

        assertThat(result).as(String.format("Route %s has not been found", route)).isNotNull();
        return result;
    }

    private static String getRouteValue(OkdClient ocClient, String route) {
        HashMap<String, String> routes = ocClient.getOkdRoutes();
        List<String> matchedRoutes =
                routes.keySet().stream().filter(r -> r.contains(route)).collect(Collectors.toList());
        return matchedRoutes.size() == 1 ? routes.get(matchedRoutes.get(0)) : routes.get(route);
    }
}
