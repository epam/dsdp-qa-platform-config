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

import static platform.qa.utils.OrchestratorServiceProvider.getPasswordFromSecretByKey;
import static platform.qa.utils.OrchestratorServiceProvider.getService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import platform.qa.entities.Ceph;
import platform.qa.entities.Configuration;
import platform.qa.entities.Db;
import platform.qa.entities.Redis;
import platform.qa.entities.RegistryConfiguration;
import platform.qa.entities.Service;
import platform.qa.entities.User;
import platform.qa.enumeration.CitusUsers;
import platform.qa.factories.OrchestratorClientFactory;
import platform.qa.keycloak.KeycloakClient;
import platform.qa.oc.OkdClient;
import platform.qa.orchestrator.OrchestratorClient;
import platform.qa.providers.impl.RegistryUserProvider;
import platform.qa.utils.LocalizationUtils;
import platform.qa.utils.OrchestratorServiceProvider;

/**
 * Initiate and store Registry services. Registry services defined in {@link RegistryConfiguration}
 */
@Log4j2
public class RegistryConfig {
  public static final String REPORTS_API = "/reports/api";
  private static final String DEFAULT_LOCALIZATION_FILE_PATH =
      "src/test/resources/properties/localizationData.json";
  private static final int TOKEN_START_LENGTH = 20;
  private final String namespace;
  private final RegistryConfiguration configuration;
  @Getter private final OrchestratorClient orchestratorClient;
  @Getter private final OkdClient ocClient; // Keep for backwards compatibility
  private final KeycloakClient keycloakClient;
  private final Service ceph;
  private final Service oc;
  private final String local;
  private final String release;
  private String localizationFilePath = DEFAULT_LOCALIZATION_FILE_PATH;
  @Getter private RegistryUserProvider registryUserProvider;
  private User citusAdminRole;
  private User citusApplicationRole;
  private User citusRegistryOwnerRole;
  private User citusSettingsRole;
  private User citusAuditRole;
  private User citusAnalyticsRole;
  private User citusExcerptExportedRole;
  private User citusExcerptRole;
  private User citusExcerptWorkerRole;
  private Db citusMaster;
  private Db citusReplica;
  private Service registryManagement;
  private Service dataFactorySoap;
  private Service dataFactory;
  private Service dataFactoryExternalPlatform;
  private Service dataFactoryExternalSystem;
  private Service dataFactoryPublicApiSystem;
  private Service digitalSignatureOps;
  private Service userSettings;
  private Service bpms;
  private Service formManagementProvider;
  private Service formManagementModeler;
  private Service formSchemaProvider;
  private Service excerpt;
  private Service userTaskManagement;
  private Service userProcessManagement;
  private Service digitalDocument;
  private Service officerPortal;
  private Service citizenPortal;
  private Service adminPortal;
  private Service processHistory;
  private Service processWebserviceGateway;
  private Service redashViewer;
  private Service redashAdmin;
  private Service gerrit;
  private Service jenkins;
  private Service nexus;
  private Service notificationService;
  private Service formSubmissionValidation;
  private Service wiremock;
  private Ceph fileDataCeph;
  private Ceph fileLowcodeCeph;
  private Ceph excerptCeph;
  private Ceph dsoCertCeph;
  private Redis redis;
  private List<Redis> redisServices;
  private String cicdTool;
  private String region;

  public RegistryConfig(
      Configuration configuration,
      String namespace,
      Service ocService,
      KeycloakClient keycloakClient,
      Service ceph,
      String local,
      String release,
      String cicdTool,
      String region,
      String orchestratorType)
      throws IOException {
    this.configuration = configuration.getRegistryConfiguration();
    this.keycloakClient = keycloakClient;
    this.ceph = ceph;
    this.namespace = namespace;
    this.local = local;
    this.release = release;
    this.cicdTool = cicdTool;
    this.region = region;
    oc = ocService;

    // Get token from MasterConfig for Kubernetes authentication
    String token = MasterConfig.getInstance().getToken();
    log.info(
        "RegistryConfig - Orchestrator type: {}, Token available: {}",
        orchestratorType,
        (token != null && !token.trim().isEmpty()));

    if (token != null && !token.isEmpty()) {
      log.debug(
          "Token length: {}, starts with: {}...",
          token.length(),
          token.substring(0, Math.min(TOKEN_START_LENGTH, token.length())));
    }

    // Create the appropriate orchestrator client based on configuration
    orchestratorClient =
        OrchestratorClientFactory.createClient(orchestratorType, ocService, namespace, token);

    // Keep the old OkdClient for backwards compatibility
    // If orchestrator is not OpenShift, this will be null in some scenarios
    if (OrchestratorClientFactory.parseOrchestratorType(orchestratorType)
        == OrchestratorClientFactory.OrchestratorType.OPENSHIFT) {
      ocClient = (OkdClient) orchestratorClient;
    } else {
      ocClient = new OkdClient(ocService, namespace); // Fallback for compatibility
    }

    registryUserProvider =
        new RegistryUserProvider(namespace, keycloakClient, "properties/users.json");
  }

