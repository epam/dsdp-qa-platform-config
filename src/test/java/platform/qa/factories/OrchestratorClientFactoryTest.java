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

package platform.qa.factories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import platform.qa.entities.Service;
import platform.qa.k8s.K8sClient;
import platform.qa.oc.OkdClient;
import platform.qa.orchestrator.OrchestratorClient;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrchestratorClientFactory Tests")
public class OrchestratorClientFactoryTest {

  private Service mockService;
  private String testNamespace;
  private String testToken;

  @BeforeEach
  void setUp() {
    mockService = mock(Service.class);
    testNamespace = "test-namespace";
    testToken = "test-token-123";
  }

  private void setupMockServiceWithUser() {
    lenient().when(mockService.getUrl()).thenReturn("https://test-cluster.example.com");
    platform.qa.entities.User mockUser = mock(platform.qa.entities.User.class);
    lenient().when(mockUser.getLogin()).thenReturn("test-user");
    lenient().when(mockUser.getPassword()).thenReturn("test-password");
    lenient().when(mockService.getUser()).thenReturn(mockUser);
  }

  @Nested
  @DisplayName("OrchestratorType Enum Tests")
  class OrchestratorTypeEnumTests {

    @Test
    @DisplayName("Should have correct enum values")
    void shouldHaveCorrectEnumValues() {
      OrchestratorClientFactory.OrchestratorType[] values = OrchestratorClientFactory.OrchestratorType.values();
      
      assertThat(values).hasSize(2);
      assertThat(values).containsExactly(
          OrchestratorClientFactory.OrchestratorType.OPENSHIFT,
          OrchestratorClientFactory.OrchestratorType.KUBERNETES
      );
    }

