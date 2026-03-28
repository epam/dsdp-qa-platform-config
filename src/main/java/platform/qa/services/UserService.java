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

package platform.qa.services;

import static org.awaitility.Awaitility.await;

import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import platform.qa.entities.Service;
import platform.qa.entities.User;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.oc.OkdClient;

/** Service to implement manipulations with users */
public class UserService {

  private static final int POLL_INTERVAL = 30;
  private static final int TIMEOUT = 5;
  private static final long TOKEN_MIN_LIFE = 240000L;
  private final KeycloakClient keycloakClient;
  @Getter private Map<String, User> testUsers;

  public UserService(Map<String, User> testUsers, KeycloakClient keycloakClient) {
    this.testUsers = new HashMap<>(testUsers);
    this.keycloakClient = keycloakClient;
  }

  public User initUser(User user, String loginName) {
    if (user == null || !user.getLogin().equals(loginName)) {
      user = testUsers.get(loginName);
      keycloakClient.createUser(user);
    }
    return user;
  }

  public User initUser(User user, String loginName, String namespace) {
    if (user == null || !user.getLogin().equals(loginName)) {
      user = testUsers.get(loginName);
      keycloakClient.createUser(user, namespace);
    }
    return user;
  }

  public User initUserInRealm(User user, String loginName, String namespace, String keycloakRealm) {
    if (user == null || !user.getLogin().equals(loginName)) {
      user = testUsers.get(loginName);
      user.setRealm(namespace + "-" + keycloakRealm);
      user.setClientId(keycloakRealm);
      user.setLogin(loginName + "-" + keycloakRealm);

      keycloakClient.createUser(user);
    }
    return user;
  }

  public User refreshUserToken(User user) {
    if (isTokenExpired(user)) {
      user.setToken(keycloakClient.getAccessToken(user.getRealm(), user));
      user.setTokenExpireTime(System.currentTimeMillis());
    }
    return user;
  }

  public void waitForPermissionsToBeAvailable(Service ocService) {
    await()
        .pollInterval(POLL_INTERVAL, TimeUnit.SECONDS)
        .pollInSameThread()
        .atMost(TIMEOUT, TimeUnit.MINUTES)
        .ignoreException(KubernetesClientException.class)
        .untilAsserted(
            () -> {
              var ocClient = new OkdClient(ocService, "user-management");
              var user1 = ocClient.getCredentials("keycloak");
              ocClient.getOsClient().close();
              Assertions.assertThat(user1.getPassword())
                  .withFailMessage("User is not synced for openshift: " + ocService.getUser())
                  .isNotEmpty();
            });
  }

  private boolean isTokenExpired(User user) {
    // TOKEN_MIN_LIFE = 240000l it's milliseconds that means 4 min
    return user.getTokenExpireTime() == 0L
        || System.currentTimeMillis() - user.getTokenExpireTime() >= TOKEN_MIN_LIFE;
  }
}
