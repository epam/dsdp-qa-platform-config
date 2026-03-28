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

package platform.qa.providers.impl;

import lombok.Getter;
import platform.qa.entities.User;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.providers.api.AtomicOperation;
import platform.qa.services.UserService;
import platform.qa.utils.ConfigurationUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Provide registry users data by username
 */
public class RegistryUserProvider implements AtomicOperation<User> {
    private AtomicReference<User> user = new AtomicReference<>();
    @Getter
    private Map<String, User> users;
    @Getter
    private UserService userService;

    public RegistryUserProvider(String namespace, KeycloakClient keycloakClient, String usersFilePath) {
        this.users = getRegistryUsersFromJson(usersFilePath, namespace);
        userService = new UserService(users, keycloakClient);
    }

    @Override
    public User get(String name) {
        return user.updateAndGet((user) -> {
            User currentUser = userService.initUser(user, name);
            return userService.refreshUserToken(currentUser);
        });
    }

    public User get(String name, String namespace) {
        return user.updateAndGet((user) -> {
            User currentUser = userService.initUser(user, name, namespace);
            return userService.refreshUserToken(currentUser);
        });
    }

    public User getInRealm(String name, String namespace, String keycloakRealm) {
        return user.updateAndGet((user) -> {
            User currentUser = userService.initUserInRealm(user, name, namespace, keycloakRealm);
            return userService.refreshUserToken(currentUser);
        });
    }

    private Map<String, User> getRegistryUsersFromJson(String path, String namespace) {
        Map<String, User> users = ConfigurationUtils.uploadUserConfiguration(path, User.class);
        users.values().forEach(user -> {
            user.setPassword(user.getLogin());
            if (user.getRealm().startsWith("-"))
                user.setRealm(namespace + user.getRealm());

            updateRolesForAdminPortal(namespace, user);
        });
        return users;
    }

    private void updateRolesForAdminPortal(String namespace, User user) {
        List<String> realmRoles = user.getRealmRoles();

        if ("admin-portal".equals(user.getClientId())) {
            List<String> rolesUpdate = realmRoles.stream()
                    .map(role -> "cp-registry-admin".equals(role) ? "cp-registry-admin-".concat(namespace) : role)
                    .collect(Collectors.toList());

            user.setRealmRoles(rolesUpdate);
        }
    }
}
