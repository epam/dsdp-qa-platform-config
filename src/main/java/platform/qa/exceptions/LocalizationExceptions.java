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

/**
 * Group of exceptions that related to localization issues. Currently, available {@link
 * LocalizationFileLoadException}, {@link LocaleNotFoundException} and {@link
 * LocalizationKeyNotFoundException} Example of usage:
 *
 * <p>{@code throw new LocalizationExceptions.LocalizationFileLoadException("Failed to load
 * localization file", exception); }
 */
public final class LocalizationExceptions {
  private LocalizationExceptions() {
    throw new IllegalStateException("This class can't be instantiated!");
  }

  /**
   * Exception thrown when a localization file cannot be loaded (e.g., file not found, invalid JSON
   * format).
   */
  public static final class LocalizationFileLoadException extends RuntimeException {

    public LocalizationFileLoadException(String message, Throwable cause) {
      super(message, cause);
    }

    public LocalizationFileLoadException(Throwable cause) {
      super(cause);
    }
  }

  /** Exception thrown when a requested locale is not found in the localization file. */
  public static final class LocaleNotFoundException extends RuntimeException {

    public LocaleNotFoundException(String message) {
      super(message);
    }
  }

  /**
   * Exception thrown when a requested localization key is not found for a specific locale in the
   * localization file.
   */
  public static final class LocalizationKeyNotFoundException extends RuntimeException {

    public LocalizationKeyNotFoundException(String message) {
      super(message);
    }
  }
}
