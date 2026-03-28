package platform.qa.configuration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.fabric8.openshift.api.model.operatorhub.v1alpha1.CatalogSource;
import java.util.List;
import org.junit.jupiter.api.*;
import org.objenesis.ObjenesisStd;
import platform.qa.entities.*;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.orchestrator.OrchestratorClient;
import platform.qa.providers.impl.PlatformUserProvider;

class CentralConfigAdditionalTest {

  private ServiceConfiguration sc(String name) {
    ServiceConfiguration c = new ServiceConfiguration();
    c.setRoute(name);
    c.setNamespace("ns");
    c.setPodLabel("app=" + name);
    c.setSecret("secret-" + name);
    return c;
  }

  private CentralConfiguration centConf() {
    CentralConfiguration cc = new CentralConfiguration();
    cc.setCeph(sc("ceph"));
    cc.setKibana(sc("kibana"));
    cc.setKiali(sc("kiali"));
    cc.setJager(sc("jager"));
    cc.setDefaultGrafana(sc("graf"));
    cc.setCustomGrafana(sc("cgraf"));
    cc.setWiremock(sc("wire"));
    cc.setKeycloak(sc("keycloak"));
    cc.setControlPlane(sc("cp"));
    cc.setNexus(sc("nx"));
    cc.setEmail(sc("mail"));
    cc.setVault(sc("vault"));
    return cc;
  }

  private CentralConfig prepare(ConfigSetup setup) {
    CentralConfig cc = newInstanceWithoutConstructor();

    setPrivate(cc, "configuration", setup.configuration.getCentralConfiguration());
    setPrivate(cc, "ocService", setup.ocService);
    setPrivate(cc, "orchestratorClient", setup.orchestratorClient);

    // отключаем загрузку реального keycloak
    setPrivate(cc, "keycloakClient", mock(KeycloakClient.class));

    // НИ В КОЕМ СЛУЧАЕ не мокать!!!
    PlatformUserProvider safeProvider = new ObjenesisStd().newInstance(PlatformUserProvider.class);

    setPrivate(cc, "platformUserProvider", safeProvider);

    return spy(cc);
  }

  private void setPrivate(Object target, String fieldName, Object value) {
    try {
      var f = CentralConfig.class.getDeclaredField(fieldName);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private CentralConfig newInstanceWithoutConstructor() {
    return new ObjenesisStd().newInstance(CentralConfig.class);
  }

  private ConfigSetup configSetup(boolean orchestrator) {
    Configuration cfg = new Configuration();
    cfg.setCentralConfiguration(centConf());

    Service oc = mock(Service.class);
    when(oc.getUser()).thenReturn(new User("u", "p"));

    OrchestratorClient orch = orchestrator ? mock(OrchestratorClient.class) : null;
    if (orch != null) when(orch.getType()).thenReturn("openshift");

    return new ConfigSetup(cfg, oc, orch);
  }

  @Test
  void cephFallback() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found")).when(cc).getService(any());
    var result = cc.getCeph();

    assertThat(result.getUrl()).isEqualTo("http://localhost:9999/");
  }

  // ============ TESTS ============

  @Test
  void wiremockRewrite() {
    var set = configSetup(false);
    var cc = prepare(set);

    doReturn(new Service("https://lol.com/x/")).when(cc).getService(any());
    var result = cc.getWiremock();

    assertThat(result.getUrl()).isEqualTo("lol.com/x");
  }

  @Test
  void keycloakRewriteK8s() {
    var set = configSetup(true);
    when(set.orchestratorClient.getType()).thenReturn("kubernetes");

    var cc = prepare(set);

    doReturn(new Service("http://key:8080")).when(cc).getServiceWithUser(any());
    var result = cc.getKeycloak();

    assertThat(result.getUrl()).isEqualTo("http://key:8080/auth/");
  }

  @Test
  void vaultToken() {
    var set = configSetup(true);
    var cc = prepare(set);

    when(set.orchestratorClient.getTokenVault("secret-vault")).thenReturn("TOKEN123");
    set.configuration.getCentralConfiguration().getVault().setSecret("secret-vault");

    doReturn(new Service("http://vault")).when(cc).getService(any());

    Service s = cc.getVaultService();
    assertThat(s.getToken()).isEqualTo("TOKEN123");
  }

  @Test
  void cacheCeph() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service s = new Service("http://one");
    doReturn(s).when(cc).getService(any());

    assertThat(cc.getCeph()).isSameAs(cc.getCeph());
  }

