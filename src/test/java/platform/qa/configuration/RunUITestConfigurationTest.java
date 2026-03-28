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

package platform.qa.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import platform.qa.utils.ConfigurationUtils;

@DisplayName("RunUITestConfiguration Tests")
public class RunUITestConfigurationTest {

  private MockedStatic<ConfigurationUtils> mockedConfigurationUtils;

  @BeforeEach
  void setUp() {
    // Clear system properties before each test
    clearSystemProperties();
    
    // Mock ConfigurationUtils to return empty properties
    mockedConfigurationUtils = mockStatic(ConfigurationUtils.class);
    Properties emptyProperties = new Properties();
    mockedConfigurationUtils.when(() -> ConfigurationUtils.uploadPropertiesConfiguration(anyString()))
        .thenReturn(emptyProperties);
  }

  @AfterEach
  void tearDown() {
    // Clear system properties after each test
    clearSystemProperties();
    
    // Close the static mock
    if (mockedConfigurationUtils != null) {
      mockedConfigurationUtils.close();
    }
  }

  private void clearSystemProperties() {
    System.clearProperty("isRemoteMoonRun");
    System.clearProperty("isDiiaTheme");
    System.clearProperty("isProxyEnabled");
    System.clearProperty("browser");
    System.clearProperty("isMobileViewEnabled");
    System.clearProperty("deviceName");
  }

  @Nested
  @DisplayName("Singleton Pattern Tests")
  class SingletonPatternTests {

    @Test
    @DisplayName("Should return same instance on multiple calls")
    void shouldReturnSameInstanceOnMultipleCalls() {
      RunUITestConfiguration instance1 = RunUITestConfiguration.getInstance();
      RunUITestConfiguration instance2 = RunUITestConfiguration.getInstance();
      
      assertThat(instance1).isNotNull();
      assertThat(instance2).isNotNull();
      assertThat(instance1).isSameAs(instance2);
    }

    @Test
    @DisplayName("Should create instance on first call")
    void shouldCreateInstanceOnFirstCall() {
      RunUITestConfiguration instance = RunUITestConfiguration.getInstance();
      
      assertThat(instance).isNotNull();
    }
  }

  @Nested
  @DisplayName("Remote Run Configuration Tests")
  class RemoteRunConfigurationTests {

    @Test
    @DisplayName("Should return false by default for remote run")
    void shouldReturnFalseByDefaultForRemoteRun() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isRemoteRunEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return true when system property is set to true")
    void shouldReturnTrueWhenSystemPropertyIsSetToTrue() {
      System.setProperty("isRemoteMoonRun", "true");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isRemoteRunEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return false when system property is set to false")
    void shouldReturnFalseWhenSystemPropertyIsSetToFalse() {
      System.setProperty("isRemoteMoonRun", "false");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isRemoteRunEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should handle invalid system property values")
    void shouldHandleInvalidSystemPropertyValues() {
      System.setProperty("isRemoteMoonRun", "invalid");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isRemoteRunEnabled()).isFalse();
    }
  }

  @Nested
  @DisplayName("Diia Theme Configuration Tests")
  class DiiaThemeConfigurationTests {

    @Test
    @DisplayName("Should return false by default for Diia theme")
    void shouldReturnFalseByDefaultForDiiaTheme() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isDiiaThemeEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return true when system property is set to true")
    void shouldReturnTrueWhenSystemPropertyIsSetToTrue() {
      System.setProperty("isDiiaTheme", "true");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isDiiaThemeEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return false when system property is set to false")
    void shouldReturnFalseWhenSystemPropertyIsSetToFalse() {
      System.setProperty("isDiiaTheme", "false");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isDiiaThemeEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should handle case insensitive values")
    void shouldHandleCaseInsensitiveValues() {
      System.setProperty("isDiiaTheme", "TRUE");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isDiiaThemeEnabled()).isTrue();
    }
  }

  @Nested
  @DisplayName("Proxy Configuration Tests")
  class ProxyConfigurationTests {

    @Test
    @DisplayName("Should return false by default for proxy")
    void shouldReturnFalseByDefaultForProxy() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isProxyEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return true when system property is set to true")
    void shouldReturnTrueWhenSystemPropertyIsSetToTrue() {
      System.setProperty("isProxyEnabled", "true");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isProxyEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return false when system property is set to false")
    void shouldReturnFalseWhenSystemPropertyIsSetToFalse() {
      System.setProperty("isProxyEnabled", "false");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isProxyEnabled()).isFalse();
    }
  }

