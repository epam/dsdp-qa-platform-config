package platform.qa.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import platform.qa.entities.*;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.orchestrator.OrchestratorClient;
import platform.qa.providers.impl.RegistryUserProvider;
import platform.qa.services.UserService;
import platform.qa.utils.LocalizationUtils;
import platform.qa.utils.OrchestratorServiceProvider;

class RegistryConfigTest {

  /* -------------------------------------------------------------
     STATIC MOCK MasterConfig
  ------------------------------------------------------------- */
  private static MockedStatic<MasterConfig> masterMock;

  @BeforeAll
  static void beforeAll() {
    masterMock = mockStatic(MasterConfig.class);
    MasterConfig mc = mock(MasterConfig.class);
    masterMock.when(MasterConfig::getInstance).thenReturn(mc);
    when(mc.getToken()).thenReturn("fake-token");
  }

  @AfterAll
  static void afterAll() {
    masterMock.close();
  }

  /* -------------------------------------------------------------
     ENUMS for PARAMETERIZED TESTS
  ------------------------------------------------------------- */

  static Stream<Arguments> simpleServiceProvider() {
    return Stream.of(
        Arguments.of("getUserTaskManagement", "userTaskManagement"),
        Arguments.of("getUserProcessManagement", "userProcessManagement"),
        Arguments.of("getDigitalDocument", "digitalDocument"),
        Arguments.of("getOfficerPortal", "officerPortal"),
        Arguments.of("getCitizenPortal", "citizenPortal"),
        Arguments.of("getAdminPortal", "adminPortal"),
        Arguments.of("getProcessHistory", "processHistory"),
        Arguments.of("getWiremock", "wiremock"),
        Arguments.of("getNotificationService", "notificationService"));
  }

  /* -------------------------------------------------------------
     TEST SETUP: createRegistryConfig()
  ------------------------------------------------------------- */

  static Stream<Arguments> redashViewerProvider() {
    return Stream.of(
        Arguments.of("ua", "1.9.8", "/reports/api"),
        Arguments.of("en", "1.9.8", "/reports/api"),
        Arguments.of("ua", "1.9.7", "/reports/uk/api"),
        Arguments.of("en", "1.9.7", "/reports/en/api"));
  }

  static Stream<Arguments> gerritProvider() {
    return Stream.of(
        Arguments.of("http://gerrit", "http://gerrit/"),
        Arguments.of("http://gerrit/", "http://gerrit/"));
  }

