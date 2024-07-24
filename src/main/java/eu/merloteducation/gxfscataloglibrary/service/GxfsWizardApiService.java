/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GxfsWizardApiService {
    private final GxfsWizardApiClient gxfsWizardApiClient;

    public GxfsWizardApiService(@Autowired GxfsWizardApiClient gxfsWizardApiClient) {
        this.gxfsWizardApiClient = gxfsWizardApiClient;
    }

    /**
     * Return the map of shape-type:filename for a given ecosystem.
     *
     * @param ecosystem ecosystem to filter for
     * @return map of filenames as described above
     */
    public Map<String, List<String>> getShapesByEcosystem(String ecosystem) {
        return gxfsWizardApiClient.getAvailableShapesCategorized(ecosystem);
    }

    /**
     *  Return a list of service offering shape files for the given ecosystem.
     *
     * @param ecosystem ecosystem to filter for
     * @return list of service offering shape JSON files
     */
    public List<String> getServiceOfferingShapesByEcosystem(String ecosystem) {
        return gxfsWizardApiClient.getAvailableShapesCategorized(ecosystem).get("Service");
    }

    /**
     *  Return a list of participant shape files for the given ecosystem.
     *
     * @param ecosystem ecosystem to filter for
     * @return list of participant shape JSON files
     */
    public List<String> getParticipantShapesByEcosystem(String ecosystem) {
        return gxfsWizardApiClient.getAvailableShapesCategorized(ecosystem).get("Participant");
    }

    /**
     * Given a JSON file name, return the corresponding JSON shape file
     *
     * @param ecosystem ecosystem of shape
     * @param jsonName JSON file name
     * @return JSON file
     */
    public String getShapeByName(String ecosystem, String jsonName) {
        return gxfsWizardApiClient.getJSON(ecosystem, jsonName);
    }
}
