package platform.qa.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.LocalPortForward;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.openshift.client.OpenShiftClient;
import java.util.*;
import jodd.util.Base64;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import platform.qa.configuration.MasterConfig;
import platform.qa.entities.*;
import platform.qa.extension.SocketAnalyzer;
import platform.qa.oc.OkdClient;

public class OpenshiftServiceProviderTest {

  private OkdClient ocClient;
  private ServiceConfiguration config;

  @BeforeEach
  void setup() {
    ocClient = mock(OkdClient.class);
    config = mock(ServiceConfiguration.class);
  }

  // -------------------------------
  // getService(Service, Config)
  // -------------------------------
  @Test
  void testGetService_noPortForward_routeExists() {
    Service ocService = new Service("http://oc");

    when(config.getNamespace()).thenReturn("ns");
    when(config.isPortForwarding()).thenReturn(false);
    when(config.getRoute()).thenReturn("abc");

    try (MockedConstruction<OkdClient> okdMock =
        mockConstruction(
            OkdClient.class,
            (mock, ctx) ->
                doReturn(new HashMap<>(Map.of("abc", "http://route"))).when(mock).getOkdRoutes())) {

      Service svc = OpenshiftServiceProvider.getService(ocService, config);

      assertThat(svc.getUrl()).isEqualTo("http://route");
    }
  }

  @Test
  void testGetService_portForwarding() {
    Service ocService = new Service("http://oc");

    when(config.isPortForwarding()).thenReturn(true);
    when(config.getNamespace()).thenReturn("ns");
    when(config.getPodLabel()).thenReturn("lbl");
    when(config.getRoute()).thenReturn("rt");
    when(config.getDefaultPort()).thenReturn(1234);

    try (MockedConstruction<OkdClient> okdMock =
        mockConstruction(
            OkdClient.class,
            (mock, ctx) ->
                when(mock.performPortForwarding(any(), any(), anyInt())).thenReturn(5555))) {

      Service svc = OpenshiftServiceProvider.getService(ocService, config);

      assertThat(svc.getUrl()).isEqualTo("http://localhost:5555/");
    }
  }

  // -------------------------------
  // getService(OkdClient, Config)
  // -------------------------------
  @Test
  void testGetService_ocClient_routeExists() {
    when(config.isPortForwarding()).thenReturn(false);
    when(config.getRoute()).thenReturn("rt");

    doReturn(new HashMap<>(Map.of("rt", "http://real"))).when(ocClient).getOkdRoutes();

    Service svc = OpenshiftServiceProvider.getService(ocClient, config);

    assertThat(svc.getUrl()).isEqualTo("http://real");
  }

  @Test
  void testGetService_ocClient_portForward() {
    when(config.isPortForwarding()).thenReturn(true);
    when(config.getPodLabel()).thenReturn("p");
    when(config.getRoute()).thenReturn("rt");
    when(config.getDefaultPort()).thenReturn(123);

    when(ocClient.performPortForwarding(any(), any(), anyInt())).thenReturn(777);

    Service svc = OpenshiftServiceProvider.getService(ocClient, config);

    assertThat(svc.getUrl()).isEqualTo("http://localhost:777/");
  }

  // -------------------------------------
  // getService with User
  // -------------------------------------
  @Test
  void testGetService_withUser() {
    User u = new User("login", "pwd");

    when(config.isPortForwarding()).thenReturn(false);
    when(config.getRoute()).thenReturn("abc");

    HashMap<String, String> routes = new HashMap<>();
    routes.put("abc", "http://host");

    doReturn(routes).when(ocClient).getOkdRoutes();

    Service svc = OpenshiftServiceProvider.getService(ocClient, config, u);

    assertThat(svc.getUrl()).isEqualTo("http://host");
    assertThat(svc.getUser()).isEqualTo(u);
  }

  // -------------------------------
  // getDbService
  // -------------------------------
  @Test
  void testGetDbService() {
    User creds = new User("pg", "pass");

    when(config.getSecret()).thenReturn("citus-secret");
    when(config.getPodLabel()).thenReturn("db");
    when(config.getDefaultPort()).thenReturn(5432);
    when(config.getRoute()).thenReturn("citus");

    Service fakeDbService = new Service("http://some-db/");

    try (MockedStatic<OpenshiftServiceProvider> staticMock =
        mockStatic(
            OpenshiftServiceProvider.class,
            invocation -> {
              if (invocation.getMethod().getName().equals("getDbPodService")) {
                return fakeDbService;
              }

              return invocation.callRealMethod();
            })) {

      when(ocClient.getCredentials("citus-secret")).thenReturn(creds);

      Db db = OpenshiftServiceProvider.getDbService(ocClient, config);

      assertThat(db.getUrl()).isEqualTo("jdbc:postgresql://some-db/");
      assertThat(db.getUser()).isEqualTo("pg");
      assertThat(db.getPassword()).isEqualTo("pass");
    }
  }

