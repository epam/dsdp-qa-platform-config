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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.reflect.TypeToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import platform.qa.entities.Configuration;

/**
 * Utilities for configuration upload. Currently, supports load to {@link Configuration}, {@link
 * Properties}, {@link Map<String,Object>} Example of usage:
 *
 * <p>{@code Properties props =
 * ConfigurationUtils.uploadPropertiesConfiguration("example.properties"); Configuration config =
 * ConfigurationUtils.uploadConfiguration("example.json"); Map<String, User> users =
 * ConfigurationUtils.uploadUserConfiguration("example_users.json", User.class); }
 */
@Log4j2
public final class ConfigurationUtils {
  private ConfigurationUtils() {
    throw new IllegalStateException("This is utility class!");
  }

  /**
   * Upload configuration from property file to {@link Properties}
   *
   * @param resourcePath path to properties resource
   * @return {@link Properties}
   */
  public static Properties uploadPropertiesConfiguration(String resourcePath) {
    Properties properties = new Properties();
    try {
      URL url = ConfigurationUtils.class.getClassLoader().getResource(resourcePath);
      properties.load(url.openStream());
    } catch (IOException e) {
      log.info("Configuration file wasn't found by path: {}", resourcePath, e);
    }
    return properties;
  }

  /**
   * Upload configuration form json file that has the same structure as {@link Configuration}
   *
   * @param resourcePath path to json resource
   * @return {@link Configuration}
   */
  @SneakyThrows()
  public static Configuration uploadConfiguration(String resourcePath) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      mapper.registerModule(new Jdk8Module());

      // this.class.getClassLoader() used required to read configuration from jar instead of test
      // project
      InputStream is = ConfigurationUtils.class.getClassLoader().getResourceAsStream(resourcePath);
      return (Configuration) mapper.readValue(is, new TypeToken<Configuration>() {}.getRawType());
    } catch (IOException ex) {
      throw new UncheckedIOException("Configuration file wasn't found by path: " + resourcePath, ex);
    }
  }

  /**
   * Upload configuration from json that has map structure to {@link Map} of <String, T>
   *
   * @param resourcePath path to json resource
   * @param clazzValue class of key object
   * @param <T> type of key object
   * @return {@link Map<String, T>}
   */
  @SuppressWarnings("java:S1166")
  @SneakyThrows(IOException.class)
  public static <T> Map<String, T> uploadUserConfiguration(
      String resourcePath, Class<T> clazzValue) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    objectMapper.registerModule(new Jdk8Module());

    try {
      URL resource =
          Objects.requireNonNull(
              ConfigurationUtils.class.getClassLoader().getResource(resourcePath));
      MapLikeType type =
          objectMapper
              .getTypeFactory()
              .constructMapLikeType(LinkedHashMap.class, String.class, clazzValue);
      return objectMapper.readValue(resource, type);
    } catch (NullPointerException ignored) {
      log.info("User Configuration file wasn't found by path: {}", resourcePath);
      return new HashMap<>();
    }
  }
}
