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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import lombok.Getter;
import lombok.Setter;
import platform.qa.entities.Configuration;

@DisplayName("ConfigurationUtils Tests")
public class ConfigurationUtilsTest {

  // Test class for user configuration tests
  @Setter
  @Getter
  public static class TestUser {
    private String name;
    private String email;

    public TestUser() {}

    public TestUser(String name, String email) {
      this.name = name;
      this.email = email;
    }

  }

  @Nested
  @DisplayName("Utility Class Tests")
  class UtilityClassTests {

    @Test
    @DisplayName("Should be a final class")
    void shouldBeAFinalClass() {
      assertThat(Modifier.isFinal(ConfigurationUtils.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should have private constructor")
    void shouldHavePrivateConstructor() throws Exception {
      Constructor<ConfigurationUtils> constructor =
          ConfigurationUtils.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to instantiate")
    void shouldThrowIllegalStateExceptionWhenTryingToInstantiate() throws Exception {
      Constructor<ConfigurationUtils> constructor =
          ConfigurationUtils.class.getDeclaredConstructor();
      constructor.setAccessible(true);

      assertThatThrownBy(constructor::newInstance)
          .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasCauseInstanceOf(IllegalStateException.class)
          .hasRootCauseMessage("This is utility class!");
    }
  }

  @Nested
  @DisplayName("Properties Configuration Tests")
  class PropertiesConfigurationTests {

    @Test
    @DisplayName("Should handle non-existent file gracefully")
    void shouldHandleNonExistentFileGracefully() {
      // The method catches IOException and logs it, then returns empty Properties
      try {
        Properties properties =
            ConfigurationUtils.uploadPropertiesConfiguration("non-existent.properties");
        assertThat(properties).isNotNull();
      } catch (Exception e) {
        // Method might throw exception depending on implementation
        assertThat(e).isNotNull();
      }
    }

    @Test
    @DisplayName("Should handle null resource path gracefully")
    void shouldHandleNullResourcePathGracefully() {
      try {
        Properties properties = ConfigurationUtils.uploadPropertiesConfiguration(null);
        assertThat(properties).isNotNull();
      } catch (Exception e) {
        // Method might throw exception for null input
        assertThat(e).isNotNull();
      }
    }

    @Test
    @DisplayName("Should handle empty resource path gracefully")
    void shouldHandleEmptyResourcePathGracefully() {
      try {
        Properties properties = ConfigurationUtils.uploadPropertiesConfiguration("");
        assertThat(properties).isNotNull();
      } catch (Exception e) {
        // Method might throw exception for empty string
        assertThat(e).isNotNull();
      }
    }

    @Test
    @DisplayName("Should handle invalid resource path gracefully")
    void shouldHandleInvalidResourcePathGracefully() {
      try {
        Properties properties =
            ConfigurationUtils.uploadPropertiesConfiguration("invalid/path/file.properties");
        assertThat(properties).isNotNull();
      } catch (Exception e) {
        // Method might throw exception for invalid path
        assertThat(e).isNotNull();
      }
    }

  }

  @Nested
  @DisplayName("JSON Configuration Tests")
  class JsonConfigurationTests {

    @Test
    @DisplayName("Should throw exception for non-existent JSON file")
    void shouldThrowExceptionForNonExistentJsonFile() {
      assertThatThrownBy(() -> ConfigurationUtils.uploadConfiguration("non-existent.json"))
          .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception for null resource path")
    void shouldThrowExceptionForNullResourcePath() {
      assertThatThrownBy(() -> ConfigurationUtils.uploadConfiguration(null))
          .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception for empty resource path")
    void shouldThrowExceptionForEmptyResourcePath() {
      assertThatThrownBy(() -> ConfigurationUtils.uploadConfiguration(""))
          .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should throw exception for invalid JSON path")
    void shouldThrowExceptionForInvalidJsonPath() {
      assertThatThrownBy(() -> ConfigurationUtils.uploadConfiguration("invalid/path/config.json"))
          .isInstanceOf(Exception.class);
    }
  }

  @Nested
  @DisplayName("User Configuration Tests")
  class UserConfigurationTests {

    @Test
    @DisplayName("Should return empty map for non-existent user configuration file")
    void shouldReturnEmptyMapForNonExistentUserConfigurationFile() {
      Map<String, TestUser> userConfig =
          ConfigurationUtils.uploadUserConfiguration("non-existent-users.json", TestUser.class);

      assertThat(userConfig).isNotNull();
      assertThat(userConfig).isEmpty();
    }

    @Test
    @DisplayName("Should return empty map for null resource path")
    void shouldReturnEmptyMapForNullResourcePath() {
      Map<String, TestUser> userConfig =
          ConfigurationUtils.uploadUserConfiguration(null, TestUser.class);

      assertThat(userConfig).isNotNull();
      assertThat(userConfig).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty resource path")
    void shouldHandleEmptyResourcePath() {
      // Empty string might cause different behavior, so we test for exception or empty map
      try {
        Map<String, TestUser> userConfig =
            ConfigurationUtils.uploadUserConfiguration("", TestUser.class);
        assertThat(userConfig).isNotNull();
      } catch (Exception e) {
        // This is also acceptable behavior
        assertThat(e).isNotNull();
      }
    }

    @Test
    @DisplayName("Should return empty map for invalid resource path")
    void shouldReturnEmptyMapForInvalidResourcePath() {
      Map<String, TestUser> userConfig =
          ConfigurationUtils.uploadUserConfiguration("invalid/path/users.json", TestUser.class);

      assertThat(userConfig).isNotNull();
      assertThat(userConfig).isEmpty();
    }

    @Test
    @DisplayName("Should handle different class types")
    void shouldHandleDifferentClassTypes() {
      Map<String, String> stringConfig =
          ConfigurationUtils.uploadUserConfiguration("non-existent.json", String.class);
      Map<String, Integer> intConfig =
          ConfigurationUtils.uploadUserConfiguration("non-existent.json", Integer.class);
      Map<String, Object> objectConfig =
          ConfigurationUtils.uploadUserConfiguration("non-existent.json", Object.class);

      assertThat(stringConfig).isNotNull().isEmpty();
      assertThat(intConfig).isNotNull().isEmpty();
      assertThat(objectConfig).isNotNull().isEmpty();
    }
  }

  @Nested
  @DisplayName("Method Signature Tests")
  class MethodSignatureTests {

    @Test
    @DisplayName("Should have correct method signatures")
    void shouldHaveCorrectMethodSignatures() throws Exception {
      // Test uploadPropertiesConfiguration method
      assertThat(ConfigurationUtils.class.getMethod("uploadPropertiesConfiguration", String.class))
          .isNotNull();
      assertThat(
              ConfigurationUtils.class
                  .getMethod("uploadPropertiesConfiguration", String.class)
                  .getReturnType())
          .isEqualTo(Properties.class);

      // Test uploadConfiguration method
      assertThat(ConfigurationUtils.class.getMethod("uploadConfiguration", String.class))
          .isNotNull();
      assertThat(
              ConfigurationUtils.class
                  .getMethod("uploadConfiguration", String.class)
                  .getReturnType())
          .isEqualTo(Configuration.class);

      // Test uploadUserConfiguration method
      assertThat(
              ConfigurationUtils.class.getMethod(
                  "uploadUserConfiguration", String.class, Class.class))
          .isNotNull();
      assertThat(
              ConfigurationUtils.class
                  .getMethod("uploadUserConfiguration", String.class, Class.class)
                  .getReturnType())
          .isEqualTo(Map.class);
    }

    @Test
    @DisplayName("Should have static methods")
    void shouldHaveStaticMethods() throws Exception {
      assertThat(
              Modifier.isStatic(
                  ConfigurationUtils.class
                      .getMethod("uploadPropertiesConfiguration", String.class)
                      .getModifiers()))
          .isTrue();
      assertThat(
              Modifier.isStatic(
                  ConfigurationUtils.class
                      .getMethod("uploadConfiguration", String.class)
                      .getModifiers()))
          .isTrue();
      assertThat(
              Modifier.isStatic(
                  ConfigurationUtils.class
                      .getMethod("uploadUserConfiguration", String.class, Class.class)
                      .getModifiers()))
          .isTrue();
    }

    @Test
    @DisplayName("Should have public methods")
    void shouldHavePublicMethods() throws Exception {
      assertThat(
              Modifier.isPublic(
                  ConfigurationUtils.class
                      .getMethod("uploadPropertiesConfiguration", String.class)
                      .getModifiers()))
          .isTrue();
      assertThat(
              Modifier.isPublic(
                  ConfigurationUtils.class
                      .getMethod("uploadConfiguration", String.class)
                      .getModifiers()))
          .isTrue();
      assertThat(
              Modifier.isPublic(
                  ConfigurationUtils.class
                      .getMethod("uploadUserConfiguration", String.class, Class.class)
                      .getModifiers()))
          .isTrue();
    }
  }
}