  // -------------------------------
  // getUserSecretsBySecretNameAndKey
  // -------------------------------
  @Test
  void testGetUserSecrets() {
    Map<String, String> secrets = new HashMap<>();
    secrets.put("adminName", Base64.encodeToString("root"));
    secrets.put("adminPass", Base64.encodeToString("pwd"));

    when(ocClient.getSecretsByName("sec")).thenReturn(secrets);

    User u = OpenshiftServiceProvider.getUserSecretsBySecretNameAndKey(ocClient, "sec", "admin");

    assertThat(u.getLogin()).isEqualTo("root");
    assertThat(u.getPassword()).isEqualTo("pwd");
  }

  // -------------------------------
  // getPasswordFromSecretByKey
  // -------------------------------
  @Test
  void testGetPasswordFromSecret() {
    when(ocClient.getSecretsByName("s")).thenReturn(Map.of("key", Base64.encodeToString("secret")));

    String val = OpenshiftServiceProvider.getPasswordFromSecretByKey(ocClient, "s", "key");

    assertThat(val).isEqualTo("secret");
  }

  // -------------------------------
  // getCephService
  // -------------------------------
  @Test
  void testGetCephService() {
    Map<String, String> secret =
        Map.of(
            "AWS_ACCESS_KEY_ID", Base64.encodeToString("ak"),
            "AWS_SECRET_ACCESS_KEY", Base64.encodeToString("sk"));
    Map<String, String> cfg =
        Map.of(
            "BUCKET_NAME", "b",
            "BUCKET_HOST", "host");

    when(ocClient.getSecretsByName("ceph-sec")).thenReturn(secret);
    when(ocClient.getConfigurationMap("ceph-sec")).thenReturn(cfg);

    ServiceConfiguration cephCfg = mock(ServiceConfiguration.class);
    when(cephCfg.isPortForwarding()).thenReturn(false);

    MasterConfig mc = mock(MasterConfig.class);
    Configuration globalCfg = mock(Configuration.class);
    CentralConfiguration cc = mock(CentralConfiguration.class);

    when(globalCfg.getCentralConfiguration()).thenReturn(cc);
    when(cc.getCeph()).thenReturn(cephCfg);

    try (MockedStatic<MasterConfig> m = mockStatic(MasterConfig.class)) {
      m.when(MasterConfig::getInstance).thenReturn(mc);
      when(mc.getConfiguration()).thenReturn(globalCfg);

      Ceph ceph = OpenshiftServiceProvider.getCephService(ocClient, "ceph-sec", null);

      assertThat(ceph.getBucketName()).isEqualTo("b");
      assertThat(ceph.getHost()).isEqualTo("host");
    }
  }

  // -------------------------------
  // getDbPodService SUCCESS
  // -------------------------------
  @Test
  void testGetDbPodService_success() {
    when(config.getPodLabel()).thenReturn("citus");
    when(config.getRoute()).thenReturn("citus");
    when(config.getDefaultPort()).thenReturn(5432);

    // ---------- Fake Pod ----------
    Pod pod = new Pod();
    pod.setMetadata(new ObjectMeta());
    pod.getMetadata().setName("citus-xyz");

    PodList list = new PodList();
    list.setItems(List.of(pod));

    // ---------- Fabric8 mocks ----------
    OpenShiftClient client = mock(OpenShiftClient.class);

    MixedOperation<Pod, PodList, PodResource<Pod>> podsOp = mock(MixedOperation.class);
    PodResource<Pod> podRes = mock(PodResource.class);

    // return the OpenShiftClient
    doReturn(client).when(ocClient).getOsClient();

    when(client.pods()).thenReturn(podsOp);
    when(podsOp.withLabel("citus")).thenReturn(podsOp);
    when(podsOp.list()).thenReturn(list);
    when(podsOp.withName("citus-xyz")).thenReturn(podRes);

    // ---------- portForward mock ----------
    LocalPortForward lpf = mock(LocalPortForward.class);
    doReturn(lpf).when(podRes).portForward(eq(5432), anyInt());

    // ---------- Fake socket analyzer ----------
    try (MockedConstruction<SocketAnalyzer> sockets =
        mockConstruction(
            SocketAnalyzer.class, (mock, ctx) -> when(mock.getAvailablePort()).thenReturn(7777))) {

      Service svc = OpenshiftServiceProvider.getDbPodService(ocClient, config);

      assertThat(svc.getUrl()).isEqualTo("http://localhost:7777/");
    }
  }

