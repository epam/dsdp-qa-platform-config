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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegistryConfiguration {
    private ServiceConfiguration dataFactory;
    private ServiceConfiguration dataFactoryExternalSystem;
    private ServiceConfiguration dataFactoryPublicApiSystem;
    private ServiceConfiguration dataFactoryExternalPlatform;
    private ServiceConfiguration registryManagement;
    private ServiceConfiguration dataFactorySoap;
    private ServiceConfiguration digitalSignature;
    private ServiceConfiguration bpms;
    private ServiceConfiguration userSettings;
    private ServiceConfiguration formManagementProvider;
    private ServiceConfiguration formManagementModeler;
    private ServiceConfiguration formSchemaProvider;
    private ServiceConfiguration excerpt;
    private ServiceConfiguration userTaskManagement;
    private ServiceConfiguration userProcessManagement;
    private ServiceConfiguration digitalDocument;
    private ServiceConfiguration officerPortal;
    private ServiceConfiguration citizenPortal;
    private ServiceConfiguration adminPortal;
    private ServiceConfiguration processHistory;
    private ServiceConfiguration redashViewer;
    private ServiceConfiguration redashAdmin;
    private ServiceConfiguration jenkins;
    private ServiceConfiguration nexus;
    private ServiceConfiguration gerrit;
    private ServiceConfiguration citusMaster;
    private ServiceConfiguration citusReplica;
    private ServiceConfiguration citusRoles;
    private ServiceConfiguration processWebserviceGateway;
    private ServiceConfiguration redis;
    private ServiceConfiguration wiremock;
    private CephBuckets ceph;
    private ServiceConfiguration notificationService;
    private ServiceConfiguration formSubmissionValidation;
}