  @Test
  void clusterSourcesOrchestrator() {
    var set = configSetup(true);
    var cc = prepare(set);

    List<CatalogSource> expected = List.of(mock(CatalogSource.class));
    when(set.orchestratorClient.getClusterSources()).thenReturn(expected);

    assertThat(cc.getClusterSources()).isEqualTo(expected);
  }

  // =============================
  // TESTS FOR getCustomGrafana()
  // =============================
  @Test
  @DisplayName("CustomGrafana: returns cached instance when already initialized")
  void testCustomGrafanaCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://cached");
    setPrivate(cc, "customGrafana", cached);

    Service result = cc.getCustomGrafana();

    assertThat(result).isSameAs(cached);
  }

  @Test
  @DisplayName("CustomGrafana: successful call returns normal service")
  void testCustomGrafanaSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://graf");
    doReturn(expected).when(cc).getService(any());

    Service result = cc.getCustomGrafana();

    assertThat(result.getUrl()).isEqualTo("http://graf");
  }

  @Test
  @DisplayName(
      "CustomGrafana: dummy service returned when 'No pods found with label' exception thrown")
  void testCustomGrafanaDummyWhenPodsMissing() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found with label abc")).when(cc).getService(any());

    Service result = cc.getCustomGrafana();

    assertThat(result.getUrl()).isEqualTo("http://localhost:9999/");
  }

  @Test
  @DisplayName("CustomGrafana: rethrows exception when message does not contain expected text")
  void testCustomGrafanaRethrowWrongMessage() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("some random shit")).when(cc).getService(any());

    assertThatThrownBy(cc::getCustomGrafana)
        .isInstanceOf(RuntimeException.class)
        .hasMessage("some random shit");
  }

  @Test
  @DisplayName("CustomGrafana: rethrows exception when message is null")
  void testCustomGrafanaRethrowNullMessage() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException((String) null)).when(cc).getService(any());

    assertThatThrownBy(cc::getCustomGrafana).isInstanceOf(RuntimeException.class).hasMessage(null);
  }

  @Test
  @DisplayName("Ceph: returns cached instance")
  void testCephCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://cached");
    setPrivate(cc, "ceph", cached);

    assertThat(cc.getCeph()).isSameAs(cached);
  }

  @Test
  @DisplayName("Ceph: success path")
  void testCephSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://ceph");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getCeph().getUrl()).isEqualTo("http://ceph");
  }

  @Test
  @DisplayName("Ceph: fallback to dummy on 'No pods found'")
  void testCephDummy() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found")).when(cc).getService(any());

    assertThat(cc.getCeph().getUrl()).isEqualTo("http://localhost:9999/");
  }

  @Test
  @DisplayName("Ceph: rethrow unexpected exception")
  void testCephRethrow() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("boom")).when(cc).getService(any());

    assertThatThrownBy(cc::getCeph).isInstanceOf(RuntimeException.class).hasMessage("boom");
  }

  @Test
  @DisplayName("Kibana: cached instance")
  void testKibanaCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://kb");
    setPrivate(cc, "kibana", cached);

    assertThat(cc.getKibana()).isSameAs(cached);
  }

  @Test
  @DisplayName("Kibana: success")
  void testKibanaSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://kb");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getKibana().getUrl()).isEqualTo("http://kb");
  }

  @Test
  @DisplayName("Kibana: dummy")
  void testKibanaDummy() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found with label kibana")).when(cc).getService(any());

    assertThat(cc.getKibana().getUrl()).isEqualTo("http://localhost:9999/");
  }

  @Test
  @DisplayName("Kibana: rethrow")
  void testKibanaRethrow() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("x")).when(cc).getService(any());

    assertThatThrownBy(cc::getKibana).isInstanceOf(RuntimeException.class).hasMessage("x");
  }

  @Test
  @DisplayName("Kiali: cached")
  void testKialiCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://ki");
    setPrivate(cc, "kiali", cached);

    assertThat(cc.getKiali()).isSameAs(cached);
  }

  @Test
  @DisplayName("Kiali: success")
  void testKialiSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://ki");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getKiali().getUrl()).isEqualTo("http://ki");
  }

  @Test
  @DisplayName("Kiali: dummy")
  void testKialiDummy() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found with label kiali")).when(cc).getService(any());

    assertThat(cc.getKiali().getUrl()).isEqualTo("http://localhost:9999/");
  }

  @Test
  @DisplayName("Kiali: rethrow")
  void testKialiRethrow() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("fail")).when(cc).getService(any());

    assertThatThrownBy(cc::getKiali).isInstanceOf(RuntimeException.class).hasMessage("fail");
  }

  @Test
  @DisplayName("Jager: cached")
  void testJagerCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://j");
    setPrivate(cc, "jaeger", cached);

    assertThat(cc.getJager()).isSameAs(cached);
  }

  @Test
  @DisplayName("Jager: success")
  void testJagerSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://j");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getJager().getUrl()).isEqualTo("http://j");
  }

  @Test
  @DisplayName("Jager: dummy")
  void testJagerDummy() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found with label jager")).when(cc).getService(any());

    assertThat(cc.getJager().getUrl()).isEqualTo("http://localhost:9999/");
  }

  @Test
  @DisplayName("Jaeger: rethrow")
  void testJaegerRethrow() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("err")).when(cc).getService(any());

    assertThatThrownBy(cc::getJager).isInstanceOf(RuntimeException.class).hasMessage("err");
  }

  @Test
  void testDefaultGrafanaCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://dg");
    setPrivate(cc, "defaultGrafana", cached);

    assertThat(cc.getDefaultGrafana()).isSameAs(cached);
  }

  @Test
  void testDefaultGrafanaSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://dg");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getDefaultGrafana().getUrl()).isEqualTo("http://dg");
  }

  @Test
  void testDefaultGrafanaDummy() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("No pods found with label graf")).when(cc).getService(any());

    assertThat(cc.getDefaultGrafana().getUrl()).isEqualTo("http://localhost:9999/");
  }

  @Test
  void testDefaultGrafanaRethrow() {
    var set = configSetup(false);
    var cc = prepare(set);

    doThrow(new RuntimeException("boom")).when(cc).getService(any());

    assertThatThrownBy(cc::getDefaultGrafana)
        .isInstanceOf(RuntimeException.class)
        .hasMessage("boom");
  }

  @Test
  void testControlPlaneCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://cp");
    setPrivate(cc, "controlPlane", cached);

    assertThat(cc.getControlPlane()).isSameAs(cached);
  }

  @Test
  void testControlPlaneSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://cp");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getControlPlane().getUrl()).isEqualTo("http://cp");
  }

  @Test
  void testWiremockCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("xxx");
    setPrivate(cc, "wiremock", cached);

    assertThat(cc.getWiremock()).isSameAs(cached);
  }

  @Test
  void testWiremockSuccessRewrite() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service original = new Service("https://abc.com/test/");
    doReturn(original).when(cc).getService(any());

    Service result = cc.getWiremock();

    assertThat(result.getUrl()).isEqualTo("abc.com/test");
  }

  @Test
  void testEmailCached() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service cached = new Service("http://mail");
    setPrivate(cc, "email", cached);

    assertThat(cc.getEmail()).isSameAs(cached);
  }

  @Test
  void testEmailSuccess() {
    var set = configSetup(false);
    var cc = prepare(set);

    Service expected = new Service("http://mail");
    doReturn(expected).when(cc).getService(any());

    assertThat(cc.getEmail().getUrl()).isEqualTo("http://mail");
  }

  @Test
  void testNexusCached() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service cached = new Service("http://nx");
    setPrivate(cc, "nexus", cached);

    assertThat(cc.getNexus()).isSameAs(cached);
  }

  @Test
  void testNexusSuccess() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service expected = new Service("http://nx");
    doReturn(expected).when(cc).getServiceWithUser(any());

    assertThat(cc.getNexus()).isSameAs(expected);
  }

  @Test
  void testKeycloakCached() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service cached = new Service("http://kc");
    setPrivate(cc, "keycloak", cached);

    assertThat(cc.getKeycloak()).isSameAs(cached);
  }

  @Test
  void testKeycloakSuccessOpenshift() {
    var set = configSetup(true);
    when(set.orchestratorClient.getType()).thenReturn("openshift");

    var cc = prepare(set);

    Service base = new Service("http://kc");
    doReturn(base).when(cc).getServiceWithUser(any());

    assertThat(cc.getKeycloak().getUrl()).isEqualTo("http://kc");
  }

  @Test
  void testKeycloakSuccessKubernetesRewrite() {
    var set = configSetup(true);
    when(set.orchestratorClient.getType()).thenReturn("kubernetes");

    var cc = prepare(set);

    Service base = new Service("http://kc");
    doReturn(base).when(cc).getServiceWithUser(any());

    assertThat(cc.getKeycloak().getUrl()).isEqualTo("http://kc/auth/");
  }

  @Test
  void testVaultSuccess() {
    var set = configSetup(true);
    var cc = prepare(set);

    set.configuration.getCentralConfiguration().getVault().setSecret("s1");
    when(set.orchestratorClient.getTokenVault("s1")).thenReturn("TKN");

    doReturn(new Service("http://vault")).when(cc).getService(any());

    Service result = cc.getVaultService();

    assertThat(result.getToken()).isEqualTo("TKN");
    assertThat(result.getUrl()).isEqualTo("http://vault");
  }

  @Test
  @DisplayName("VaultService: returns cached instance when already initialized")
  void testVaultServiceCached() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service cached = new Service("http://vault.cached");
    setPrivate(cc, "vaultService", cached);

    Service result = cc.getVaultService();

    assertThat(result).isSameAs(cached);
  }

  @Test
  @DisplayName("VaultService: resolves via getServiceVaultWithToken when cache empty")
  void testVaultServiceResolved() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service resolved = new Service("http://vault.real");
    doReturn(resolved).when(cc).getServiceVaultWithToken(any());

    Service result = cc.getVaultService();

    assertThat(result).isSameAs(resolved);
  }

  @Test
  void testClusterSourcesOrchestrator() {
    var set = configSetup(true);
    var cc = prepare(set);

    List<CatalogSource> expected = List.of(mock(CatalogSource.class));
    when(set.orchestratorClient.getClusterSources()).thenReturn(expected);

    assertThat(cc.getClusterSources()).isEqualTo(expected);
  }

  @Test
  void testClusterSourcesNoOrchestrator() {
    var set = configSetup(false);
    var cc = prepare(set);

    doReturn(List.of()).when(cc).getClusterSources();

    assertThat(cc.getClusterSources()).isNotNull();
  }

  @Test
  @DisplayName("Jenkins: cached instance is returned")
  void testJenkinsCached() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service cached = new Service("http://jenkins.cached");
    setPrivate(cc, "jenkins", cached);

    assertThat(cc.getJenkins()).isSameAs(cached);
  }

  @Test
  @DisplayName("Jenkins: resolves via getServiceWithUser when not cached")
  void testJenkinsResolved() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service expected = new Service("http://jenkins.real");
    doReturn(expected).when(cc).getServiceWithUser(any());

    Service result = cc.getJenkins();

    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("Gerrit: cached instance returned")
  void testGerritCached() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service cached = new Service("http://gerrit.cached");
    setPrivate(cc, "gerrit", cached);

    assertThat(cc.getGerrit()).isSameAs(cached);
  }

  @Test
  @DisplayName("Gerrit: resolves via getServiceWithUser when not cached")
  void testGerritResolved() {
    var set = configSetup(true);
    var cc = prepare(set);

    Service expected = new Service("http://gerrit.real");
    doReturn(expected).when(cc).getServiceWithUser(any());

    Service result = cc.getGerrit();

    assertThat(result).isSameAs(expected);
  }

  @Test
  @DisplayName("KeycloakClient: returns cached instance when already initialized")
  void testKeycloakClientCached() {
    var set = configSetup(true);
    var cc = prepare(set);

    KeycloakClient cached = mock(KeycloakClient.class);
    setPrivate(cc, "keycloakClient", cached);

    assertThat(cc.getKeycloakClient()).isSameAs(cached);
  }

  private record ConfigSetup(
      Configuration configuration, Service ocService, OrchestratorClient orchestratorClient) {}
}
