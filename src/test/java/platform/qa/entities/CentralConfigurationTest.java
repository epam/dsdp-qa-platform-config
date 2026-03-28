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

@DisplayName("CentralConfiguration Entity Tests")
public class CentralConfigurationTest {

  @Test
  @DisplayName("Should create CentralConfiguration with no-args constructor")
  void shouldCreateCentralConfigurationWithNoArgsConstructor() {
    CentralConfiguration config = new CentralConfiguration();
    
    assertThat(config).isNotNull();
    assertThat(config.getJenkins()).isNull();
    assertThat(config.getKibana()).isNull();
    assertThat(config.getKiali()).isNull();
    assertThat(config.getJager()).isNull();
  }

  @Test
  @DisplayName("Should set and get jenkins configuration")
  void shouldSetAndGetJenkinsConfiguration() {
    CentralConfiguration config = new CentralConfiguration();
    ServiceConfiguration jenkins = new ServiceConfiguration();
    
    config.setJenkins(jenkins);
    
    assertThat(config.getJenkins()).isEqualTo(jenkins);
  }

  @Test
  @DisplayName("Should set and get kibana configuration")
  void shouldSetAndGetKibanaConfiguration() {
    CentralConfiguration config = new CentralConfiguration();
    ServiceConfiguration kibana = new ServiceConfiguration();
    
    config.setKibana(kibana);
    
    assertThat(config.getKibana()).isEqualTo(kibana);
  }

  @Test
  @DisplayName("Should implement equals correctly")
  void shouldImplementEqualsCorrectly() {
    ServiceConfiguration jenkins = new ServiceConfiguration();
    
    CentralConfiguration config1 = new CentralConfiguration();
    config1.setJenkins(jenkins);
    
    CentralConfiguration config2 = new CentralConfiguration();
    config2.setJenkins(jenkins);
    
    CentralConfiguration config3 = new CentralConfiguration();
    
    assertThat(config1).isEqualTo(config2);
    assertThat(config1).isNotEqualTo(config3);
    assertThat(config1).isNotEqualTo(null);
    assertThat(config1).isEqualTo(config1);
  }

  @Test
  @DisplayName("Should implement hashCode correctly")
  void shouldImplementHashCodeCorrectly() {
    ServiceConfiguration jenkins = new ServiceConfiguration();
    
    CentralConfiguration config1 = new CentralConfiguration();
    config1.setJenkins(jenkins);
    
    CentralConfiguration config2 = new CentralConfiguration();
    config2.setJenkins(jenkins);
    
    assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
  }

  @Test
  @DisplayName("Should implement toString correctly")
  void shouldImplementToStringCorrectly() {
    CentralConfiguration config = new CentralConfiguration();
    String toString = config.toString();
    
    assertThat(toString).isNotNull();
    assertThat(toString).contains("CentralConfiguration");
  }
}
