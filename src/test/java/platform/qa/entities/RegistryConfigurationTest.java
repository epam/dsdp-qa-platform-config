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

@DisplayName("RegistryConfiguration Tests")
public class RegistryConfigurationTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create RegistryConfiguration with no-args constructor")
    void shouldCreateRegistryConfigurationWithNoArgsConstructor() {
      RegistryConfiguration config = new RegistryConfiguration();
      
      assertThat(config).isNotNull();
      assertThat(config.getDataFactory()).isNull();
      assertThat(config.getRegistryManagement()).isNull();
      assertThat(config.getCeph()).isNull();
      assertThat(config.getOfficerPortal()).isNull();
      assertThat(config.getCitizenPortal()).isNull();
    }

    @Test
    @DisplayName("Should create RegistryConfiguration and set fields via setters")
    void shouldCreateRegistryConfigurationAndSetFieldsViaSetters() {
      // Create test objects
      ServiceConfiguration dataFactory = new ServiceConfiguration();
      ServiceConfiguration registryManagement = new ServiceConfiguration();
      CephBuckets ceph = new CephBuckets();
      
      RegistryConfiguration config = new RegistryConfiguration();
      config.setDataFactory(dataFactory);
      config.setRegistryManagement(registryManagement);
      config.setCeph(ceph);
      
      assertThat(config).isNotNull();
      assertThat(config.getDataFactory()).isEqualTo(dataFactory);
      assertThat(config.getRegistryManagement()).isEqualTo(registryManagement);
      assertThat(config.getCeph()).isEqualTo(ceph);
    }
  }

  @Nested
  @DisplayName("Core Service Configuration Tests")
  class CoreServiceConfigurationTests {

    @Test
    @DisplayName("Should set and get dataFactory")
    void shouldSetAndGetDataFactory() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration dataFactory = new ServiceConfiguration();
      
      config.setDataFactory(dataFactory);
      
      assertThat(config.getDataFactory()).isEqualTo(dataFactory);
    }

    @Test
    @DisplayName("Should set and get registryManagement")
    void shouldSetAndGetRegistryManagement() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration registryManagement = new ServiceConfiguration();
      
      config.setRegistryManagement(registryManagement);
      
      assertThat(config.getRegistryManagement()).isEqualTo(registryManagement);
    }

    @Test
    @DisplayName("Should set and get digitalSignature")
    void shouldSetAndGetDigitalSignature() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration digitalSignature = new ServiceConfiguration();
      
      config.setDigitalSignature(digitalSignature);
      
      assertThat(config.getDigitalSignature()).isEqualTo(digitalSignature);
    }

    @Test
    @DisplayName("Should set and get bpms")
    void shouldSetAndGetBpms() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration bpms = new ServiceConfiguration();
      
      config.setBpms(bpms);
      
      assertThat(config.getBpms()).isEqualTo(bpms);
    }
  }

  @Nested
  @DisplayName("Portal Configuration Tests")
  class PortalConfigurationTests {

    @Test
    @DisplayName("Should set and get officerPortal")
    void shouldSetAndGetOfficerPortal() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration officerPortal = new ServiceConfiguration();
      
      config.setOfficerPortal(officerPortal);
      
      assertThat(config.getOfficerPortal()).isEqualTo(officerPortal);
    }

    @Test
    @DisplayName("Should set and get citizenPortal")
    void shouldSetAndGetCitizenPortal() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration citizenPortal = new ServiceConfiguration();
      
      config.setCitizenPortal(citizenPortal);
      
      assertThat(config.getCitizenPortal()).isEqualTo(citizenPortal);
    }

    @Test
    @DisplayName("Should set and get adminPortal")
    void shouldSetAndGetAdminPortal() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration adminPortal = new ServiceConfiguration();
      
      config.setAdminPortal(adminPortal);
      
      assertThat(config.getAdminPortal()).isEqualTo(adminPortal);
    }
  }

  @Nested
  @DisplayName("Form Management Configuration Tests")
  class FormManagementConfigurationTests {

    @Test
    @DisplayName("Should set and get formManagementProvider")
    void shouldSetAndGetFormManagementProvider() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration formManagementProvider = new ServiceConfiguration();
      
      config.setFormManagementProvider(formManagementProvider);
      
      assertThat(config.getFormManagementProvider()).isEqualTo(formManagementProvider);
    }

    @Test
    @DisplayName("Should set and get formManagementModeler")
    void shouldSetAndGetFormManagementModeler() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration formManagementModeler = new ServiceConfiguration();
      
      config.setFormManagementModeler(formManagementModeler);
      
      assertThat(config.getFormManagementModeler()).isEqualTo(formManagementModeler);
    }

    @Test
    @DisplayName("Should set and get formSchemaProvider")
    void shouldSetAndGetFormSchemaProvider() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration formSchemaProvider = new ServiceConfiguration();
      
      config.setFormSchemaProvider(formSchemaProvider);
      
      assertThat(config.getFormSchemaProvider()).isEqualTo(formSchemaProvider);
    }
  }

  @Nested
  @DisplayName("Database Configuration Tests")
  class DatabaseConfigurationTests {

    @Test
    @DisplayName("Should set and get citusMaster")
    void shouldSetAndGetCitusMaster() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration citusMaster = new ServiceConfiguration();
      
      config.setCitusMaster(citusMaster);
      
      assertThat(config.getCitusMaster()).isEqualTo(citusMaster);
    }

    @Test
    @DisplayName("Should set and get citusReplica")
    void shouldSetAndGetCitusReplica() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration citusReplica = new ServiceConfiguration();
      
      config.setCitusReplica(citusReplica);
      
      assertThat(config.getCitusReplica()).isEqualTo(citusReplica);
    }

    @Test
    @DisplayName("Should set and get redis")
    void shouldSetAndGetRedis() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration redis = new ServiceConfiguration();
      
      config.setRedis(redis);
      
      assertThat(config.getRedis()).isEqualTo(redis);
    }
  }

  @Nested
  @DisplayName("Storage Configuration Tests")
  class StorageConfigurationTests {

    @Test
    @DisplayName("Should set and get ceph")
    void shouldSetAndGetCeph() {
      RegistryConfiguration config = new RegistryConfiguration();
      CephBuckets ceph = new CephBuckets();
      
      config.setCeph(ceph);
      
      assertThat(config.getCeph()).isEqualTo(ceph);
    }

    @Test
    @DisplayName("Should handle null ceph")
    void shouldHandleNullCeph() {
      RegistryConfiguration config = new RegistryConfiguration();
      
      config.setCeph(null);
      
      assertThat(config.getCeph()).isNull();
    }
  }

  @Nested
  @DisplayName("Additional Services Tests")
  class AdditionalServicesTests {

    @Test
    @DisplayName("Should set and get excerpt")
    void shouldSetAndGetExcerpt() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration excerpt = new ServiceConfiguration();
      
      config.setExcerpt(excerpt);
      
      assertThat(config.getExcerpt()).isEqualTo(excerpt);
    }

    @Test
    @DisplayName("Should set and get notificationService")
    void shouldSetAndGetNotificationService() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration notificationService = new ServiceConfiguration();
      
      config.setNotificationService(notificationService);
      
      assertThat(config.getNotificationService()).isEqualTo(notificationService);
    }

    @Test
    @DisplayName("Should set and get wiremock")
    void shouldSetAndGetWiremock() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration wiremock = new ServiceConfiguration();
      
      config.setWiremock(wiremock);
      
      assertThat(config.getWiremock()).isEqualTo(wiremock);
    }

    @Test
    @DisplayName("Should set and get jenkins")
    void shouldSetAndGetJenkins() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration jenkins = new ServiceConfiguration();
      
      config.setJenkins(jenkins);
      
      assertThat(config.getJenkins()).isEqualTo(jenkins);
    }
  }

  @Nested
  @DisplayName("Lombok Generated Methods Tests")
  class LombokGeneratedMethodsTests {

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
      ServiceConfiguration service1 = new ServiceConfiguration();
      service1.setPodLabel("service1");
      
      ServiceConfiguration service2 = new ServiceConfiguration();
      service2.setPodLabel("service2");
      
      CephBuckets ceph1 = new CephBuckets();
      ceph1.setSignatureBucket("bucket1");
      
      CephBuckets ceph2 = new CephBuckets();
      ceph2.setSignatureBucket("bucket2");
      
      RegistryConfiguration config1 = new RegistryConfiguration();
      config1.setDataFactory(service1);
      config1.setCeph(ceph1);
      
      RegistryConfiguration config2 = new RegistryConfiguration();
      config2.setDataFactory(service1);
      config2.setCeph(ceph1);
      
      RegistryConfiguration config3 = new RegistryConfiguration();
      config3.setDataFactory(service2);
      config3.setCeph(ceph2);
      
      assertThat(config1).isEqualTo(config2);
      assertThat(config1).isNotEqualTo(config3);
      assertThat(config1).isEqualTo(config1); // reflexive
      assertThat(config1).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should implement hashCode correctly")
    void shouldImplementHashCodeCorrectly() {
      ServiceConfiguration service = new ServiceConfiguration();
      service.setPodLabel("test-service");
      CephBuckets ceph = new CephBuckets();
      ceph.setSignatureBucket("test-bucket");
      
      RegistryConfiguration config1 = new RegistryConfiguration();
      config1.setDataFactory(service);
      config1.setCeph(ceph);
      
      RegistryConfiguration config2 = new RegistryConfiguration();
      config2.setDataFactory(service);
      config2.setCeph(ceph);
      
      assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString correctly")
    void shouldImplementToStringCorrectly() {
      RegistryConfiguration config = new RegistryConfiguration();
      ServiceConfiguration dataFactory = new ServiceConfiguration();
      config.setDataFactory(dataFactory);
      
      String toString = config.toString();
      
      assertThat(toString).contains("RegistryConfiguration");
      assertThat(toString).contains("dataFactory=");
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle multiple service configurations")
    void shouldHandleMultipleServiceConfigurations() {
      RegistryConfiguration config = new RegistryConfiguration();
      
      ServiceConfiguration dataFactory = new ServiceConfiguration();
      ServiceConfiguration bpms = new ServiceConfiguration();
      ServiceConfiguration officerPortal = new ServiceConfiguration();
      CephBuckets ceph = new CephBuckets();
      
      config.setDataFactory(dataFactory);
      config.setBpms(bpms);
      config.setOfficerPortal(officerPortal);
      config.setCeph(ceph);
      
      assertThat(config.getDataFactory()).isEqualTo(dataFactory);
      assertThat(config.getBpms()).isEqualTo(bpms);
      assertThat(config.getOfficerPortal()).isEqualTo(officerPortal);
      assertThat(config.getCeph()).isEqualTo(ceph);
    }

    @Test
    @DisplayName("Should handle mixed null and non-null configurations")
    void shouldHandleMixedNullAndNonNullConfigurations() {
      RegistryConfiguration config = new RegistryConfiguration();
      
      ServiceConfiguration dataFactory = new ServiceConfiguration();
      config.setDataFactory(dataFactory);
      config.setBpms(null);
      config.setOfficerPortal(null);
      config.setCeph(null);
      
      assertThat(config.getDataFactory()).isEqualTo(dataFactory);
      assertThat(config.getBpms()).isNull();
      assertThat(config.getOfficerPortal()).isNull();
      assertThat(config.getCeph()).isNull();
    }

    @Test
    @DisplayName("Should support independent instances")
    void shouldSupportIndependentInstances() {
      RegistryConfiguration config1 = new RegistryConfiguration();
      RegistryConfiguration config2 = new RegistryConfiguration();
      
      ServiceConfiguration service1 = new ServiceConfiguration();
      ServiceConfiguration service2 = new ServiceConfiguration();
      
      config1.setDataFactory(service1);
      config2.setDataFactory(service2);
      
      assertThat(config1.getDataFactory()).isEqualTo(service1);
      assertThat(config2.getDataFactory()).isEqualTo(service2);
      assertThat(config1).isNotSameAs(config2);
    }
  }
}