  @Nested
  @DisplayName("Browser Configuration Tests")
  class BrowserConfigurationTests {

    @Test
    @DisplayName("Should return chrome by default")
    void shouldReturnChromeByDefault() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.getBrowser()).isEqualTo("chrome");
    }

    @Test
    @DisplayName("Should return system property value when set")
    void shouldReturnSystemPropertyValueWhenSet() {
      System.setProperty("browser", "firefox");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.getBrowser()).isEqualTo("firefox");
    }

    @Test
    @DisplayName("Should handle different browser values")
    void shouldHandleDifferentBrowserValues() {
      String[] browsers = {"chrome", "firefox", "safari", "edge"};
      
      for (String browser : browsers) {
        System.setProperty("browser", browser);
        
        RunUITestConfiguration config = RunUITestConfiguration.getInstance();
        
        assertThat(config.getBrowser()).isEqualTo(browser);
        
        System.clearProperty("browser");
      }
    }

    @Test
    @DisplayName("Should handle empty browser value")
    void shouldHandleEmptyBrowserValue() {
      System.setProperty("browser", "");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.getBrowser()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Mobile View Configuration Tests")
  class MobileViewConfigurationTests {

    @Test
    @DisplayName("Should return false by default for mobile view")
    void shouldReturnFalseByDefaultForMobileView() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isMobileViewEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should return true when system property is set to true")
    void shouldReturnTrueWhenSystemPropertyIsSetToTrue() {
      System.setProperty("isMobileViewEnabled", "true");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isMobileViewEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should return false when system property is set to false")
    void shouldReturnFalseWhenSystemPropertyIsSetToFalse() {
      System.setProperty("isMobileViewEnabled", "false");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isMobileViewEnabled()).isFalse();
    }
  }

  @Nested
  @DisplayName("Device Name Configuration Tests")
  class DeviceNameConfigurationTests {

    @Test
    @DisplayName("Should return iPhone SE by default")
    void shouldReturnIPhoneSEByDefault() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.getDeviceName()).isEqualTo("iPhone SE");
    }

    @Test
    @DisplayName("Should return system property value when set")
    void shouldReturnSystemPropertyValueWhenSet() {
      System.setProperty("deviceName", "iPhone 12");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.getDeviceName()).isEqualTo("iPhone 12");
    }

    @Test
    @DisplayName("Should handle different device names")
    void shouldHandleDifferentDeviceNames() {
      String[] devices = {"iPhone SE", "iPhone 12", "Samsung Galaxy S21", "iPad"};
      
      for (String device : devices) {
        System.setProperty("deviceName", device);
        
        RunUITestConfiguration config = RunUITestConfiguration.getInstance();
        
        assertThat(config.getDeviceName()).isEqualTo(device);
        
        System.clearProperty("deviceName");
      }
    }

    @Test
    @DisplayName("Should handle empty device name")
    void shouldHandleEmptyDeviceName() {
      System.setProperty("deviceName", "");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.getDeviceName()).isEmpty();
    }
  }

  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should handle multiple properties set simultaneously")
    void shouldHandleMultiplePropertiesSetSimultaneously() {
      System.setProperty("isRemoteMoonRun", "true");
      System.setProperty("isDiiaTheme", "true");
      System.setProperty("isProxyEnabled", "false");
      System.setProperty("browser", "firefox");
      System.setProperty("isMobileViewEnabled", "true");
      System.setProperty("deviceName", "iPhone 12");
      
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isRemoteRunEnabled()).isTrue();
      assertThat(config.isDiiaThemeEnabled()).isTrue();
      assertThat(config.isProxyEnabled()).isFalse();
      assertThat(config.getBrowser()).isEqualTo("firefox");
      assertThat(config.isMobileViewEnabled()).isTrue();
      assertThat(config.getDeviceName()).isEqualTo("iPhone 12");
    }

    @Test
    @DisplayName("Should work correctly when no properties are set")
    void shouldWorkCorrectlyWhenNoPropertiesAreSet() {
      RunUITestConfiguration config = RunUITestConfiguration.getInstance();
      
      assertThat(config.isRemoteRunEnabled()).isFalse();
      assertThat(config.isDiiaThemeEnabled()).isFalse();
      assertThat(config.isProxyEnabled()).isFalse();
      assertThat(config.getBrowser()).isEqualTo("chrome");
      assertThat(config.isMobileViewEnabled()).isFalse();
      assertThat(config.getDeviceName()).isEqualTo("iPhone SE");
    }
  }
}
