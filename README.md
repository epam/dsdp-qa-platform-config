##Platform qa configuration library

### Overview

* The main purpose of the platform qa configuration library is to provide configuration services abilities to use 
  them inside platform tests;
* implement abilities to work through openshift services information;
* implement usage of port forwarding if it is needed.


### Usage

#### Prerequisites:

* Cluster is installed, configured and running;
* openshift namespace exists.

#### Configuration

Available system properties are following:

* `username` - openshift user login name;
* `password` - openshift user password;
* `cluster` - cluster name;
* `baseDomain` - openshift base domain;
* `url` - openshift url in format `https://api.%s.%s:{openshit port number}`, when library run: first %s will be 
  equal to cluster and second %s will be baseDomain;
  * `users.json` - file inside tests repository into test/java/resources/properties location, users list with roles 
    mapping to the services are following: 
      * **BPMS** service: user with _realm_=-admin; _clientId_=camunda-cockpit; _realmRoles_=camunda-admin;
      * **Form Management Provider** service: user with _realm_=-admin; _clientId_=camunda-cockpit; _realmRoles_=camunda-admin;
      * **Process Webservice Gateway** service: user with _realm_=-admin; _clientId_=camunda-cockpit; _realmRoles_=camunda-admin;
      * **Form Management Modeler** service: user with _realm_=-admin; _clientId_=camunda-cockpit; 
        _realmRoles_=camunda-admin;  
        **Example:**
        ```json
        "auto-user-admin": {
          "login": "auto-admin",
          "realm": "-admin",
          "clientId": "camunda-cockpit",
          "realmRoles": [
            "camunda-admin"
          ],
          "realmClientsDictionary": {
            "admin": [
              {
                "clientId": "camunda-cockpit"
              }
            ]
          },
          "attributes": {}
          }
      * **Data Factory** service: user with _realm_=-officer-portal; _clientId_=officer-portal; 
        _realmRoles_=officer; _RedashGroups_=Registry Officers;
      * **Data Factory External** service: user with _realm_=-officer-portal; _clientId_=officer-portal;
        _realmRoles_=officer; _RedashGroups_=Registry Officers;
      * **Data Factory Soap** service: user with _realm_=-officer-portal; _clientId_=officer-portal;
        _realmRoles_=officer; _RedashGroups_=Registry Officers;
      * **Digital Signature Ops** service: user with _realm_=-officer-portal; _clientId_=officer-portal;
        _realmRoles_=officer; _RedashGroups_=Registry Officers;
      * **Excerpt** service: user with _realm_=-officer-portal; _clientId_=officer-portal;
        _realmRoles_=officer; _RedashGroups_=Registry Officers;                                                    
        **Example:**
        ```json
        "auto-user-data": {
        "login": "auto-user-data",
        "realm": "-officer-portal",
        "clientId": "officer-portal",
        "realmRoles": [
           "officer"
        ],
        "attributes": {
          "drfo": [
            ""
          ],
          "edrpou": [
            ""
          ],
          "fullName": [
            ""
          ],
          "RedashGroups": [
            ""
          ]
         }
        }
      * **Citizen** user                                                                                 
        **Example:**
        ```json
        "auto-user-officer": {
        "login": "auto-user-officer",
        "realm": "-officer-portal",
        "clientId": "officer-portal",
        "realmRoles": [
          "officer",
          "officer-first-rank",
          "officer-second-rank",
          "op-regression"
        ],
        "attributes": {
          "drfo": [
           ""
          ],
          "edrpou": [
           ""
          ],
          "fullName": [
           ""
          ]
        },
        "key": {
         "name": "<key file name located in src/test/java/resources/data/files/keys>",
         "password": "<encoded key pass>",
         "provider": "<key provider>"
         }
        }
      * **Officer** user                                                                                 
        **Example:**
    ```json
    "auto-user-citizen": {
    "login": "auto-user-citizen",
    "realm": "-citizen-portal",
    "clientId": "citizen-portal",
    "realmRoles": [
      "citizen",
      "cp-regression"
    ],
    "attributes": {
      "drfo": [
       ""
      ],
      "fullName": [
       ""
      ],
      "subjectType": [
       ""
      ]
    },
    "key": {
     "name": "<key file name located in src/test/java/resources/data/files/keys>",
     "password": "<encoded key pass>",
     "provider": "<key provider>"
     }
    }
    * `platform.json` - file inside tests repository into test/java/resources/properties location, platform openshift 
      services list mapping to the services are following:
      * "centralConfiguration" - central components configuration:
        * jenkins
        * gerrit
        * ceph
        * keycloak
        * vault
        * controlPlane
      * "registryConfiguration" - registry configuration:
        * jenkins
        * gerrit
        * dataFactory
        * dataFactoryExternalPlatform
        * dataFactoryExternalSystem
        * dataFactorySoap
        * digitalSignature
        * bpms
        * userSettings
        * formManagementProvider
        * formManagementModeler
        * excerpt
        * userTaskManagement
        * userProcessManagement
        * processHistory
        * digitalDocument
        * processWebserviceGateway
        * officerPortal
        * citizenPortal
        * adminPortal
        * redashViewer
        * redashAdmin
        * citusMaster
        * citusReplica
        * citusRoles
        * ceph
        * notificationService
        * formSubmissionValidation
      * `platform.json mapping openshift services example:`
      ```json 
      {
        "centralConfiguration": {
          "jenkins": {
            "podLabel": "pod-label-name",
            "secret": "jenkins-secret-name",
            "namespace": "jenkins-namespace",
            "route": "jenkins-route",
            "portForwarding": false[or true],
            "defaultPort": jenkins-service-port
          },
          "gerrit": {
            "podLabel": "pod-label-name",
            "secret": "gerrit-secret-name",
            "namespace": "gerrit-namespace",
            "route": "gerrit-route",
            "portForwarding": false[or true],
            "defaultPort": gerrit-service-port
          },
          "ceph": {
            "podLabel": "pod-label-name",
            "namespace": "ceph-namespace",
            "route": "ceph-route",
            "portForwarding": false[or true],
            "defaultPort": ceph-service-port
          },
          "keycloak": {
            "podLabel": "pod-label-name",
            "secret": "keycloak-secret-name",
            "namespace": "keycloak-namespace",
            "route": "keycloak-route",
            "portForwarding": false[or true],
            "defaultPort": keycloak-service-port
          },
          "vault": {
            "podLabel": "pod-label-name",
            "secret": "vault-secret-name",
            "namespace": "vault-namespace",
            "route": "vault-route",
            "portForwarding": false[or true],
            "defaultPort": vault-service-port
          },
          "controlPlane": {
            "podLabel": "pod-label-name",
            "namespace": "control-plane-secret-name",
            "route": "control-plane-route",
            "portForwarding": false[or true],
            "defaultPort": control-plane-service-port
          }
        },
        "registryConfiguration": {
          "jenkins": {
            "podLabel": "pod-label-name",
            "secret": "jenkins-secret-name",
            "route": "jenkins-route",
            "portForwarding": false[or true],
            "defaultPort": jenkins-service-port
          },
          "gerrit": {
            "podLabel": "pod-label-name",
            "secret": "gerrit-secret-name",
            "route": "gerrit-route",
            "portForwarding": false[or true],
            "defaultPort": gerrit-service-port
          },
          "dataFactory": {
            "podLabel": "pod-label-name",
            "route": "data-factory-route",
            "portForwarding": false[or true],
            "defaultPort": data-factory-service-port
          },
          "dataFactoryExternalPlatform": {
            "podLabel": "pod-label-name",
            "route": "data-factory-ext-platform-route",
            "portForwarding": false[or true],
            "defaultPort": data-factory-ext-platform-service-port
          },
          "dataFactoryExternalSystem": {
            "podLabel": "pod-label-name",
            "route": "data-factory-ext-system-platform-route",
            "portForwarding": false[or true],
            "defaultPort": data-factory-ext-system-service-port
          },
          "dataFactorySoap": {
            "podLabel": "pod-label-name",
            "route": "data-factory-soap-route",
            "portForwarding": false[or true],
            "defaultPort": data-factory-soap-service-port
          },
          "digitalSignature": {
            "podLabel": "pod-label-name",
            "route": "digital-signature-route",
            "portForwarding": false[or true],
            "defaultPort": digital-signature-service-port
          },
          "bpms": {
            "podLabel": "pod-label-name",
            "route": "business-process-management-system-route",
            "portForwarding": false[or true],
            "defaultPort": business-process-management-system-service-port
          },
          "userSettings": {
            "podLabel": "pod-label-name",
            "route": "user-settings-route",
            "portForwarding": false[or true],
            "defaultPort": user-settings-service-port
          },
          "formManagementProvider": {
            "podLabel": "pod-label-name",
            "route": "form-management-route",
            "portForwarding": false[or true],
            "defaultPort": form-management-service-port
          },
          "formManagementModeler": {
            "podLabel": "pod-label-name",
            "route": "form-management-modeler-route",
            "portForwarding": false[or true],
            "defaultPort": form-management-modeler-service-port
          },
          "excerpt": {
            "podLabel": "pod-label-name",
            "route": "excerpt-route",
            "portForwarding": false[or true],
            "defaultPort": excerpt-service-port
          },
          "userTaskManagement": {
            "podLabel": "pod-label-name",
            "route": "user-task-management-route",
            "portForwarding": false[or true],
            "defaultPort": user-task-management-service-port
          },
          "userProcessManagement": {
            "podLabel": "pod-label-name",
            "route": "user-process-management-route",
            "portForwarding": false[or true],
            "defaultPort": user-process-management-service-port
          },
          "processHistory": {
            "podLabel": "pod-label-name",
            "route": "process-history-route",
            "portForwarding": false[or true],
            "defaultPort": process-history-service-port
          },
          "digitalDocument": {
            "podLabel": "pod-label-name",
            "route": "digital-document-route",
            "portForwarding": false[or true],
            "defaultPort": digital-document-service-port
          },
          "processWebserviceGateway": {
            "podLabel": "pod-label-name",
            "route": "webservice-gateway-route",
            "portForwarding": false[or true],
            "defaultPort": webservice-gateway-service-port
          },
          "officerPortal": {
            "podLabel": "pod-label-name",
            "route": "officer-portal-route",
            "portForwarding": false[or true],
            "defaultPort": officer-portal-service-port
          },
          "citizenPortal": {
            "podLabel": "pod-label-name",
            "route": "citizen-portal-route",
            "portForwarding": false[or true],
            "defaultPort": citizen-portal-service-port
          },
          "adminPortal": {
            "podLabel": "pod-label-name",
            "route": "admin-portal-route",
            "portForwarding": false[or true],
            "defaultPort": admin-portal-service-port
          },
          "redashViewer": {
            "podLabel": "pod-label-name",
            "secret": "redash-secret-name",
            "route": "redash-route",
            "portForwarding": false[or true],
            "defaultPort": redash-service-port
          },
          "redashAdmin": {
            "podLabel": "pod-label-name",
            "secret": "redash-api-secret-name",
            "route": "redash-admin-route",
            "portForwarding": false[or true],
            "defaultPort": redash-api-service-port
          },
          "citusMaster": {
            "podLabel": "pod-label-name",
            "secret": "citus-secret-name",
            "route": "citus-route",
            "portForwarding": false[or true],
            "defaultPort": citus-service-port
          },
          "citusReplica": {
            "podLabel": "pod-label-name",
            "secret": "citus-secret-name",
            "route": "citus-replica-route",
            "portForwarding": false[or true],
            "defaultPort": citus-replica-service-port
          },
          "citusRoles": {
            "secret": "citus-role-secret-name"
          },
          "ceph": {
            "signatureBucket": "sign-bucket-name",
            "dataFileBucket": "file-bucket-name",
            "excerptBucket": "excerpt-bucket-name"
          },
          "redis": {
            "podLabel": "rfr-redis-sentinel",
            "secret": "redis-auth",
            "portForwarding": true,
            "defaultPort": 6379
          },
          "notificationService": {
            "podLabel": "app.kubernetes.io/instance=ddm-notification-service",
            "route": "ddm-notification-service",
            "portForwarding": true,
            "defaultPort": 8080
          },
          "formSubmissionValidation": {
            "podLabel": "app.kubernetes.io/name=form-submission-validation",
            "route": "form-submission-validation",
            "portForwarding": true,
            "defaultPort": 8080
          }
        }
}

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.

### License

The platform-qa-config is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).