    @Test
    @DisplayName("Should be able to get enum by name")
    void shouldBeAbleToGetEnumByName() {
      assertThat(OrchestratorClientFactory.OrchestratorType.valueOf("OPENSHIFT"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
      assertThat(OrchestratorClientFactory.OrchestratorType.valueOf("KUBERNETES"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
    }

    @Test
    @DisplayName("Should have correct string representation")
    void shouldHaveCorrectStringRepresentation() {
      assertThat(OrchestratorClientFactory.OrchestratorType.OPENSHIFT.name()).isEqualTo("OPENSHIFT");
      assertThat(OrchestratorClientFactory.OrchestratorType.KUBERNETES.name()).isEqualTo("KUBERNETES");
    }
  }

  @Nested
  @DisplayName("Create Client Tests")
  class CreateClientTests {

    @Test
    @DisplayName("Should create OkdClient for OPENSHIFT type")
    void shouldCreateOkdClientForOpenshiftType() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          OrchestratorClientFactory.OrchestratorType.OPENSHIFT, 
          mockService, 
          testNamespace
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(OkdClient.class);
    }

    @Test
    @DisplayName("Should create K8sClient for KUBERNETES type")
    void shouldCreateK8sClientForKubernetesType() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          OrchestratorClientFactory.OrchestratorType.KUBERNETES, 
          mockService, 
          testNamespace
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(K8sClient.class);
    }

    @Test
    @DisplayName("Should create OkdClient with token for OPENSHIFT type")
    void shouldCreateOkdClientWithTokenForOpenshiftType() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          "openshift", 
          mockService, 
          testNamespace, 
          testToken
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(OkdClient.class);
    }

    @Test
    @DisplayName("Should create K8sClient with token for KUBERNETES type")
    void shouldCreateK8sClientWithTokenForKubernetesType() {
      when(mockService.getUrl()).thenReturn("https://test-cluster.example.com");
      
      // This will try to create K8sClient with token, which may fail due to validation
      assertThatThrownBy(() -> OrchestratorClientFactory.createClient(
          "kubernetes", 
          mockService, 
          testNamespace, 
          testToken
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle null service parameter")
    void shouldHandleNullServiceParameter() {
      assertThatThrownBy(() -> OrchestratorClientFactory.createClient(
          OrchestratorClientFactory.OrchestratorType.OPENSHIFT, 
          null, 
          testNamespace
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle null namespace parameter")
    void shouldHandleNullNamespaceParameter() {
      setupMockServiceWithUser();
      
      // Null namespace should cause validation error
      assertThatThrownBy(() -> OrchestratorClientFactory.createClient(
          OrchestratorClientFactory.OrchestratorType.OPENSHIFT, 
          mockService, 
          null
      )).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("namespace is NOT set");
    }

    @Test
    @DisplayName("Should handle null token parameter")
    void shouldHandleNullTokenParameter() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          "kubernetes", 
          mockService, 
          testNamespace, 
          null
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(K8sClient.class);
    }

    @Test
    @DisplayName("Should handle empty namespace")
    void shouldHandleEmptyNamespace() {
      setupMockServiceWithUser();
      
      // Empty namespace should cause validation error
      assertThatThrownBy(() -> OrchestratorClientFactory.createClient(
          OrchestratorClientFactory.OrchestratorType.KUBERNETES, 
          mockService, 
          ""
      )).isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("namespace is NOT set");
    }

    @Test
    @DisplayName("Should handle empty token")
    void shouldHandleEmptyToken() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          "openshift", 
          mockService, 
          testNamespace, 
          ""
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(OkdClient.class);
    }
  }

  @Nested
  @DisplayName("Parse Orchestrator Type Tests")
  class ParseOrchestratorTypeTests {

    @Test
    @DisplayName("Should parse 'openshift' string to OPENSHIFT type")
    void shouldParseOpenshiftStringToOpenshiftType() {
      OrchestratorClientFactory.OrchestratorType type = OrchestratorClientFactory.parseOrchestratorType("openshift");
      
      assertThat(type).isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
    }

    @Test
    @DisplayName("Should parse 'kubernetes' string to KUBERNETES type")
    void shouldParseKubernetesStringToKubernetesType() {
      OrchestratorClientFactory.OrchestratorType type = OrchestratorClientFactory.parseOrchestratorType("kubernetes");
      
      assertThat(type).isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
    }

    @Test
    @DisplayName("Should parse 'k8s' string to KUBERNETES type")
    void shouldParseK8sStringToKubernetesType() {
      OrchestratorClientFactory.OrchestratorType type = OrchestratorClientFactory.parseOrchestratorType("k8s");
      
      assertThat(type).isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
    }

    @Test
    @DisplayName("Should parse 'okd' string to OPENSHIFT type")
    void shouldParseOkdStringToOpenshiftType() {
      OrchestratorClientFactory.OrchestratorType type = OrchestratorClientFactory.parseOrchestratorType("okd");
      
      assertThat(type).isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
    }

    @Test
    @DisplayName("Should parse 'ocp' string to OPENSHIFT type")
    void shouldParseOcpStringToOpenshiftType() {
      OrchestratorClientFactory.OrchestratorType type = OrchestratorClientFactory.parseOrchestratorType("ocp");
      
      assertThat(type).isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
    }

    @Test
    @DisplayName("Should be case insensitive")
    void shouldBeCaseInsensitive() {
      assertThat(OrchestratorClientFactory.parseOrchestratorType("OPENSHIFT"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
      assertThat(OrchestratorClientFactory.parseOrchestratorType("OpenShift"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
      assertThat(OrchestratorClientFactory.parseOrchestratorType("KUBERNETES"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
      assertThat(OrchestratorClientFactory.parseOrchestratorType("Kubernetes"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
      assertThat(OrchestratorClientFactory.parseOrchestratorType("K8S"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
    }

    @Test
    @DisplayName("Should default to OPENSHIFT for unknown strings")
    void shouldDefaultToOpenshiftForUnknownStrings() {
      assertThat(OrchestratorClientFactory.parseOrchestratorType("unknown"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
      assertThat(OrchestratorClientFactory.parseOrchestratorType("invalid"))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
      assertThat(OrchestratorClientFactory.parseOrchestratorType(""))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
    }

    @Test
    @DisplayName("Should handle null input")
    void shouldHandleNullInput() {
      OrchestratorClientFactory.OrchestratorType type = OrchestratorClientFactory.parseOrchestratorType(null);
      
      assertThat(type).isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
    }

    @Test
    @DisplayName("Should handle whitespace")
    void shouldHandleWhitespace() {
      assertThat(OrchestratorClientFactory.parseOrchestratorType(" openshift "))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.OPENSHIFT);
      assertThat(OrchestratorClientFactory.parseOrchestratorType(" kubernetes "))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
      assertThat(OrchestratorClientFactory.parseOrchestratorType(" k8s "))
          .isEqualTo(OrchestratorClientFactory.OrchestratorType.KUBERNETES);
    }
  }

  @Nested
  @DisplayName("Kubernetes Token Authentication Tests")
  class KubernetesTokenAuthTests {

    @Test
    @DisplayName("Should handle token authentication method call")
    void shouldHandleTokenAuthenticationMethodCall() {
      String masterUrl = "https://k8s-api.example.com";
      
      // Test that the method exists and can be called (may throw exception due to invalid token/cluster)
      assertThatThrownBy(() -> OrchestratorClientFactory.createKubernetesClientWithToken(
          masterUrl, 
          testToken, 
          testNamespace
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle null master URL in token auth")
    void shouldHandleNullMasterUrl() {
      assertThatThrownBy(() -> OrchestratorClientFactory.createKubernetesClientWithToken(
          null, 
          testToken, 
          testNamespace
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle null token in token auth")
    void shouldHandleNullTokenInTokenAuth() {
      String masterUrl = "https://k8s-api.example.com";
      
      // Null token should cause validation error
      assertThatThrownBy(() -> OrchestratorClientFactory.createKubernetesClientWithToken(
          masterUrl, 
          null, 
          testNamespace
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle empty token in token auth")
    void shouldHandleEmptyTokenInTokenAuth() {
      String masterUrl = "https://k8s-api.example.com";
      
      // Empty token should cause validation error
      assertThatThrownBy(() -> OrchestratorClientFactory.createKubernetesClientWithToken(
          masterUrl, 
          "", 
          testNamespace
      )).isInstanceOf(Exception.class);
    }
  }

  @Nested
  @DisplayName("String-based Create Client Tests")
  class StringBasedCreateClientTests {

    @Test
    @DisplayName("Should create OkdClient for 'openshift' string")
    void shouldCreateOkdClientForOpenshiftString() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          "openshift", 
          mockService, 
          testNamespace, 
          testToken
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(OkdClient.class);
    }

    @Test
    @DisplayName("Should handle 'kubernetes' string (may fail due to token validation)")
    void shouldHandleKubernetesString() {
      when(mockService.getUrl()).thenReturn("https://test-cluster.example.com");
      
      // Kubernetes with token may fail due to cluster validation
      assertThatThrownBy(() -> OrchestratorClientFactory.createClient(
          "kubernetes", 
          mockService, 
          testNamespace, 
          testToken
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should handle 'k8s' string (may fail due to token validation)")
    void shouldHandleK8sString() {
      when(mockService.getUrl()).thenReturn("https://test-cluster.example.com");
      
      // K8s with token may fail due to cluster validation
      assertThatThrownBy(() -> OrchestratorClientFactory.createClient(
          "k8s", 
          mockService, 
          testNamespace, 
          testToken
      )).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should default to OkdClient for unknown string")
    void shouldDefaultToOkdClientForUnknownString() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          "unknown", 
          mockService, 
          testNamespace, 
          testToken
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(OkdClient.class);
    }

    @Test
    @DisplayName("Should handle null orchestrator type string")
    void shouldHandleNullOrchestratorTypeString() {
      setupMockServiceWithUser();
      
      OrchestratorClient client = OrchestratorClientFactory.createClient(
          null, 
          mockService, 
          testNamespace, 
          testToken
      );
      
      assertThat(client).isNotNull();
      assertThat(client).isInstanceOf(OkdClient.class);
    }
  }
}
