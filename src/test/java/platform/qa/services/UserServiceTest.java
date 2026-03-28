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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import platform.qa.entities.User;
import platform.qa.keycloak.KeycloakClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
public class UserServiceTest {

  @Mock
  private KeycloakClient mockKeycloakClient;

  private Map<String, User> testUsers;
  private UserService userService;
  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = createTestUser("testuser", "testuser@example.com");
    testUsers = new HashMap<>();
    testUsers.put("testuser", testUser);
    testUsers.put("admin", createTestUser("admin", "admin@example.com"));
    
    userService = new UserService(testUsers, mockKeycloakClient);
  }

  private User createTestUser(String login, String mail) {
    User user = mock(User.class);
    lenient().when(user.getLogin()).thenReturn(login);
    lenient().when(user.getMail()).thenReturn(mail);
    lenient().when(user.getTokenExpireTime()).thenReturn(0L);
    lenient().when(user.getRealm()).thenReturn("test-realm");
    return user;
  }

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create UserService with test users")
    void shouldCreateUserServiceWithTestUsers() {
      assertThat(userService).isNotNull();
      assertThat(userService.getTestUsers()).isNotNull();
      assertThat(userService.getTestUsers()).hasSize(2);
      assertThat(userService.getTestUsers()).containsKeys("testuser", "admin");
    }

    @Test
    @DisplayName("Should create defensive copy of test users")
    void shouldCreateDefensiveCopyOfTestUsers() {
      Map<String, User> originalUsers = new HashMap<>();
      originalUsers.put("user1", testUser);
      
      UserService service = new UserService(originalUsers, mockKeycloakClient);
      
      // Modify original map
      originalUsers.put("user2", createTestUser("user2", "User 2"));
      
      // Service should not be affected
      assertThat(service.getTestUsers()).hasSize(1);
      assertThat(service.getTestUsers()).containsKey("user1");
      assertThat(service.getTestUsers()).doesNotContainKey("user2");
    }
  }

  @Nested
  @DisplayName("Init User Tests")
  class InitUserTests {

    @Test
    @DisplayName("Should return existing user when user matches login name")
    void shouldReturnExistingUserWhenUserMatchesLoginName() {
      User existingUser = createTestUser("testuser", "existing@example.com");
      
      User result = userService.initUser(existingUser, "testuser");
      
      assertThat(result).isSameAs(existingUser);
      verify(mockKeycloakClient, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should create new user when user is null")
    void shouldCreateNewUserWhenUserIsNull() {
      User result = userService.initUser(null, "testuser");
      
      assertThat(result).isEqualTo(testUser);
      verify(mockKeycloakClient, times(1)).createUser(testUser);
    }

    @Test
    @DisplayName("Should create new user when login names don't match")
    void shouldCreateNewUserWhenLoginNamesDontMatch() {
      User differentUser = createTestUser("different", "different@example.com");
      
      User result = userService.initUser(differentUser, "testuser");
      
      assertThat(result).isEqualTo(testUser);
      verify(mockKeycloakClient, times(1)).createUser(testUser);
    }

    @Test
    @DisplayName("Should handle user not found in test users")
    void shouldHandleUserNotFoundInTestUsers() {
      User result = userService.initUser(null, "nonexistent");
      
      assertThat(result).isNull();
      verify(mockKeycloakClient, times(1)).createUser(null);
    }
  }

  @Nested
  @DisplayName("Init User With Namespace Tests")
  class InitUserWithNamespaceTests {

    @Test
    @DisplayName("Should return existing user when user matches login name")
    void shouldReturnExistingUserWhenUserMatchesLoginName() {
      User existingUser = createTestUser("testuser", "existing@example.com");
      
      User result = userService.initUser(existingUser, "testuser", "test-namespace");
      
      assertThat(result).isSameAs(existingUser);
      verify(mockKeycloakClient, never()).createUser(any(User.class), anyString());
    }

    @Test
    @DisplayName("Should create new user with namespace when user is null")
    void shouldCreateNewUserWithNamespaceWhenUserIsNull() {
      User result = userService.initUser(null, "testuser", "test-namespace");
      
      assertThat(result).isEqualTo(testUser);
      verify(mockKeycloakClient, times(1)).createUser(testUser, "test-namespace");
    }

    @Test
    @DisplayName("Should create new user with namespace when login names don't match")
    void shouldCreateNewUserWithNamespaceWhenLoginNamesDontMatch() {
      User differentUser = createTestUser("different", "different@example.com");
      
      User result = userService.initUser(differentUser, "testuser", "test-namespace");
      
      assertThat(result).isEqualTo(testUser);
      verify(mockKeycloakClient, times(1)).createUser(testUser, "test-namespace");
    }
  }

  @Nested
  @DisplayName("Init User In Realm Tests")
  class InitUserInRealmTests {

    @Test
    @DisplayName("Should return existing user when user matches login name")
    void shouldReturnExistingUserWhenUserMatchesLoginName() {
      User existingUser = createTestUser("testuser", "existing@example.com");
      
      User result = userService.initUserInRealm(existingUser, "testuser", "test-namespace", "test-realm");
      
      assertThat(result).isSameAs(existingUser);
      verify(mockKeycloakClient, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should create and configure user in realm when user is null")
    void shouldCreateAndConfigureUserInRealmWhenUserIsNull() {
      User result = userService.initUserInRealm(null, "testuser", "test-namespace", "test-realm");
      
      assertThat(result).isEqualTo(testUser);
      verify(testUser).setRealm("test-namespace-test-realm");
      verify(testUser).setClientId("test-realm");
      verify(testUser).setLogin("testuser-test-realm");
      verify(mockKeycloakClient, times(1)).createUser(testUser);
    }

    @Test
    @DisplayName("Should create and configure user in realm when login names don't match")
    void shouldCreateAndConfigureUserInRealmWhenLoginNamesDontMatch() {
      User differentUser = createTestUser("different", "different@example.com");
      
      User result = userService.initUserInRealm(differentUser, "testuser", "test-namespace", "test-realm");
      
      assertThat(result).isEqualTo(testUser);
      verify(testUser).setRealm("test-namespace-test-realm");
      verify(testUser).setClientId("test-realm");
      verify(testUser).setLogin("testuser-test-realm");
      verify(mockKeycloakClient, times(1)).createUser(testUser);
    }
  }

  @Nested
  @DisplayName("Refresh User Token Tests")
  class RefreshUserTokenTests {

    @Test
    @DisplayName("Should refresh token when token is expired")
    void shouldRefreshTokenWhenTokenIsExpired() {
      // Set token expire time to 0 (expired)
      when(testUser.getTokenExpireTime()).thenReturn(0L);
      when(mockKeycloakClient.getAccessToken("test-realm", testUser)).thenReturn("new-token");
      
      User result = userService.refreshUserToken(testUser);
      
      assertThat(result).isSameAs(testUser);
      verify(testUser).setToken("new-token");
      verify(testUser).setTokenExpireTime(any(Long.class));
    }

    @Test
    @DisplayName("Should refresh token when token is old")
    void shouldRefreshTokenWhenTokenIsOld() {
      // Set token expire time to old timestamp (more than 4 minutes ago)
      long oldTime = System.currentTimeMillis() - 300000L; // 5 minutes ago
      when(testUser.getTokenExpireTime()).thenReturn(oldTime);
      when(mockKeycloakClient.getAccessToken("test-realm", testUser)).thenReturn("new-token");
      
      User result = userService.refreshUserToken(testUser);
      
      assertThat(result).isSameAs(testUser);
      verify(testUser).setToken("new-token");
      verify(testUser).setTokenExpireTime(any(Long.class));
    }

    @Test
    @DisplayName("Should not refresh token when token is still valid")
    void shouldNotRefreshTokenWhenTokenIsStillValid() {
      // Set token expire time to recent timestamp (less than 4 minutes ago)
      long recentTime = System.currentTimeMillis() - 60000L; // 1 minute ago
      when(testUser.getTokenExpireTime()).thenReturn(recentTime);
      
      User result = userService.refreshUserToken(testUser);
      
      assertThat(result).isSameAs(testUser);
      verify(testUser, never()).setToken(anyString());
      verify(testUser, never()).setTokenExpireTime(any(Long.class));
      verify(mockKeycloakClient, never()).getAccessToken(anyString(), any(User.class));
    }

    @Test
    @DisplayName("Should handle token refresh at boundary time")
    void shouldHandleTokenRefreshAtBoundaryTime() {
      // Set token expire time to exactly 4 minutes ago (240000ms)
      long boundaryTime = System.currentTimeMillis() - 240000L;
      when(testUser.getTokenExpireTime()).thenReturn(boundaryTime);
      when(mockKeycloakClient.getAccessToken("test-realm", testUser)).thenReturn("boundary-token");
      
      User result = userService.refreshUserToken(testUser);
      
      assertThat(result).isSameAs(testUser);
      verify(testUser).setToken("boundary-token");
      verify(testUser).setTokenExpireTime(any(Long.class));
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle complete user lifecycle")
    void shouldHandleCompleteUserLifecycle() {
      // Initialize user
      User initializedUser = userService.initUser(null, "testuser");
      assertThat(initializedUser).isEqualTo(testUser);
      
      // Refresh token
      when(testUser.getTokenExpireTime()).thenReturn(0L);
      when(mockKeycloakClient.getAccessToken("test-realm", testUser)).thenReturn("fresh-token");
      
      User refreshedUser = userService.refreshUserToken(initializedUser);
      assertThat(refreshedUser).isSameAs(initializedUser);
      
      verify(mockKeycloakClient).createUser(testUser);
      verify(testUser).setToken("fresh-token");
    }

    @Test
    @DisplayName("Should handle multiple users correctly")
    void shouldHandleMultipleUsersCorrectly() {
      User adminUser = userService.initUser(null, "admin");
      User testUserResult = userService.initUser(null, "testuser");
      
      assertThat(adminUser).isEqualTo(testUsers.get("admin"));
      assertThat(testUserResult).isEqualTo(testUsers.get("testuser"));
      assertThat(adminUser).isNotSameAs(testUserResult);
      
      verify(mockKeycloakClient, times(2)).createUser(any(User.class));
    }
  }
}