  /** Constructor for backwards compatibility - defaults to OpenShift */
  public RegistryConfig(
      Configuration configuration,
      String namespace,
      Service ocService,
      KeycloakClient keycloakClient,
      Service ceph,
      String local,
      String release,
      String cicdTool,
      String region)
      throws IOException {
    this(
        configuration,
        namespace,
        ocService,
        keycloakClient,
        ceph,
        local,
        release,
        cicdTool,
        region,
        "openshift");
  }

  public Ceph getFileDataCeph() {
    if (fileDataCeph != null) {
      return fileDataCeph;
    }

    fileDataCeph =
        OrchestratorServiceProvider.getCephService(
            orchestratorClient, configuration.getCeph().getDataFileBucket(), ceph.getUrl());
    return fileDataCeph;
  }

  public Ceph getFileLowcodeCeph() {
    if (fileLowcodeCeph != null) {
      return fileLowcodeCeph;
    }

    fileLowcodeCeph =
        OrchestratorServiceProvider.getCephService(
            orchestratorClient, configuration.getCeph().getLowCodeFileBucket(), ceph.getUrl());
    return fileLowcodeCeph;
  }

  public Ceph getExcerptCeph() {
    if (excerptCeph != null) {
      return excerptCeph;
    }

    excerptCeph =
        OrchestratorServiceProvider.getCephService(
            orchestratorClient, configuration.getCeph().getExcerptBucket(), ceph.getUrl());
    return excerptCeph;
  }

  public Ceph getDsoCertCeph() {
    if (dsoCertCeph != null) {
      return dsoCertCeph;
    }

    dsoCertCeph =
        OrchestratorServiceProvider.getCephService(
            orchestratorClient, configuration.getCeph().getDsoCertBucket(), ceph.getUrl());
    return dsoCertCeph;
  }

  public Db getCitusMaster() {
    if (citusMaster != null) {
      return citusMaster;
    }

    citusMaster =
        OrchestratorServiceProvider.getDbService(
            orchestratorClient, configuration.getCitusMaster());
    return citusMaster;
  }

  public Db getCitusReplica() {
    if (citusReplica != null) {
      return citusReplica;
    }

    citusReplica =
        OrchestratorServiceProvider.getDbService(
            orchestratorClient, configuration.getCitusReplica());
    return citusReplica;
  }

