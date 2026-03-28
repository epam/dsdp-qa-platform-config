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

package platform.qa.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Configuration Entity Tests")
public class ConfigurationTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create Configuration with no-args constructor")
    void shouldCreateConfigurationWithNoArgsConstructor() {
      Configuration configuration = new Configuration();
      
      assertThat(configuration).isNotNull();
      assertThat(configuration.getCentralConfiguration()).isNull();
      assertThat(configuration.getRegistryConfiguration()).isNull();
    }

    @Test
    @DisplayName("Should create Configuration with all-args constructor")
    void shouldCreateConfigurationWithAllArgsConstructor() {
      CentralConfiguration centralConfig = new CentralConfiguration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      Configuration configuration = new Configuration(centralConfig, registryConfig);
      
      assertThat(configuration).isNotNull();
      assertThat(configuration.getCentralConfiguration()).isEqualTo(centralConfig);
      assertThat(configuration.getRegistryConfiguration()).isEqualTo(registryConfig);
    }

    @Test
    @DisplayName("Should handle null values in all-args constructor")
    void shouldHandleNullValuesInAllArgsConstructor() {
      Configuration configuration = new Configuration(null, null);
      
      assertThat(configuration).isNotNull();
      assertThat(configuration.getCentralConfiguration()).isNull();
      assertThat(configuration.getRegistryConfiguration()).isNull();
    }
  }

  @Nested
  @DisplayName("Getter and Setter Tests")
  class GetterSetterTests {

    @Test
    @DisplayName("Should set and get central configuration")
    void shouldSetAndGetCentralConfiguration() {
      Configuration configuration = new Configuration();
      CentralConfiguration centralConfig = new CentralConfiguration();
      
      configuration.setCentralConfiguration(centralConfig);
      
      assertThat(configuration.getCentralConfiguration()).isEqualTo(centralConfig);
    }

    @Test
    @DisplayName("Should set and get registry configuration")
    void shouldSetAndGetRegistryConfiguration() {
      Configuration configuration = new Configuration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      configuration.setRegistryConfiguration(registryConfig);
      
      assertThat(configuration.getRegistryConfiguration()).isEqualTo(registryConfig);
    }

    @Test
    @DisplayName("Should handle null values in setters")
    void shouldHandleNullValuesInSetters() {
      Configuration configuration = new Configuration();
      
      configuration.setCentralConfiguration(null);
      configuration.setRegistryConfiguration(null);
      
      assertThat(configuration.getCentralConfiguration()).isNull();
      assertThat(configuration.getRegistryConfiguration()).isNull();
    }

    @Test
    @DisplayName("Should allow overwriting existing values")
    void shouldAllowOverwritingExistingValues() {
      Configuration configuration = new Configuration();
      CentralConfiguration centralConfig1 = new CentralConfiguration();
      CentralConfiguration centralConfig2 = new CentralConfiguration();
      RegistryConfiguration registryConfig1 = new RegistryConfiguration();
      RegistryConfiguration registryConfig2 = new RegistryConfiguration();
      
      configuration.setCentralConfiguration(centralConfig1);
      configuration.setRegistryConfiguration(registryConfig1);
      
      assertThat(configuration.getCentralConfiguration()).isEqualTo(centralConfig1);
      assertThat(configuration.getRegistryConfiguration()).isEqualTo(registryConfig1);
      
      configuration.setCentralConfiguration(centralConfig2);
      configuration.setRegistryConfiguration(registryConfig2);
      
      assertThat(configuration.getCentralConfiguration()).isEqualTo(centralConfig2);
      assertThat(configuration.getRegistryConfiguration()).isEqualTo(registryConfig2);
    }
  }

  @Nested
  @DisplayName("Lombok Generated Methods Tests")
  class LombokGeneratedMethodsTests {

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
      CentralConfiguration centralConfig = new CentralConfiguration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      Configuration config1 = new Configuration(centralConfig, registryConfig);
      Configuration config2 = new Configuration(centralConfig, registryConfig);
      Configuration config3 = new Configuration(null, null);
      
      assertThat(config1).isEqualTo(config2);
      assertThat(config1).isNotEqualTo(config3);
      assertThat(config1).isNotEqualTo(null);
      assertThat(config1).isEqualTo(config1);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
      CentralConfiguration centralConfig = new CentralConfiguration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      Configuration config1 = new Configuration(centralConfig, registryConfig);
      Configuration config2 = new Configuration(centralConfig, registryConfig);
      
      assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
      CentralConfiguration centralConfig = new CentralConfiguration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      Configuration configuration = new Configuration(centralConfig, registryConfig);
      String toString = configuration.toString();
      
      assertThat(toString).isNotNull();
      assertThat(toString).contains("Configuration");
      assertThat(toString).contains("centralConfiguration");
      assertThat(toString).contains("registryConfiguration");
    }

    @Test
    @DisplayName("Should handle equals with different object types")
    void shouldHandleEqualsWithDifferentObjectTypes() {
      Configuration configuration = new Configuration();
      
      assertThat(configuration).isNotEqualTo("string");
      assertThat(configuration).isNotEqualTo(123);
      assertThat(configuration).isNotEqualTo(new Object());
    }

    @Test
    @DisplayName("Should handle equals with null fields")
    void shouldHandleEqualsWithNullFields() {
      Configuration config1 = new Configuration(null, null);
      Configuration config2 = new Configuration(null, null);
      Configuration config3 = new Configuration(new CentralConfiguration(), null);
      
      assertThat(config1).isEqualTo(config2);
      assertThat(config1).isNotEqualTo(config3);
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work correctly in typical usage scenario")
    void shouldWorkCorrectlyInTypicalUsageScenario() {
      // Create configurations
      CentralConfiguration centralConfig = new CentralConfiguration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      // Create main configuration
      Configuration configuration = new Configuration();
      
      // Set configurations
      configuration.setCentralConfiguration(centralConfig);
      configuration.setRegistryConfiguration(registryConfig);
      
      // Verify state
      assertThat(configuration.getCentralConfiguration()).isNotNull();
      assertThat(configuration.getRegistryConfiguration()).isNotNull();
      assertThat(configuration.getCentralConfiguration()).isEqualTo(centralConfig);
      assertThat(configuration.getRegistryConfiguration()).isEqualTo(registryConfig);
      
      // Test immutability of references
      CentralConfiguration retrievedCentral = configuration.getCentralConfiguration();
      RegistryConfiguration retrievedRegistry = configuration.getRegistryConfiguration();
      
      assertThat(retrievedCentral).isSameAs(centralConfig);
      assertThat(retrievedRegistry).isSameAs(registryConfig);
    }

    @Test
    @DisplayName("Should support builder pattern usage")
    void shouldSupportBuilderPatternUsage() {
      CentralConfiguration centralConfig = new CentralConfiguration();
      RegistryConfiguration registryConfig = new RegistryConfiguration();
      
      Configuration configuration = new Configuration();
      Configuration result = configuration;
      
      // Simulate builder pattern
      result.setCentralConfiguration(centralConfig);
      result.setRegistryConfiguration(registryConfig);
      
      assertThat(result).isSameAs(configuration);
      assertThat(result.getCentralConfiguration()).isEqualTo(centralConfig);
      assertThat(result.getRegistryConfiguration()).isEqualTo(registryConfig);
    }
  }
}
