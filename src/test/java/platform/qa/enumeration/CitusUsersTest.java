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

package platform.qa.enumeration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("CitusUsers Enum Tests")
public class CitusUsersTest {

  @Nested
  @DisplayName("Enum Values Tests")
  class EnumValuesTests {

    @Test
    @DisplayName("Should have correct role names for all enum values")
    void shouldHaveCorrectRoleNames() {
      assertThat(CitusUsers.ADMIN_ROLE.getRoleName()).isEqualTo("admRole");
      assertThat(CitusUsers.APPLICATION_ROLE.getRoleName()).isEqualTo("appRole");
      assertThat(CitusUsers.REGISTRY_OWNER_ROLE.getRoleName()).isEqualTo("regOwner");
      assertThat(CitusUsers.SETTINGS_ROLE.getRoleName()).isEqualTo("settRole");
      assertThat(CitusUsers.AUDIT_ROLE.getRoleName()).isEqualTo("anSvc");
      assertThat(CitusUsers.ANALYTICS_ROLE.getRoleName()).isEqualTo("anRole");
      assertThat(CitusUsers.EXCERPT_EXPORTER_ROLE.getRoleName()).isEqualTo("excerptExporter");
      assertThat(CitusUsers.EXCERPT_ROLE.getRoleName()).isEqualTo("excerptSvc");
      assertThat(CitusUsers.EXCERPT_WORKER_ROLE.getRoleName()).isEqualTo("excerptWork");
    }

    @Test
    @DisplayName("Should have all expected enum values")
    void shouldHaveAllExpectedEnumValues() {
      CitusUsers[] values = CitusUsers.values();
      
      assertThat(values).hasSize(9);
      assertThat(values).containsExactly(
          CitusUsers.ADMIN_ROLE,
          CitusUsers.APPLICATION_ROLE,
          CitusUsers.REGISTRY_OWNER_ROLE,
          CitusUsers.SETTINGS_ROLE,
          CitusUsers.AUDIT_ROLE,
          CitusUsers.ANALYTICS_ROLE,
          CitusUsers.EXCERPT_EXPORTER_ROLE,
          CitusUsers.EXCERPT_ROLE,
          CitusUsers.EXCERPT_WORKER_ROLE
      );
    }

    @Test
    @DisplayName("Should be able to get enum by name")
    void shouldBeAbleToGetEnumByName() {
      assertThat(CitusUsers.valueOf("ADMIN_ROLE")).isEqualTo(CitusUsers.ADMIN_ROLE);
      assertThat(CitusUsers.valueOf("APPLICATION_ROLE")).isEqualTo(CitusUsers.APPLICATION_ROLE);
      assertThat(CitusUsers.valueOf("REGISTRY_OWNER_ROLE")).isEqualTo(CitusUsers.REGISTRY_OWNER_ROLE);
      assertThat(CitusUsers.valueOf("SETTINGS_ROLE")).isEqualTo(CitusUsers.SETTINGS_ROLE);
      assertThat(CitusUsers.valueOf("AUDIT_ROLE")).isEqualTo(CitusUsers.AUDIT_ROLE);
      assertThat(CitusUsers.valueOf("ANALYTICS_ROLE")).isEqualTo(CitusUsers.ANALYTICS_ROLE);
      assertThat(CitusUsers.valueOf("EXCERPT_EXPORTER_ROLE")).isEqualTo(CitusUsers.EXCERPT_EXPORTER_ROLE);
      assertThat(CitusUsers.valueOf("EXCERPT_ROLE")).isEqualTo(CitusUsers.EXCERPT_ROLE);
      assertThat(CitusUsers.valueOf("EXCERPT_WORKER_ROLE")).isEqualTo(CitusUsers.EXCERPT_WORKER_ROLE);
    }
  }

  @Nested
  @DisplayName("Role Name Validation Tests")
  class RoleNameValidationTests {

    @Test
    @DisplayName("Should return non-null role names")
    void shouldReturnNonNullRoleNames() {
      for (CitusUsers user : CitusUsers.values()) {
        assertThat(user.getRoleName()).isNotNull();
        assertThat(user.getRoleName()).isNotEmpty();
      }
    }

    @Test
    @DisplayName("Should have unique role names")
    void shouldHaveUniqueRoleNames() {
      String[] roleNames = new String[CitusUsers.values().length];
      for (int i = 0; i < CitusUsers.values().length; i++) {
        roleNames[i] = CitusUsers.values()[i].getRoleName();
      }
      
      assertThat(roleNames).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("Should maintain consistency between enum name and role name pattern")
    void shouldMaintainConsistencyBetweenEnumNameAndRoleName() {
      // Verify that role names follow expected patterns
      assertThat(CitusUsers.ADMIN_ROLE.getRoleName().toLowerCase()).contains("role");
      assertThat(CitusUsers.APPLICATION_ROLE.getRoleName().toLowerCase()).contains("role");
      assertThat(CitusUsers.SETTINGS_ROLE.getRoleName().toLowerCase()).contains("role");
      assertThat(CitusUsers.AUDIT_ROLE.getRoleName()).contains("Svc");
      assertThat(CitusUsers.ANALYTICS_ROLE.getRoleName().toLowerCase()).contains("role");
      assertThat(CitusUsers.EXCERPT_EXPORTER_ROLE.getRoleName()).contains("excerpt");
      assertThat(CitusUsers.EXCERPT_ROLE.getRoleName()).contains("Svc");
      assertThat(CitusUsers.EXCERPT_WORKER_ROLE.getRoleName()).contains("Work");
    }
  }
}