  private RegistryConfig createRegistryConfig() throws Exception {

    Configuration config = mock(Configuration.class);
    RegistryConfiguration regConfig = mock(RegistryConfiguration.class);
    when(config.getRegistryConfiguration()).thenReturn(regConfig);

    // Ceph buckets
    CephBuckets cephBuckets = mock(CephBuckets.class);
    when(regConfig.getCeph()).thenReturn(cephBuckets);

    when(cephBuckets.getDataFileBucket()).thenReturn("bucket1");
    when(cephBuckets.getLowCodeFileBucket()).thenReturn("bucket2");
    when(cephBuckets.getExcerptBucket()).thenReturn("bucket3");
    when(cephBuckets.getDsoCertBucket()).thenReturn("bucket4");
    when(cephBuckets.getSignatureBucket()).thenReturn("signature-bucket");

    // Citus roles config
    ServiceConfiguration citusRoles = mock(ServiceConfiguration.class);
    when(regConfig.getCitusRoles()).thenReturn(citusRoles);
    when(citusRoles.getSecret()).thenReturn("citus-secret");

    // Nexus config
    ServiceConfiguration nexusCfg = mock(ServiceConfiguration.class);
    when(regConfig.getNexus()).thenReturn(nexusCfg);
    when(nexusCfg.getSecret()).thenReturn("nexus-secret");

    // Redash Viewer
    ServiceConfiguration redashViewerCfg = mock(ServiceConfiguration.class);
    when(regConfig.getRedashViewer()).thenReturn(redashViewerCfg);
    when(redashViewerCfg.getSecret()).thenReturn("redash-viewer-secret");

    // Redash Admin
    ServiceConfiguration redashAdminCfg = mock(ServiceConfiguration.class);
    when(regConfig.getRedashAdmin()).thenReturn(redashAdminCfg);
    when(redashAdminCfg.getSecret()).thenReturn("redash-admin-secret");

    // Gerrit
    ServiceConfiguration gerritCfg = mock(ServiceConfiguration.class);
    when(regConfig.getGerrit()).thenReturn(gerritCfg);
    when(gerritCfg.getSecret()).thenReturn("gerrit-secret");

    // Jenkins config
    ServiceConfiguration jenkinsCfg = mock(ServiceConfiguration.class);
    when(jenkinsCfg.getSecret()).thenReturn("jenkins-secret");
    when(regConfig.getJenkins()).thenReturn(jenkinsCfg);

    // Redis config
    ServiceConfiguration redisCfg = mock(ServiceConfiguration.class);
    when(regConfig.getRedis()).thenReturn(redisCfg);
    when(redisCfg.getSecret()).thenReturn("redis-secret");

    // Orchestrator client
    OrchestratorClient orch = mock(OrchestratorClient.class);
    when(orch.getCredentials(any())).thenReturn(new User("u", "p"));
    when(orch.getCredentialsWithoutLogin(any())).thenReturn(new User("x", "y"));

    // oc service
    User ocUser = new User("root", "pass");
    Service ocService = new Service("http://oc");
    ocService.setUser(ocUser);

    Service cephService = new Service("http://ceph-host");

    RegistryConfig rc =
        new RegistryConfig(
            config,
            "ns",
            ocService,
            mock(KeycloakClient.class),
            cephService,
            "ua",
            "1.9.7",
            "jenkins",
            "region",
            "openshift");

    // override private fields
    setField(rc, "orchestratorClient", orch);

    RegistryUserProvider rp = mock(RegistryUserProvider.class);
    UserService userService = mock(UserService.class);
    User user = new User("login", "pass");

    when(rp.get("testUser")).thenReturn(user);
    when(rp.getUserService()).thenReturn(userService);
    when(userService.refreshUserToken(user)).thenReturn(user);

    setField(rc, "registryUserProvider", rp);

    return rc;
  }

  private void setField(Object target, String field, Object value) throws Exception {
    Field f = target.getClass().getDeclaredField(field);
    f.setAccessible(true);
    f.set(target, value);
  }

  private Object invoke(Object rc, String method) throws Exception {
    return rc.getClass().getMethod(method).invoke(rc);
  }

  /* -------------------------------------------------------------
     HELPERS
  ------------------------------------------------------------- */

  private Object invoke(Object rc, String method, String param) throws Exception {
    return rc.getClass().getMethod(method, String.class).invoke(rc, param);
  }

  @ParameterizedTest
  @EnumSource(CephMethod.class)
  void testCeph_cached(CephMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    Ceph cached = mock(Ceph.class);

    setField(rc, sm.field, cached);

    assertThat((Ceph) invoke(rc, sm.getter)).isSameAs(cached);
  }

  /* -------------------------------------------------------------
     PARAMETERIZED TESTS: CEPH
  ------------------------------------------------------------- */

  @ParameterizedTest
  @EnumSource(CephMethod.class)
  void testCeph_created(CephMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    Ceph created = mock(Ceph.class);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {
      mocked
          .when(() -> OrchestratorServiceProvider.getCephService(any(), any(), any()))
          .thenReturn(created);

      assertThat((Ceph) invoke(rc, sm.getter)).isSameAs(created);
    }
  }

  @ParameterizedTest
  @EnumSource(DbMethod.class)
  void testDb_cached(DbMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    Db cached = mock(Db.class);

    setField(rc, sm.field, cached);

    assertThat((Db) invoke(rc, sm.getter)).isSameAs(cached);
  }

  /* -------------------------------------------------------------
     PARAMETERIZED TESTS: DB
  ------------------------------------------------------------- */

  @ParameterizedTest
  @EnumSource(DbMethod.class)
  void testDb_created(DbMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    Db created = mock(Db.class);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {
      mocked.when(() -> OrchestratorServiceProvider.getDbService(any(), any())).thenReturn(created);

      assertThat((Db) invoke(rc, sm.getter)).isSameAs(created);
    }
  }

  @ParameterizedTest
  @EnumSource(CitusRoleMethod.class)
  void testCitusRole_cached(CitusRoleMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    User cached = mock(User.class);

    setField(rc, sm.field, cached);

    assertThat((User) invoke(rc, sm.getter)).isSameAs(cached);
  }

