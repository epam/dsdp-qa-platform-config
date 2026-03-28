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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import platform.qa.exceptions.api.BaseException;

@DisplayName("OpenshiftClientExceptions Tests")
public class OpenshiftClientExceptionsTest {

  @Nested
  @DisplayName("Utility Class Tests")
  class UtilityClassTests {

    @Test
    @DisplayName("Should be final class")
    void shouldBeFinalClass() {
      assertThat(Modifier.isFinal(OpenshiftClientExceptions.class.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should have private constructor")
    void shouldHavePrivateConstructor() throws NoSuchMethodException {
      Constructor<OpenshiftClientExceptions> constructor =
          OpenshiftClientExceptions.class.getDeclaredConstructor();
      assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalStateException when instantiated")
    void shouldThrowIllegalStateExceptionWhenInstantiated() throws NoSuchMethodException {
      Constructor<OpenshiftClientExceptions> constructor =
          OpenshiftClientExceptions.class.getDeclaredConstructor();
      constructor.setAccessible(true);

      assertThatThrownBy(constructor::newInstance)
          .isInstanceOf(InvocationTargetException.class)
          .hasCauseInstanceOf(IllegalStateException.class)
          .hasRootCauseMessage("This class can't be instantiated!");
    }

    @Test
    @DisplayName("Should have only one constructor")
    void shouldHaveOnlyOneConstructor() {
      Constructor<?>[] constructors = OpenshiftClientExceptions.class.getDeclaredConstructors();
      assertThat(constructors).hasSize(1);
    }

    @Test
    @DisplayName("Should not have public methods except nested classes")
    void shouldNotHavePublicMethodsExceptNestedClasses() {
      // The class should only contain nested exception classes, no user-defined public methods
      // Filter out JaCoCo-generated methods
      long userDefinedMethods =
          java.util.Arrays.stream(OpenshiftClientExceptions.class.getDeclaredMethods())
              .filter(method -> !method.getName().startsWith("$jacoco"))
              .count();
      assertThat(userDefinedMethods).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("OpenshiftNamespaceMissingException Tests")
  class OpenshiftNamespaceMissingExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Namespace is missing!";

      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(message);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should extend BaseException")
    void shouldExtendBaseException() {
      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException("test");

      assertThat(exception).isInstanceOf(BaseException.class);
      assertThat(exception).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
      String message = "Test namespace missing";

      assertThatThrownBy(
              () -> {
                throw new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(message);
              })
          .isInstanceOf(OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class)
          .hasMessage(message);
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(null);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
      String emptyMessage = "";

      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(emptyMessage);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(emptyMessage);
    }

    @Test
    @DisplayName("Should handle long message")
    void shouldHandleLongMessage() {
      String longMessage =
          "This is a very long error message that describes in detail "
              + "what went wrong with the OpenShift namespace configuration and why the "
              + "operation failed to complete successfully due to missing namespace information.";

      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(longMessage);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("Should handle special characters in message")
    void shouldHandleSpecialCharactersInMessage() {
      String specialMessage = "Namespace 'test-namespace-123' with special chars: !@#$%^&*()";

      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(specialMessage);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(specialMessage);
    }

    @Test
    @DisplayName("Should be final class")
    void shouldBeFinalClass() {
      assertThat(
              Modifier.isFinal(
                  OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class
                      .getModifiers()))
          .isTrue();
    }

    @Test
    @DisplayName("Should be static nested class")
    void shouldBeStaticNestedClass() {
      assertThat(
              Modifier.isStatic(
                  OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class
                      .getModifiers()))
          .isTrue();
    }

    @Test
    @DisplayName("Should be public class")
    void shouldBePublicClass() {
      assertThat(
              Modifier.isPublic(
                  OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class
                      .getModifiers()))
          .isTrue();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work in typical usage scenario")
    void shouldWorkInTypicalUsageScenario() {
      // Simulate typical usage as shown in class javadoc
      String namespaceName = "missing-namespace";
      String expectedMessage = "Namespace '" + namespaceName + "' is missing!";

      assertThatThrownBy(
              () -> {
                // Simulate some operation that checks for namespace
                if (namespaceName.contains("missing")) {
                  throw new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(
                      expectedMessage);
                }
              })
          .isInstanceOf(OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class)
          .hasMessage(expectedMessage)
          .isInstanceOf(BaseException.class);
    }

    @Test
    @DisplayName("Should work with exception chaining")
    void shouldWorkWithExceptionChaining() {
      String message = "Namespace validation failed";

      // Create the exception
      OpenshiftClientExceptions.OpenshiftNamespaceMissingException originalException =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException(message);

      // Test that it can be used in exception chaining
      assertThatThrownBy(
              () -> {
                try {
                  throw originalException;
                } catch (OpenshiftClientExceptions.OpenshiftNamespaceMissingException e) {
                  throw new RuntimeException("Wrapper exception", e);
                }
              })
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Wrapper exception")
          .hasCause(originalException);
    }

    @Test
    @DisplayName("Should maintain exception hierarchy")
    void shouldMaintainExceptionHierarchy() {
      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException("test");

      // Verify the complete inheritance chain (BaseException extends Exception, not
      // RuntimeException)
      assertThat(exception)
          .isInstanceOf(OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class);
      assertThat(exception).isInstanceOf(BaseException.class);
      assertThat(exception).isInstanceOf(Exception.class);
      assertThat(exception).isInstanceOf(Throwable.class);
    }

    @Test
    @DisplayName("Should support multiple instances")
    void shouldSupportMultipleInstances() {
      // Test that multiple instances can be created independently
      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception1 =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException("First exception");

      OpenshiftClientExceptions.OpenshiftNamespaceMissingException exception2 =
          new OpenshiftClientExceptions.OpenshiftNamespaceMissingException("Second exception");

      assertThat(exception1).isNotSameAs(exception2);
      assertThat(exception1.getMessage()).isEqualTo("First exception");
      assertThat(exception2.getMessage()).isEqualTo("Second exception");
    }
  }

  @Nested
  @DisplayName("Class Structure Tests")
  class ClassStructureTests {

    @Test
    @DisplayName("Should have exactly one nested class")
    void shouldHaveExactlyOneNestedClass() {
      Class<?>[] nestedClasses = OpenshiftClientExceptions.class.getDeclaredClasses();
      assertThat(nestedClasses).hasSize(1);
      assertThat(nestedClasses[0])
          .isEqualTo(OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class);
    }

    @Test
    @DisplayName("Should be in correct package")
    void shouldBeInCorrectPackage() {
      assertThat(OpenshiftClientExceptions.class.getPackage().getName())
          .isEqualTo("platform.qa.exceptions");
    }

    @Test
    @DisplayName("Should have correct class name")
    void shouldHaveCorrectClassName() {
      assertThat(OpenshiftClientExceptions.class.getSimpleName())
          .isEqualTo("OpenshiftClientExceptions");
      assertThat(OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class.getSimpleName())
          .isEqualTo("OpenshiftNamespaceMissingException");
    }

    @Test
    @DisplayName("Should not be abstract")
    void shouldNotBeAbstract() {
      assertThat(Modifier.isAbstract(OpenshiftClientExceptions.class.getModifiers())).isFalse();
      assertThat(
              Modifier.isAbstract(
                  OpenshiftClientExceptions.OpenshiftNamespaceMissingException.class
                      .getModifiers()))
          .isFalse();
    }
  }
}