  public User getCitusAdminRole() {
    if (citusAdminRole != null) {
      return citusAdminRole;
    }

    citusAdminRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.ADMIN_ROLE.getRoleName());
    return citusAdminRole;
  }

  public User getCitusApplicationRole() {
    if (citusApplicationRole != null) {
      return citusApplicationRole;
    }

    citusApplicationRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.APPLICATION_ROLE.getRoleName());
    return citusApplicationRole;
  }

  public User getCitusRegistryOwnerRole() {
    if (citusRegistryOwnerRole != null) {
      return citusRegistryOwnerRole;
    }

    citusRegistryOwnerRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.REGISTRY_OWNER_ROLE.getRoleName());
    return citusRegistryOwnerRole;
  }

  public User getCitusSettingsRole() {
    if (citusSettingsRole != null) {
      return citusSettingsRole;
    }

    citusSettingsRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.SETTINGS_ROLE.getRoleName());
    return citusSettingsRole;
  }

  public User getCitusAuditRole() {
    if (citusAuditRole != null) {
      return citusAuditRole;
    }

    citusAuditRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.AUDIT_ROLE.getRoleName());
    return citusAuditRole;
  }

  public User getCitusAnalyticsRoleRole() {
    if (citusAnalyticsRole != null) {
      return citusAnalyticsRole;
    }

    citusAnalyticsRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.ANALYTICS_ROLE.getRoleName());
    return citusAnalyticsRole;
  }

  public User getCitusExcerptExportedRole() {
    if (citusExcerptExportedRole != null) {
      return citusExcerptExportedRole;
    }

    citusExcerptExportedRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.EXCERPT_EXPORTER_ROLE.getRoleName());
    return citusExcerptExportedRole;
  }

  public User getCitusExcerptRole() {
    if (citusExcerptRole != null) {
      return citusExcerptRole;
    }

    citusExcerptRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.EXCERPT_ROLE.getRoleName());
    return citusExcerptRole;
  }

  public User getCitusExcerptWorkerRole() {
    if (citusExcerptWorkerRole != null) {
      return citusExcerptWorkerRole;
    }

    citusExcerptWorkerRole =
        OrchestratorServiceProvider.getUserSecretsBySecretNameAndKey(
            orchestratorClient,
            configuration.getCitusRoles().getSecret(),
            CitusUsers.EXCERPT_WORKER_ROLE.getRoleName());
    return citusExcerptWorkerRole;
  }

  public Service getDataFactory(String userName) {
    if (dataFactory != null) {
      dataFactory.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return dataFactory;
    }

    dataFactory =
        getService(
            orchestratorClient, configuration.getDataFactory(), registryUserProvider.get(userName));
    return dataFactory;
  }

  public Service getDataFactoryExternalPlatform(String userName) {
    if (dataFactoryExternalPlatform != null) {
      dataFactoryExternalPlatform.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return dataFactoryExternalPlatform;
    }

    dataFactoryExternalPlatform =
        getService(
            orchestratorClient,
            configuration.getDataFactoryExternalPlatform(),
            registryUserProvider.get(userName));
    return dataFactoryExternalPlatform;
  }

  public Service getRegistryManagement(String userName) {
    if (registryManagement != null) {
      registryManagement.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return registryManagement;
    }

    registryManagement =
        getService(
            orchestratorClient,
            configuration.getRegistryManagement(),
            registryUserProvider.get(userName));
    return registryManagement;
  }

  public Service getDataFactoryExternalSystem() {
    if (dataFactoryExternalSystem != null) {
      return dataFactoryExternalSystem;
    }

    dataFactoryExternalSystem =
        getService(orchestratorClient, configuration.getDataFactoryExternalSystem());
    return dataFactoryExternalSystem;
  }

  public Service getDataFactoryPublicApiSystem() {
    if (dataFactoryPublicApiSystem != null) {
      return dataFactoryPublicApiSystem;
    }

    dataFactoryPublicApiSystem =
        getService(orchestratorClient, configuration.getDataFactoryPublicApiSystem());
    return dataFactoryPublicApiSystem;
  }

  public Service getDataFactorySoap(String userName) {
    if (dataFactorySoap != null) {
      dataFactorySoap.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return dataFactorySoap;
    }

    dataFactorySoap =
        getService(
            orchestratorClient,
            configuration.getDataFactorySoap(),
            registryUserProvider.get(userName));

    String dataFactorySoapUrl = dataFactorySoap.getUrl();

    if (!dataFactorySoapUrl.endsWith("/")) {
      dataFactorySoapUrl += "/";
    }

    dataFactorySoap.setUrl(dataFactorySoapUrl + "ws?wsdl");

    return dataFactorySoap;
  }

  public Service getDigitalSignatureOps(String userName) {
    if (digitalSignatureOps != null) {
      digitalSignatureOps.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return digitalSignatureOps;
    }

    var dsoConfig = configuration.getDigitalSignature();
    digitalSignatureOps =
        getService(orchestratorClient, dsoConfig, registryUserProvider.get(userName));
    return digitalSignatureOps;
  }

  public Service getUserSettings(String userName) {
    if (userSettings != null) {
      userSettings.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return userSettings;
    }

    userSettings =
        getService(
            orchestratorClient,
            configuration.getUserSettings(),
            registryUserProvider.get(userName));
    return userSettings;
  }

  public Service getBpms(String userName) {
    if (bpms != null) {
      bpms.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return bpms;
    }

    var bpmsConfig = configuration.getBpms();

    bpms = getService(orchestratorClient, bpmsConfig, registryUserProvider.get(userName));
    return bpms;
  }

  public Service getFormManagementModeler(String userName) {
    if (formManagementModeler != null) {
      formManagementModeler.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return formManagementModeler;
    }

    formManagementModeler =
        getService(
            orchestratorClient,
            configuration.getFormManagementModeler(),
            registryUserProvider.get(userName));
    return formManagementModeler;
  }

  public Service getProcessWebserviceGateway(String userName) {
    return getProcessWebService(userName);
  }

  public Service getProcessWebserviceGatewayTrembita(String userName) {
    return getProcessWebService(userName);
  }

  private Service getProcessWebService(String userName) {
    if (processWebserviceGateway != null) {
      processWebserviceGateway.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return processWebserviceGateway;
    }

    processWebserviceGateway =
        getService(
            orchestratorClient,
            configuration.getProcessWebserviceGateway(),
            registryUserProvider.get(userName));
    processWebserviceGateway.setUrl(processWebserviceGateway.getUrl());

    return processWebserviceGateway;
  }

  public Service getFormManagementProvider(String userName) {
    if (formManagementProvider != null) {
      formManagementProvider.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return formManagementProvider;
    }

    formManagementProvider =
        getService(
            orchestratorClient,
            configuration.getFormManagementProvider(),
            registryUserProvider.get(userName));
    return formManagementProvider;
  }

  public Service getExcerpt(String userName) {
    if (excerpt != null) {
      excerpt.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return excerpt;
    }

    excerpt =
        getService(
            orchestratorClient, configuration.getExcerpt(), registryUserProvider.get(userName));
    return excerpt;
  }

  public Service getUserTaskManagement() {
    if (userTaskManagement != null) {
      return userTaskManagement;
    }

    userTaskManagement = getService(orchestratorClient, configuration.getUserTaskManagement());
    return userTaskManagement;
  }

  public Service getUserProcessManagement() {
    if (userProcessManagement != null) {
      return userProcessManagement;
    }

    userProcessManagement =
        getService(orchestratorClient, configuration.getUserProcessManagement());
    return userProcessManagement;
  }

  public Service getDigitalDocument() {
    if (digitalDocument != null) {
      return digitalDocument;
    }

    digitalDocument = getService(orchestratorClient, configuration.getDigitalDocument());
    return digitalDocument;
  }

  public Service getOfficerPortal() {
    if (officerPortal != null) {
      return officerPortal;
    }

    officerPortal = getService(orchestratorClient, configuration.getOfficerPortal());
    return officerPortal;
  }

  public Service getCitizenPortal() {
    if (citizenPortal != null) {
      return citizenPortal;
    }

    citizenPortal = getService(orchestratorClient, configuration.getCitizenPortal());
    return citizenPortal;
  }

  public Service getAdminPortal() {
    if (adminPortal != null) {
      return adminPortal;
    }

    adminPortal = getService(orchestratorClient, configuration.getAdminPortal());
    return adminPortal;
  }

  public Service getProcessHistory() {
    if (processHistory != null) {
      return processHistory;
    }

    processHistory = getService(orchestratorClient, configuration.getProcessHistory());
    return processHistory;
  }

  public Service getRedashViewer() {
    if (redashViewer != null) {
      return redashViewer;
    }

    redashViewer = getService(orchestratorClient, configuration.getRedashViewer());

    String apiPath = REPORTS_API;
    String apiKeySuffix = "viewer-api-key";

    String locale = "ua".equals(local) ? "uk" : local;
    if (!Objects.equals(release, "1.9.8")) {
      apiPath = "/reports/" + locale + "/api";
      apiKeySuffix += "-" + locale;
    }

    redashViewer.setUrl(redashViewer.getUrl() + apiPath);
    redashViewer.setToken(
        getPasswordFromSecretByKey(
            orchestratorClient, configuration.getRedashViewer().getSecret(), apiKeySuffix));

    return redashViewer;
  }

  public Service getRedashAdmin() {
    if (redashAdmin != null) {
      return redashAdmin;
    }

    redashAdmin = getService(orchestratorClient, configuration.getRedashAdmin());
    redashAdmin.setUrl(redashAdmin.getUrl() + REPORTS_API);
    redashAdmin.setToken(
        getPasswordFromSecretByKey(
            orchestratorClient, configuration.getRedashAdmin().getSecret(), "admin-api-key"));
    return redashAdmin;
  }

  public Service getGerrit() {
    if (gerrit != null) {
      return gerrit;
    }

    gerrit =
        getService(
            orchestratorClient,
            configuration.getGerrit(),
            orchestratorClient.getCredentials(configuration.getGerrit().getSecret()));

    if (!gerrit.getUrl().endsWith("/")) {
      gerrit.setUrl(gerrit.getUrl() + "/");
    }
    return gerrit;
  }

  // We intentionally catch NPE here to detect missing Jenkins configuration and fallback to Tekton
  @SuppressWarnings("java:S1166")
  public Service getJenkins() {
    if (jenkins != null) {
      return jenkins;
    }

    try {
      User user = orchestratorClient.getCredentials(configuration.getJenkins().getSecret());
      jenkins = getService(orchestratorClient, configuration.getJenkins(), user);
      return jenkins;
    } catch (NullPointerException e) {
      log.info("Jenkins was not found. Setting cicdTool to 'tekton'.");
      cicdTool = "tekton";
      return null;
    }
  }

  public Service getNexus() {
    if (nexus != null) {
      return nexus;
    }
    User user = orchestratorClient.getCredentials(configuration.getNexus().getSecret());
    nexus = getService(orchestratorClient, configuration.getNexus(), user);
    return nexus;
  }

  public Service getFormSchemaProvider(String userName) {
    if (formSchemaProvider != null) {
      formSchemaProvider.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return formSchemaProvider;
    }

    formSchemaProvider =
        getService(
            orchestratorClient,
            configuration.getFormSchemaProvider(),
            registryUserProvider.get(userName));
    return formSchemaProvider;
  }

  public Service getWiremock() {
    if (wiremock != null) {
      return wiremock;
    }

    wiremock =
        OrchestratorServiceProvider.getService(orchestratorClient, configuration.getWiremock());
    return wiremock;
  }

  public Redis getRedis() {
    return getRedis(false);
  }

  public Redis getRedis(boolean isReinit) {
    if (redis != null && !isReinit) {
      return redis;
    }

    redis =
        OrchestratorServiceProvider.getRedisService(
            orchestratorClient,
            configuration.getRedis(),
            orchestratorClient.getCredentialsWithoutLogin(configuration.getRedis().getSecret()));
    return redis;
  }

  public List<Redis> getRedisList() {
    return getRedisList(false);
  }

  public List<Redis> getRedisList(boolean isReinit) {
    if (redis != null && !isReinit) {
      return redisServices;
    }

    return OrchestratorServiceProvider.getRedisServices(
        orchestratorClient,
        configuration.getRedis(),
        orchestratorClient.getCredentialsWithoutLogin(configuration.getRedis().getSecret()));
  }

  public Service getNotificationService() {
    if (notificationService != null) {
      return notificationService;
    }

    notificationService = getService(orchestratorClient, configuration.getNotificationService());
    return notificationService;
  }

  public void setLocalizationFilePath(String newLocalizationFilePath) {
    this.localizationFilePath = newLocalizationFilePath;
  }

  public String getLocalizationDataForSelectedLanguageByKey(String localizationKey) {
    return new LocalizationUtils()
        .getLocalizationDataForSelectedLanguageByKey(localizationFilePath, local, localizationKey);
  }

  public String getLocalizationDataForSelectedLanguageByPath(
      String localizationKey, String... pathSegments) {
    return new LocalizationUtils()
        .getLocalizationDataForSelectedLanguageByPath(
            localizationFilePath, local, localizationKey, pathSegments);
  }

  public List<String> getLocalizationDataForAllLanguages(String localizationKey) {
    return new LocalizationUtils()
        .getLocalizationDataForAllLanguages(localizationFilePath, localizationKey);
  }

  public Service getForSubmissionValidation(String userName) {
    if (formSubmissionValidation != null) {
      formSubmissionValidation.setUser(
          registryUserProvider
              .getUserService()
              .refreshUserToken(registryUserProvider.get(userName)));
      return formSubmissionValidation;
    }

    var formSubmissionValidationConfig = configuration.getFormSubmissionValidation();

    formSubmissionValidation =
        getService(
            orchestratorClient, formSubmissionValidationConfig, registryUserProvider.get(userName));
    return formSubmissionValidation;
  }

  public String getLocalizationDataFromFlatJsonForSelectedLanguageByKey(String localizationKey) {
    return new LocalizationUtils()
            .getLocalizationDataFromFlatJsonForSelectedLanguageByKey(localizationFilePath, local, localizationKey);
  }
}