  // -------------------------------
  // getRedisService
  // -------------------------------
  @Test
  void testGetRedisService_portForwarding() {
    OkdClient okd = mock(OkdClient.class);

    when(config.isPortForwarding()).thenReturn(true);
    when(config.getPodLabel()).thenReturn("redis");
    when(config.getDefaultPort()).thenReturn(6379);

    User user = new User("u", "pwd");

    // ---- mock OS client ----
    OpenShiftClient k8s = mock(OpenShiftClient.class);
    MixedOperation pods = mock(MixedOperation.class);
    PodResource podRes = mock(PodResource.class);

    // prepare pod list
    Pod pod = new Pod();
    pod.setMetadata(new ObjectMeta());
    pod.getMetadata().setName("redis-0");

    PodList podList = new PodList();
    podList.setItems(List.of(pod));

    // return OpenShiftClient instead of KubernetesClient
    doReturn(k8s).when(okd).getOsClient();
    when(k8s.pods()).thenReturn(pods);
    when(pods.list()).thenReturn(podList);
    when(pods.withName("redis-0")).thenReturn(podRes);

    // port-forward mock
    LocalPortForward lpf = mock(LocalPortForward.class);
    doReturn(lpf).when(podRes).portForward(eq(6379), anyInt());

    try (MockedConstruction<SocketAnalyzer> sockets =
        mockConstruction(
            SocketAnalyzer.class, (mock, ctx) -> when(mock.getAvailablePort()).thenReturn(12345))) {

      Redis result = OpenshiftServiceProvider.getRedisService(okd, config, user);

      assertThat(result.getUrl()).isEqualTo("http://localhost:12345/");
      assertThat(result.getPassword()).isEqualTo("pwd");
    }
  }

  @Test
  void testGetRedisService_noPortForwarding() {
    when(config.isPortForwarding()).thenReturn(false);

    OpenShiftClient os = mock(OpenShiftClient.class);
    MixedOperation<Pod, PodList, PodResource<Pod>> podsOp = mock(MixedOperation.class);

    doReturn(os).when(ocClient).getOsClient();
    when(os.pods()).thenReturn(podsOp);

    when(podsOp.list()).thenReturn(new PodList());

    Redis r = OpenshiftServiceProvider.getRedisService(ocClient, config, new User("l", "p"));

    assertThat(r).isNull();
  }

  // -------------------------------
  // getRedisServices
  // -------------------------------
  @Test
  void testGetRedisServices_multiplePods() {
    User u = new User("l", "pwd");

    when(config.isPortForwarding()).thenReturn(true);
    when(config.getPodLabel()).thenReturn("redis");
    when(config.getDefaultPort()).thenReturn(6379);

    // ---- Prepare pods ----
    Pod p1 = new Pod();
    p1.setMetadata(new ObjectMeta());
    p1.getMetadata().setName("redis-1");

    Pod p2 = new Pod();
    p2.setMetadata(new ObjectMeta());
    p2.getMetadata().setName("redis-2");

    PodList podList = new PodList();
    podList.setItems(List.of(p1, p2));

    // ---- Fabric8 mocks  ----
    OpenShiftClient os = mock(OpenShiftClient.class);
    MixedOperation<Pod, PodList, PodResource<Pod>> podsOp = mock(MixedOperation.class);
    PodResource<Pod> podRes = mock(PodResource.class);

    // OkdClient.getOsClient → OpenShiftClient
    doReturn(os).when(ocClient).getOsClient();

    when(os.pods()).thenReturn(podsOp);
    when(podsOp.list()).thenReturn(podList);
    when(podsOp.withName(anyString())).thenReturn(podRes);

    LocalPortForward fw = mock(LocalPortForward.class);
    when(podRes.portForward(eq(6379), anyInt())).thenReturn(fw);

    // ---- Fake ports for each new SocketAnalyzer instance ----
    try (MockedConstruction<SocketAnalyzer> sockets =
        mockConstruction(
            SocketAnalyzer.class,
            (mock, ctx) -> {
              int idx = ctx.getCount() - 1;
              when(mock.getAvailablePort()).thenReturn(7777 + idx);
            })) {

      List<Redis> services = OpenshiftServiceProvider.getRedisServices(ocClient, config, u);

      assertThat(services).hasSize(2);
      assertThat(services.get(0).getUrl()).isEqualTo("http://localhost:7777/");
      assertThat(services.get(1).getUrl()).isEqualTo("http://localhost:7778/");
    }
  }
}
