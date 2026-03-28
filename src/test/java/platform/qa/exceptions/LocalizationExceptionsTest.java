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

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("LocalizationExceptions Tests")
public class LocalizationExceptionsTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should not allow instantiation of LocalizationExceptions")
    void shouldNotAllowInstantiationOfLocalizationExceptions() throws Exception {
      var constructor = LocalizationExceptions.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      
      assertThatThrownBy(constructor::newInstance)
          .cause()
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("This class can't be instantiated!");
    }
  }

  @Nested
  @DisplayName("LocalizationFileLoadException Tests")
  class LocalizationFileLoadExceptionTests {

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
      IOException cause = new IOException("File not found");
      String message = "Failed to load localization file";

      LocalizationExceptions.LocalizationFileLoadException exception =
          new LocalizationExceptions.LocalizationFileLoadException(message, cause);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(message);
      assertThat(exception.getCause()).isEqualTo(cause);
      assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should create exception with cause only")
    void shouldCreateExceptionWithCauseOnly() {
      IOException cause = new IOException("Invalid JSON format");

      LocalizationExceptions.LocalizationFileLoadException exception =
          new LocalizationExceptions.LocalizationFileLoadException(cause);

      assertThat(exception).isNotNull();
      assertThat(exception.getCause()).isEqualTo(cause);
      assertThat(exception.getMessage()).contains("IOException");
      assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
      IOException cause = new IOException("Test cause");

      assertThatThrownBy(
              () -> {
                throw new LocalizationExceptions.LocalizationFileLoadException(
                    "Test message", cause);
              })
          .isInstanceOf(LocalizationExceptions.LocalizationFileLoadException.class)
          .hasMessage("Test message")
          .hasCause(cause);
    }
  }

  @Nested
  @DisplayName("LocaleNotFoundException Tests")
  class LocaleNotFoundExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Locale not found: de";

      LocalizationExceptions.LocaleNotFoundException exception =
          new LocalizationExceptions.LocaleNotFoundException(message);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(message);
      assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
      assertThatThrownBy(
              () -> {
                throw new LocalizationExceptions.LocaleNotFoundException("Locale not found: fr");
              })
          .isInstanceOf(LocalizationExceptions.LocaleNotFoundException.class)
          .hasMessage("Locale not found: fr");
    }

    @Test
    @DisplayName("Should handle various locale codes")
    void shouldHandleVariousLocaleCodes() {
      String[] localeCodes = {"en", "az", "uk", "de", "fr", "es"};

      for (String locale : localeCodes) {
        LocalizationExceptions.LocaleNotFoundException exception =
            new LocalizationExceptions.LocaleNotFoundException("Locale not found: " + locale);

        assertThat(exception.getMessage()).isEqualTo("Locale not found: " + locale);
      }
    }
  }

  @Nested
  @DisplayName("LocalizationKeyNotFoundException Tests")
  class LocalizationKeyNotFoundExceptionTests {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
      String message = "Key not found: Name for locale: en";

      LocalizationExceptions.LocalizationKeyNotFoundException exception =
          new LocalizationExceptions.LocalizationKeyNotFoundException(message);

      assertThat(exception).isNotNull();
      assertThat(exception.getMessage()).isEqualTo(message);
      assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should be throwable")
    void shouldBeThrowable() {
      assertThatThrownBy(
              () -> {
                throw new LocalizationExceptions.LocalizationKeyNotFoundException(
                    "Key not found: Email for locale: az");
              })
          .isInstanceOf(LocalizationExceptions.LocalizationKeyNotFoundException.class)
          .hasMessage("Key not found: Email for locale: az");
    }

    @Test
    @DisplayName("Should handle various key and locale combinations")
    void shouldHandleVariousKeyAndLocaleCombinations() {
      String[][] combinations = {
        {"Name", "en"},
        {"Email", "az"},
        {"Phone", "uk"},
        {"Address", "de"}
      };

      for (String[] combo : combinations) {
        String key = combo[0];
        String locale = combo[1];
        String message = "Key not found: " + key + " for locale: " + locale;

        LocalizationExceptions.LocalizationKeyNotFoundException exception =
            new LocalizationExceptions.LocalizationKeyNotFoundException(message);

        assertThat(exception.getMessage()).isEqualTo(message);
      }
    }
  }

  @Nested
  @DisplayName("Exception Hierarchy Tests")
  class ExceptionHierarchyTests {

    @Test
    @DisplayName("All exceptions should extend RuntimeException")
    void allExceptionsShouldExtendRuntimeException() {
      IOException cause = new IOException("test");

      LocalizationExceptions.LocalizationFileLoadException fileException =
          new LocalizationExceptions.LocalizationFileLoadException("test", cause);
      LocalizationExceptions.LocaleNotFoundException localeException =
          new LocalizationExceptions.LocaleNotFoundException("test");
      LocalizationExceptions.LocalizationKeyNotFoundException keyException =
          new LocalizationExceptions.LocalizationKeyNotFoundException("test");

      assertThat(fileException).isInstanceOf(RuntimeException.class);
      assertThat(localeException).isInstanceOf(RuntimeException.class);
      assertThat(keyException).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Exceptions should be distinguishable by type")
    void exceptionsShouldBeDistinguishableByType() {
      IOException cause = new IOException("test");

      RuntimeException fileException =
          new LocalizationExceptions.LocalizationFileLoadException("test", cause);
      RuntimeException localeException =
          new LocalizationExceptions.LocaleNotFoundException("test");
      RuntimeException keyException =
          new LocalizationExceptions.LocalizationKeyNotFoundException("test");

      assertThat(fileException)
          .isInstanceOf(LocalizationExceptions.LocalizationFileLoadException.class);
      assertThat(localeException)
          .isInstanceOf(LocalizationExceptions.LocaleNotFoundException.class);
      assertThat(keyException)
          .isInstanceOf(LocalizationExceptions.LocalizationKeyNotFoundException.class);

      assertThat(fileException).isNotInstanceOf(LocalizationExceptions.LocaleNotFoundException.class)
              .isNotInstanceOf(LocalizationExceptions.LocalizationKeyNotFoundException.class);
    }
  }
}