  /* -------------------------------------------------------------
     PARAMETERIZED TESTS: CITUS ROLES
  ------------------------------------------------------------- */

  @ParameterizedTest
  @EnumSource(CitusRoleMethod.class)
  void testCitusRole_created(CitusRoleMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    User created = mock(User.class);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {
      mocked
          .when(
              () ->
                  OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(any(), any(), any()))
          .thenReturn(created);

      assertThat((User) invoke(rc, sm.getter)).isSameAs(created);
    }
  }

  /* -------------------------------------------------------------
     SPECIAL CASE: SOAP URL TRANSFORMATION
  ------------------------------------------------------------- */

  @ParameterizedTest
  @EnumSource(ServiceMethod.class)
  void testService_cached(ServiceMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    Service cached = mock(Service.class);

    setField(rc, sm.field, cached);

    Object result = sm.requiresUser ? invoke(rc, sm.getter, "testUser") : invoke(rc, sm.getter);

    assertThat((Service) result).isSameAs(cached);
  }

  /* -------------------------------------------------------------
     PARAMETERIZED TESTS: SERVICES
  ------------------------------------------------------------- */

  @ParameterizedTest
  @EnumSource(ServiceMethod.class)
  void testService_created(ServiceMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();
    Service created = mock(Service.class);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      if (sm.requiresUser) {
        mocked
            .when(() -> OrchestratorServiceProvider.getService(any(), any(), any()))
            .thenReturn(created);
      } else {
        mocked.when(() -> OrchestratorServiceProvider.getService(any(), any())).thenReturn(created);
      }

      Object result = sm.requiresUser ? invoke(rc, sm.getter, "testUser") : invoke(rc, sm.getter);

      assertThat(result).isSameAs(created);
    }
  }

  @Test
  void testSoapUrlTransformation() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service created = new Service("http://example.com/api");
    created.setUser(new User("u", "p"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getService(any(), any(), any()))
          .thenReturn(created);

      Service result = rc.getDataFactorySoap("testUser");

      assertThat(result.getUrl()).isEqualTo("http://example.com/api/ws?wsdl");
    }
  }

  @ParameterizedTest
  @MethodSource("simpleServiceProvider")
  void testSimpleServiceMethods(String methodName, String fieldName) throws Exception {
    RegistryConfig rc = createRegistryConfig();

    // 1. cached case
    Service cached = mock(Service.class);
    setField(rc, fieldName, cached);

    Service cachedResult = (Service) invoke(rc, methodName);
    assertThat(cachedResult).isSameAs(cached);

    // 2. created case
    Service created = mock(Service.class);
    setField(rc, fieldName, null); // reset cache

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {
      mocked.when(() -> OrchestratorServiceProvider.getService(any(), any())).thenReturn(created);

      Service result = (Service) invoke(rc, methodName);
      assertThat(result).isSameAs(created);
    }
  }

  @ParameterizedTest
  @EnumSource(UserServiceMethod.class)
  void testUserService_cached(UserServiceMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service cached = mock(Service.class);
    setField(rc, sm.field, cached);

    Service result = (Service) invoke(rc, sm.getter, "testUser");

    assertThat(result).isSameAs(cached);

    // verify refresh was called
    RegistryUserProvider rp = (RegistryUserProvider) getPrivateField(rc, "registryUserProvider");
    UserService us = rp.getUserService();
    verify(us, atLeastOnce()).refreshUserToken(any());
  }

  @ParameterizedTest
  @EnumSource(UserServiceMethod.class)
  void testUserService_created(UserServiceMethod sm) throws Exception {
    RegistryConfig rc = createRegistryConfig();

    setField(rc, sm.field, null); // ensure no cached instance

    Service created = mock(Service.class);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getService(any(), any(), any()))
          .thenReturn(created);

      Service result = (Service) invoke(rc, sm.getter, "testUser");

      assertThat(result).isSameAs(created);
    }
  }

  private Object getPrivateField(Object target, String name) throws Exception {
    Field f = target.getClass().getDeclaredField(name);
    f.setAccessible(true);
    return f.get(target);
  }

  @ParameterizedTest
  @MethodSource("redashViewerProvider")
  void testRedashViewer(String local, String release, String expectedPath) throws Exception {

    RegistryConfig rc = createRegistryConfig();
    setField(rc, "local", local);
    setField(rc, "release", release);

    Service svc = new Service("http://redash");
    svc.setUser(new User("u", "p"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked.when(() -> OrchestratorServiceProvider.getService(any(), any())).thenReturn(svc);

      mocked
          .when(() -> OrchestratorServiceProvider.getPasswordFromSecretByKey(any(), any(), any()))
          .thenReturn("zzz-token");

      Service result = (Service) invoke(rc, "getRedashViewer");

      assertThat(result.getUrl()).isEqualTo("http://redash" + expectedPath);
      assertThat(result.getToken()).isEqualTo("zzz-token");
    }
  }

  @Test
  void testRedashAdmin() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service svc = new Service("http://redash-admin");
    svc.setUser(new User("u", "p"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked.when(() -> OrchestratorServiceProvider.getService(any(), any())).thenReturn(svc);

      mocked
          .when(
              () ->
                  OrchestratorServiceProvider.getPasswordFromSecretByKey(
                      any(), any(), eq("admin-api-key")))
          .thenReturn("adm-token");

      Service result = (Service) invoke(rc, "getRedashAdmin");

      assertThat(result.getUrl()).isEqualTo("http://redash-admin/reports/api");
      assertThat(result.getToken()).isEqualTo("adm-token");
    }
  }

  @ParameterizedTest
  @MethodSource("gerritProvider")
  void testGetGerrit(String inputUrl, String expectedUrl) throws Exception {

    RegistryConfig rc = createRegistryConfig();

    Service svc = new Service(inputUrl);
    svc.setUser(new User("u", "p"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getService(any(), any(), any()))
          .thenReturn(svc);

      Service result = (Service) invoke(rc, "getGerrit");

      assertThat(result.getUrl()).isEqualTo(expectedUrl);
    }
  }

  @Test
  void testGetJenkins_success() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service svc = new Service("http://jenkins");
    svc.setUser(new User("u", "p"));

    OrchestratorClient orch = mock(OrchestratorClient.class);
    setField(rc, "orchestratorClient", orch);

    when(orch.getCredentials(any())).thenReturn(new User("jen", "kins"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(
              () ->
                  OrchestratorServiceProvider.getService(
                      eq(orch), any(ServiceConfiguration.class), any(User.class)))
          .thenReturn(svc);

      Service result = rc.getJenkins();

      assertThat(result).isSameAs(svc);
    }
  }

  @Test
  void testGetJenkins_fallbackToTekton() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    OrchestratorClient orch = mock(OrchestratorClient.class);
    setField(rc, "orchestratorClient", orch);

    when(orch.getCredentials(any())).thenThrow(new NullPointerException("test NPE"));

    Service result = (Service) invoke(rc, "getJenkins");

    assertThat(result).isNull();
    assertThat(getPrivateField(rc, "cicdTool")).isEqualTo("tekton");
  }

  @Test
  void testGetJenkins_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service cached = new Service("http://cached-jenkins");
    setField(rc, "jenkins", cached);

    OrchestratorClient orch = mock(OrchestratorClient.class);
    setField(rc, "orchestratorClient", orch);

    when(orch.getCredentials(any())).thenThrow(new RuntimeException("should not be called"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      Service result = rc.getJenkins();

      assertThat(result).isSameAs(cached);

      mocked.verifyNoInteractions();

      verify(orch, never()).getCredentials(any());
    }
  }

  @Test
  void testGetNexus() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    OrchestratorClient orch = mock(OrchestratorClient.class);
    setField(rc, "orchestratorClient", orch);

    when(orch.getCredentials(any())).thenReturn(new User("nx", "us"));

    Service svc = new Service("http://nexus");
    svc.setUser(new User("nx", "us"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getService(any(), any(), any()))
          .thenReturn(svc);

      Service result = (Service) invoke(rc, "getNexus");

      assertThat(result).isSameAs(svc);
    }
  }

  @Test
  void testGetRedis_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Redis cached = new Redis("cached-url", "pwd");
    setField(rc, "redis", cached);

    Redis result = rc.getRedis();

    assertThat(result).isSameAs(cached);
  }

  @Test
  void testGetRedis_created() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Redis created = new Redis("new-url", "pass");

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getRedisService(any(), any(), any()))
          .thenReturn(created);

      Redis result = rc.getRedis();

      assertThat(result).isSameAs(created);
    }
  }

  @Test
  void testGetRedis_reinit() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Redis cached = new Redis("old-url", "old-pwd");
    setField(rc, "redis", cached);

    Redis created = new Redis("new-url", "new-pwd");

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getRedisService(any(), any(), any()))
          .thenReturn(created);

      Redis result = rc.getRedis(true);

      assertThat(result).isSameAs(created).isNotSameAs(cached);
    }
  }

  @Test
  void testGetRedisList_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Redis redisObj = new Redis("url", "pwd");
    setField(rc, "redis", redisObj);

    var list = java.util.List.of(redisObj);
    setField(rc, "redisServices", list);

    var result = rc.getRedisList();

    assertThat(result).isSameAs(list);
  }

  @Test
  void testGetRedisList_reinit() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Redis cached = new Redis("cached-url", "cached");
    setField(rc, "redis", cached);

    var newList = java.util.List.of(new Redis("new1", "p1"), new Redis("new2", "p2"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getRedisServices(any(), any(), any()))
          .thenReturn(newList);

      var result = rc.getRedisList(true);

      assertThat(result).isSameAs(newList);
    }
  }

  @Test
  void testGetRedisList_createdWhenNoCache() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    setField(rc, "redis", null);

    var newList = java.util.List.of(new Redis("url", "pwd"));

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked
          .when(() -> OrchestratorServiceProvider.getRedisServices(any(), any(), any()))
          .thenReturn(newList);

      var result = rc.getRedisList();

      assertThat(result).isSameAs(newList);
    }
  }

  @Test
  void testSetLocalizationFilePath() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    rc.setLocalizationFilePath("new/path/file.json");

    assertThat(getPrivateField(rc, "localizationFilePath")).isEqualTo("new/path/file.json");
  }

  @Test
  void testGetLocalizationDataForSelectedLanguageByKey() throws Exception {
    RegistryConfig rc = createRegistryConfig();
    setField(rc, "localizationFilePath", "loc.json");
    setField(rc, "local", "ua");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataForSelectedLanguageByKey("loc.json", "ua", "hello"))
                    .thenReturn("Привіт"))) {

      String result = rc.getLocalizationDataForSelectedLanguageByKey("hello");

      // verify result
      assertThat(result).isEqualTo("Привіт");

      // verify correct call
      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance).getLocalizationDataForSelectedLanguageByKey("loc.json", "ua", "hello");
    }
  }

  @Test
  void testGetLocalizationDataForSelectedLanguageByPath() throws Exception {
    RegistryConfig rc = createRegistryConfig();
    setField(rc, "localizationFilePath", "loc.json");
    setField(rc, "local", "en");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataForSelectedLanguageByPath(
                        "loc.json", "en", "greeting", "level1", "level2"))
                    .thenReturn("Hello!"))) {

      String result =
          rc.getLocalizationDataForSelectedLanguageByPath("greeting", "level1", "level2");

      assertThat(result).isEqualTo("Hello!");

      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance)
          .getLocalizationDataForSelectedLanguageByPath(
              "loc.json", "en", "greeting", "level1", "level2");
    }
  }

  @Test
  void testGetLocalizationDataForAllLanguages() throws Exception {
    RegistryConfig rc = createRegistryConfig();
    setField(rc, "localizationFilePath", "loc.json");

    List<String> mockList = List.of("UA text", "EN text");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataForAllLanguages("loc.json", "bye"))
                    .thenReturn(mockList))) {

      List<String> result = rc.getLocalizationDataForAllLanguages("bye");

      assertThat(result).isEqualTo(mockList);

      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance).getLocalizationDataForAllLanguages("loc.json", "bye");
    }
  }

  @Test
  void testGetLocalizationDataFromFlatJsonForSelectedLanguageByKey() throws Exception {
    RegistryConfig rc = createRegistryConfig();
    setField(rc, "localizationFilePath", "flat-loc.json");
    setField(rc, "local", "az");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
                        "flat-loc.json", "az", "Name"))
                    .thenReturn("Ad"))) {

      String result = rc.getLocalizationDataFromFlatJsonForSelectedLanguageByKey("Name");

      // verify result
      assertThat(result).isEqualTo("Ad");

      // verify correct call
      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance)
          .getLocalizationDataFromFlatJsonForSelectedLanguageByKey("flat-loc.json", "az", "Name");
    }
  }

  @Test
  void testGetLocalizationDataFromFlatJsonForSelectedLanguageByKey_withEnglish() throws Exception {
    RegistryConfig rc = createRegistryConfig();
    setField(rc, "localizationFilePath", "flat-loc.json");
    setField(rc, "local", "en");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
                        "flat-loc.json", "en", "Email"))
                    .thenReturn("Email"))) {

      String result = rc.getLocalizationDataFromFlatJsonForSelectedLanguageByKey("Email");

      assertThat(result).isEqualTo("Email");

      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance)
          .getLocalizationDataFromFlatJsonForSelectedLanguageByKey("flat-loc.json", "en", "Email");
    }
  }

  @Test
  void testGetLocalizationDataFromFlatJsonForSelectedLanguageByKey_withUkrainian()
      throws Exception {
    RegistryConfig rc = createRegistryConfig();
    setField(rc, "localizationFilePath", "localization.json");
    setField(rc, "local", "uk");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
                        "localization.json", "uk", "Phone"))
                    .thenReturn("Номер телефону"))) {

      String result = rc.getLocalizationDataFromFlatJsonForSelectedLanguageByKey("Phone");

      assertThat(result).isEqualTo("Номер телефону");

      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance)
          .getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
              "localization.json", "uk", "Phone");
    }
  }

  @Test
  void testGetLocalizationDataFromFlatJsonForSelectedLanguageByKey_withDefaultPath()
      throws Exception {
    RegistryConfig rc = createRegistryConfig();
    // Use default localizationFilePath (no setField call)
    setField(rc, "local", "en");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
                        "src/test/resources/properties/localizationData.json", "en", "Surname"))
                    .thenReturn("Surname"))) {

      String result = rc.getLocalizationDataFromFlatJsonForSelectedLanguageByKey("Surname");

      assertThat(result).isEqualTo("Surname");

      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance)
          .getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
              "src/test/resources/properties/localizationData.json", "en", "Surname");
    }
  }

  @Test
  void testGetLocalizationDataFromFlatJsonForSelectedLanguageByKey_withCustomPath()
      throws Exception {
    RegistryConfig rc = createRegistryConfig();

    // Set custom localization file path
    rc.setLocalizationFilePath("custom/path/localization.json");
    setField(rc, "local", "az");

    try (MockedConstruction<LocalizationUtils> mocked =
        mockConstruction(
            LocalizationUtils.class,
            (mock, ctx) ->
                when(mock.getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
                        "custom/path/localization.json", "az", "Status"))
                    .thenReturn("Vəziyyət"))) {

      String result = rc.getLocalizationDataFromFlatJsonForSelectedLanguageByKey("Status");

      assertThat(result).isEqualTo("Vəziyyət");

      LocalizationUtils instance = mocked.constructed().get(0);
      verify(instance)
          .getLocalizationDataFromFlatJsonForSelectedLanguageByKey(
              "custom/path/localization.json", "az", "Status");
    }
  }

  @Test
  void testGetRedashAdmin_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service cached = new Service("http://cached-redash-admin");
    cached.setUser(new User("u", "p"));

    setField(rc, "redashAdmin", cached);

    Service result = rc.getRedashAdmin();

    assertThat(result).isSameAs(cached);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {
      mocked.verifyNoInteractions();
    }
  }

  @Test
  void testGetGerrit_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service cached = new Service("http://gerrit-cached/");
    cached.setUser(new User("u", "p"));

    setField(rc, "gerrit", cached);

    Service result = rc.getGerrit();

    assertThat(result).isSameAs(cached);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {
      mocked.verifyNoInteractions();
    }
  }

  @Test
  void testGetDataFactoryPublicApiSystem_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service cached = mock(Service.class);
    setField(rc, "dataFactoryPublicApiSystem", cached);

    Service result = rc.getDataFactoryPublicApiSystem();

    assertThat(result).isSameAs(cached);
  }

  @Test
  void testGetDataFactoryPublicApiSystem_created() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    Service created = mock(Service.class);

    try (MockedStatic<OrchestratorServiceProvider> mocked =
        mockStatic(OrchestratorServiceProvider.class)) {

      mocked.when(() -> OrchestratorServiceProvider.getService(any(), any())).thenReturn(created);

      Service result = rc.getDataFactoryPublicApiSystem();

      assertThat(result).isSameAs(created);
    }
  }

  @Test
  void testGetDataFactorySoap_cached() throws Exception {
    RegistryConfig rc = createRegistryConfig();

    // cached service
    Service cached = new Service("http://x");
    cached.setUser(new User("old", "old"));
    setField(rc, "dataFactorySoap", cached);

    RegistryUserProvider rp = (RegistryUserProvider) getPrivateField(rc, "registryUserProvider");
    UserService us = rp.getUserService();
    User refreshed = new User("newLogin", "newPass");

    when(us.refreshUserToken(any())).thenReturn(refreshed);

    Service result = rc.getDataFactorySoap("testUser");

    assertThat(result).isSameAs(cached);
    assertThat(result.getUser()).isEqualTo(refreshed);

    verify(us, times(1)).refreshUserToken(any());
  }

  /** Ceph getters mapping */
  private enum CephMethod {
    FILE_DATA("fileDataCeph", "getFileDataCeph"),
    FILE_LOW_CODE("fileLowcodeCeph", "getFileLowcodeCeph"),
    EXCERPT("excerptCeph", "getExcerptCeph"),
    DSO_CERT("dsoCertCeph", "getDsoCertCeph");

    final String field;
    final String getter;

    CephMethod(String field, String getter) {
      this.field = field;
      this.getter = getter;
    }
  }

  /** DB getters mapping */
  private enum DbMethod {
    CITUS_MASTER("citusMaster", "getCitusMaster"),
    CITUS_REPLICA("citusReplica", "getCitusReplica");

    final String field;
    final String getter;

    DbMethod(String field, String getter) {
      this.field = field;
      this.getter = getter;
    }
  }

  /** Citus roles mapping */
  private enum CitusRoleMethod {
    ADMIN("citusAdminRole", "getCitusAdminRole"),
    APPLICATION("citusApplicationRole", "getCitusApplicationRole"),
    OWNER("citusRegistryOwnerRole", "getCitusRegistryOwnerRole"),
    SETTINGS("citusSettingsRole", "getCitusSettingsRole"),
    AUDIT("citusAuditRole", "getCitusAuditRole"),
    ANALYTICS("citusAnalyticsRole", "getCitusAnalyticsRoleRole"),
    EXCERPT_EXPORTED("citusExcerptExportedRole", "getCitusExcerptExportedRole"),
    EXCERPT("citusExcerptRole", "getCitusExcerptRole"),
    EXCERPT_WORKER("citusExcerptWorkerRole", "getCitusExcerptWorkerRole");

    final String field;
    final String getter;

    CitusRoleMethod(String field, String getter) {
      this.field = field;
      this.getter = getter;
    }
  }

  /** Service getters mapping */
  private enum ServiceMethod {
    DATA_FACTORY("dataFactory", "getDataFactory", true),
    DATA_FACTORY_EXTERNAL_PLATFORM(
        "dataFactoryExternalPlatform", "getDataFactoryExternalPlatform", true),
    REGISTRY_MANAGEMENT("registryManagement", "getRegistryManagement", true),
    DATA_FACTORY_EXTERNAL_SYSTEM(
        "dataFactoryExternalSystem", "getDataFactoryExternalSystem", false),
    BPMS("bpms", "getBpms", true),
    USER_SETTINGS("userSettings", "getUserSettings", true);
    final String field;
    final String getter;
    final boolean requiresUser;

    ServiceMethod(String field, String getter, boolean requiresUser) {
      this.field = field;
      this.getter = getter;
      this.requiresUser = requiresUser;
    }
  }

  private enum UserServiceMethod {
    DIGITAL_SIGNATURE("digitalSignatureOps", "getDigitalSignatureOps"),
    FORM_MODEL("formManagementModeler", "getFormManagementModeler"),
    FORM_PROVIDER("formManagementProvider", "getFormManagementProvider"),
    FORM_SCHEMA("formSchemaProvider", "getFormSchemaProvider"),
    EXCERPT("excerpt", "getExcerpt"),
    PROCESS_GATEWAY("processWebserviceGateway", "getProcessWebserviceGateway"),
    PROCESS_GATEWAY_TREMBITA("processWebserviceGateway", "getProcessWebserviceGatewayTrembita"),
    FORM_SUBMISSION_VALIDATION("formSubmissionValidation", "getForSubmissionValidation");

    final String field;
    final String getter;

    UserServiceMethod(String field, String getter) {
      this.field = field;
      this.getter = getter;
    }
  }
}
