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

package platform.qa.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import platform.qa.exceptions.api.BaseException;

@DisplayName("ConfigurationExceptions Tests")
public class ConfigurationExceptionsTest {

  @Nested
  @DisplayName("Utility Class Tests")
  class UtilityClassTests {

    @Test
    @DisplayName("Should be a final class")
    void shouldBeAFinalClass() {
      assertThat(Modifier.isFinal(ConfigurationExceptions.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should have private constructor")
    void shouldHavePrivateConstructor() throws Exception {
      Constructor<ConfigurationExceptions> constructor = ConfigurationExceptions.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when trying to instantiate")
    void shouldThrowIllegalStateExceptionWhenTryingToInstantiate() throws Exception {
      Constructor<ConfigurationExceptions> constructor = ConfigurationExceptions.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      
      assertThatThrownBy(constructor::newInstance)
          .isInstanceOf(java.lang.reflect.InvocationTargetException.class)
          .hasCauseInstanceOf(IllegalStateException.class)
          .hasRootCauseMessage("This class can't be instantiated!");
    }
  }

  @Nested
  @DisplayName("JsonConfigurationMissingException Tests")
  class JsonConfigurationMissingExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Missing json config for /path/file.json";
      
      ConfigurationExceptions.JsonConfigurationMissingException exception = 
          new ConfigurationExceptions.JsonConfigurationMissingException(message);
      
      assertThat(exception).isInstanceOf(BaseException.class);
      assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should be a final class")
    void shouldBeAFinalClass() {
      assertThat(Modifier.isFinal(ConfigurationExceptions.JsonConfigurationMissingException.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should be a static nested class")
    void shouldBeAStaticNestedClass() {
      assertThat(Modifier.isStatic(ConfigurationExceptions.JsonConfigurationMissingException.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
      ConfigurationExceptions.JsonConfigurationMissingException exception = 
          new ConfigurationExceptions.JsonConfigurationMissingException(null);
      
      assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
      ConfigurationExceptions.JsonConfigurationMissingException exception = 
          new ConfigurationExceptions.JsonConfigurationMissingException("");
      
      assertThat(exception.getMessage()).isEmpty();
    }
  }

  @Nested
  @DisplayName("PropertyConfigurationMissingException Tests")
  class PropertyConfigurationMissingExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Missing property configuration for key: test.property";
      
      ConfigurationExceptions.PropertyConfigurationMissingException exception = 
          new ConfigurationExceptions.PropertyConfigurationMissingException(message);
      
      assertThat(exception).isInstanceOf(BaseException.class);
      assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should be a final class")
    void shouldBeAFinalClass() {
      assertThat(Modifier.isFinal(ConfigurationExceptions.PropertyConfigurationMissingException.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should be a static nested class")
    void shouldBeAStaticNestedClass() {
      assertThat(Modifier.isStatic(ConfigurationExceptions.PropertyConfigurationMissingException.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
      ConfigurationExceptions.PropertyConfigurationMissingException exception = 
          new ConfigurationExceptions.PropertyConfigurationMissingException(null);
      
      assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
      ConfigurationExceptions.PropertyConfigurationMissingException exception = 
          new ConfigurationExceptions.PropertyConfigurationMissingException("");
      
      assertThat(exception.getMessage()).isEmpty();
    }
  }

  @Nested
  @DisplayName("MissingNamespaceInConfiguration Tests")
  class MissingNamespaceInConfigurationTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Missing namespace in configuration";
      
      ConfigurationExceptions.MissingNamespaceInConfiguration exception = 
          new ConfigurationExceptions.MissingNamespaceInConfiguration(message);
      
      assertThat(exception).isInstanceOf(BaseException.class);
      assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should be a final class")
    void shouldBeAFinalClass() {
      assertThat(Modifier.isFinal(ConfigurationExceptions.MissingNamespaceInConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should be a static nested class")
    void shouldBeAStaticNestedClass() {
      assertThat(Modifier.isStatic(ConfigurationExceptions.MissingNamespaceInConfiguration.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
      ConfigurationExceptions.MissingNamespaceInConfiguration exception = 
          new ConfigurationExceptions.MissingNamespaceInConfiguration(null);
      
      assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
      ConfigurationExceptions.MissingNamespaceInConfiguration exception = 
          new ConfigurationExceptions.MissingNamespaceInConfiguration("");
      
      assertThat(exception.getMessage()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Exception Hierarchy Tests")
  class ExceptionHierarchyTests {

    @Test
    @DisplayName("All exceptions should extend BaseException")
    void allExceptionsShouldExtendBaseException() {
      assertThat(BaseException.class.isAssignableFrom(ConfigurationExceptions.JsonConfigurationMissingException.class)).isTrue();
      assertThat(BaseException.class.isAssignableFrom(ConfigurationExceptions.PropertyConfigurationMissingException.class)).isTrue();
      assertThat(BaseException.class.isAssignableFrom(ConfigurationExceptions.MissingNamespaceInConfiguration.class)).isTrue();
    }

    @Test
    @DisplayName("Should be able to catch all exceptions as BaseException")
    void shouldBeAbleToCatchAllExceptionsAsBaseException() {
      String message = "Test message";
      
      BaseException jsonException = new ConfigurationExceptions.JsonConfigurationMissingException(message);
      BaseException propertyException = new ConfigurationExceptions.PropertyConfigurationMissingException(message);
      BaseException namespaceException = new ConfigurationExceptions.MissingNamespaceInConfiguration(message);
      
      assertThat(jsonException.getMessage()).isEqualTo(message);
      assertThat(propertyException.getMessage()).isEqualTo(message);
      assertThat(namespaceException.getMessage()).isEqualTo(message);
    }
  }
}
