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
import org.junit.jupiter.api.Test;

@DisplayName("ServiceConfiguration Entity Tests")
public class ServiceConfigurationTest {

  @Test
  @DisplayName("Should create ServiceConfiguration with no-args constructor")
  void shouldCreateServiceConfigurationWithNoArgsConstructor() {
    ServiceConfiguration config = new ServiceConfiguration();
    
    assertThat(config).isNotNull();
    assertThat(config.getPodLabel()).isNull();
    assertThat(config.getSecret()).isNull();
    assertThat(config.getNamespace()).isNull();
    assertThat(config.getRoute()).isNull();
    assertThat(config.isPortForwarding()).isFalse();
    assertThat(config.getDefaultPort()).isEqualTo(0);
  }

  @Test
  @DisplayName("Should set and get podLabel")
  void shouldSetAndGetPodLabel() {
    ServiceConfiguration config = new ServiceConfiguration();
    String podLabel = "app=test-service";
    
    config.setPodLabel(podLabel);
    
    assertThat(config.getPodLabel()).isEqualTo(podLabel);
  }

  @Test
  @DisplayName("Should set and get namespace")
  void shouldSetAndGetNamespace() {
    ServiceConfiguration config = new ServiceConfiguration();
    String namespace = "test-namespace";
    
    config.setNamespace(namespace);
    
    assertThat(config.getNamespace()).isEqualTo(namespace);
  }

  @Test
  @DisplayName("Should set and get route")
  void shouldSetAndGetRoute() {
    ServiceConfiguration config = new ServiceConfiguration();
    String route = "test-route";
    
    config.setRoute(route);
    
    assertThat(config.getRoute()).isEqualTo(route);
  }

  @Test
  @DisplayName("Should set and get secret")
  void shouldSetAndGetSecret() {
    ServiceConfiguration config = new ServiceConfiguration();
    String secret = "test-secret";
    
    config.setSecret(secret);
    
    assertThat(config.getSecret()).isEqualTo(secret);
  }

  @Test
  @DisplayName("Should set and get portForwarding")
  void shouldSetAndGetPortForwarding() {
    ServiceConfiguration config = new ServiceConfiguration();
    
    config.setPortForwarding(true);
    
    assertThat(config.isPortForwarding()).isTrue();
  }

  @Test
  @DisplayName("Should set and get defaultPort")
  void shouldSetAndGetDefaultPort() {
    ServiceConfiguration config = new ServiceConfiguration();
    int port = 8080;
    
    config.setDefaultPort(port);
    
    assertThat(config.getDefaultPort()).isEqualTo(port);
  }

  @Test
  @DisplayName("Should implement equals correctly")
  void shouldImplementEqualsCorrectly() {
    ServiceConfiguration config1 = new ServiceConfiguration();
    config1.setPodLabel("app=test");
    config1.setNamespace("test-ns");
    
    ServiceConfiguration config2 = new ServiceConfiguration();
    config2.setPodLabel("app=test");
    config2.setNamespace("test-ns");
    
    ServiceConfiguration config3 = new ServiceConfiguration();
    config3.setPodLabel("app=different");
    
    assertThat(config1).isEqualTo(config2);
    assertThat(config1).isNotEqualTo(config3);
    assertThat(config1).isNotEqualTo(null);
    assertThat(config1).isEqualTo(config1);
  }

  @Test
  @DisplayName("Should implement hashCode correctly")
  void shouldImplementHashCodeCorrectly() {
    ServiceConfiguration config1 = new ServiceConfiguration();
    config1.setPodLabel("app=test");
    
    ServiceConfiguration config2 = new ServiceConfiguration();
    config2.setPodLabel("app=test");
    
    assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
  }

  @Test
  @DisplayName("Should implement toString correctly")
  void shouldImplementToStringCorrectly() {
    ServiceConfiguration config = new ServiceConfiguration();
    config.setPodLabel("app=test-service");
    
    String toString = config.toString();
    
    assertThat(toString).isNotNull();
    assertThat(toString).contains("ServiceConfiguration");
    assertThat(toString).contains("app=test-service");
  }

  @Test
  @DisplayName("Should handle null values")
  void shouldHandleNullValues() {
    ServiceConfiguration config = new ServiceConfiguration();
    
    config.setPodLabel(null);
    config.setNamespace(null);
    config.setRoute(null);
    config.setSecret(null);
    
    assertThat(config.getPodLabel()).isNull();
    assertThat(config.getNamespace()).isNull();
    assertThat(config.getRoute()).isNull();
    assertThat(config.getSecret()).isNull();
  }

  @Test
  @DisplayName("Should handle empty strings")
  void shouldHandleEmptyStrings() {
    ServiceConfiguration config = new ServiceConfiguration();
    
    config.setPodLabel("");
    config.setNamespace("");
    config.setRoute("");
    config.setSecret("");
    
    assertThat(config.getPodLabel()).isEmpty();
    assertThat(config.getNamespace()).isEmpty();
    assertThat(config.getRoute()).isEmpty();
    assertThat(config.getSecret()).isEmpty();
  }

  @Test
  @DisplayName("Should create with all-args constructor")
  void shouldCreateWithAllArgsConstructor() {
    ServiceConfiguration config = new ServiceConfiguration(
        "app=test",
        "test-secret",
        "test-namespace",
        "test-route",
        true,
        8080
    );
    
    assertThat(config.getPodLabel()).isEqualTo("app=test");
    assertThat(config.getSecret()).isEqualTo("test-secret");
    assertThat(config.getNamespace()).isEqualTo("test-namespace");
    assertThat(config.getRoute()).isEqualTo("test-route");
    assertThat(config.isPortForwarding()).isTrue();
    assertThat(config.getDefaultPort()).isEqualTo(8080);
  }
}
