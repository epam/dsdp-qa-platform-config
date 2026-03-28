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

import platform.qa.exceptions.api.BaseException;

/**
 * Group of exceptions that related to configuration issues. Currently, available {@link
 * JsonConfigurationMissingException} and {@link PropertyConfigurationMissingException} Example of
 * usage:
 *
 * <p>{@code throw new ConfigurationExceptions.JsonConfigurationMissingException("Missing json
 * config for /path/file.json"); }
 */
public final class ConfigurationExceptions {
  private ConfigurationExceptions() {
    throw new IllegalStateException("This class can't be instantiated!");
  }

  public static final class JsonConfigurationMissingException extends BaseException {

    public JsonConfigurationMissingException(String message) {
      super(message);
    }
  }

  public static final class PropertyConfigurationMissingException extends BaseException {

    public PropertyConfigurationMissingException(String message) {
      super(message);
    }
  }

  public static final class MissingNamespaceInConfiguration extends BaseException {

    public MissingNamespaceInConfiguration(String message) {
      super(message);
    }
  }
}
