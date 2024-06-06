package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GxfsWizardApiService {

    @Autowired
    private GxfsWizardApiClient gxfsWizardApiClient;

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
