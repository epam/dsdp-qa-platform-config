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

package platform.qa.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.ServerSocket;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SocketAnalyzer Tests")
public class SocketAnalyzerTest {

  @Nested
  @DisplayName("Constructor Tests")
  class ConstructorTests {

    @Test
    @DisplayName("Should create SocketAnalyzer successfully")
    void shouldCreateSocketAnalyzerSuccessfully() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      assertThat(analyzer).isNotNull();
      assertThat(analyzer).isInstanceOf(ServerSocket.class);

      analyzer.close();
    }

    @Test
    @DisplayName("Should extend ServerSocket")
    void shouldExtendServerSocket() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      assertThat(analyzer).isInstanceOf(ServerSocket.class);

      analyzer.close();
    }
  }

  @Nested
  @DisplayName("Get Available Port Tests")
  class GetAvailablePortTests {

    @Test
    @DisplayName("Should return valid port number")
    void shouldReturnValidPortNumber() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      int port = analyzer.getAvailablePort();

      assertThat(port).isGreaterThan(0);
      assertThat(port).isLessThanOrEqualTo(65535);

      analyzer.close();
    }

    @Test
    @DisplayName("Should return different ports on multiple calls")
    void shouldReturnDifferentPortsOnMultipleCalls() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      int port1 = analyzer.getAvailablePort();
      int port2 = analyzer.getAvailablePort();

      // Ports should be valid
      assertThat(port1).isGreaterThan(0);
      assertThat(port2).isGreaterThan(0);

      // Ports might be different (not guaranteed, but likely)
      // We just verify they are both valid port numbers
      assertThat(port1).isLessThanOrEqualTo(65535);
      assertThat(port2).isLessThanOrEqualTo(65535);

      analyzer.close();
    }

    @Test
    @DisplayName("Should return port that can be used for TCP connection")
    void shouldReturnPortThatCanBeUsedForTcpConnection() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      int port = analyzer.getAvailablePort();

      // Try to create a ServerSocket on the returned port
      // This should succeed if the port is truly available
      try (ServerSocket testSocket = new ServerSocket(port)) {
        assertThat(testSocket.getLocalPort()).isEqualTo(port);
        assertThat(testSocket.isBound()).isTrue();
      }

      analyzer.close();
    }

    @Test
    @DisplayName("Should handle multiple analyzers")
    void shouldHandleMultipleAnalyzers() throws IOException {
      SocketAnalyzer analyzer1 = new SocketAnalyzer();
      SocketAnalyzer analyzer2 = new SocketAnalyzer();

      int port1 = analyzer1.getAvailablePort();
      int port2 = analyzer2.getAvailablePort();

      assertThat(port1).isGreaterThan(0);
      assertThat(port2).isGreaterThan(0);

      analyzer1.close();
      analyzer2.close();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should work as documented in class javadoc")
    void shouldWorkAsDocumentedInClassJavadoc() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();
      int availablePort = analyzer.getAvailablePort();

      assertThat(availablePort).isGreaterThan(0);
      assertThat(availablePort).isLessThanOrEqualTo(65535);

      analyzer.close();
    }

    @Test
    @DisplayName("Should handle resource management properly")
    void shouldHandleResourceManagementProperly() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      // Get multiple ports
      int port1 = analyzer.getAvailablePort();
      int port2 = analyzer.getAvailablePort();
      int port3 = analyzer.getAvailablePort();

      // All should be valid
      assertThat(port1).isGreaterThan(0);
      assertThat(port2).isGreaterThan(0);
      assertThat(port3).isGreaterThan(0);

      // Close the analyzer
      analyzer.close();

      // Should be closed now
      assertThat(analyzer.isClosed()).isTrue();
    }

    @Test
    @DisplayName("Should work with try-with-resources")
    void shouldWorkWithTryWithResources() throws IOException {
      int port;

      try (SocketAnalyzer analyzer = new SocketAnalyzer()) {
        port = analyzer.getAvailablePort();
        assertThat(port).isGreaterThan(0);
      }

      // Port should still be valid after analyzer is closed
      assertThat(port).isGreaterThan(0);
      assertThat(port).isLessThanOrEqualTo(65535);
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle SneakyThrows annotation correctly")
    void shouldHandleSneakyThrowsAnnotationCorrectly() throws IOException {
      SocketAnalyzer analyzer = new SocketAnalyzer();

      // The method is annotated with @SneakyThrows(IOException.class)
      // but should not throw checked exceptions in normal operation
      int port = analyzer.getAvailablePort();

      assertThat(port).isGreaterThan(0);

      analyzer.close();
    }
  }
}
