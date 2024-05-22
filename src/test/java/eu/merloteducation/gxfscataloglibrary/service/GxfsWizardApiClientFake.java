package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

public class GxfsWizardApiClientFake implements GxfsWizardApiClient {

    private Map<String, Map<String, List<String>>> shapesByEcosystem = Map.of(
            "ecosystem1", Map.of(
                    "Participant", List.of("Participant1.json"),
                    "Service", List.of("Offering1.json", "Offering2.json")),
            "ecosystem2", Map.of(
                    "Participant", List.of("Participant2.json"),
                    "Service", List.of("Offering3.json", "Offering4.json")));

    private Set<String> getShapeFiles() {
        Set<String> shapeFiles = new HashSet<>();
        for (String ecoSystem : shapesByEcosystem.keySet()) {
            for (String shapeType : shapesByEcosystem.get(ecoSystem).keySet()) {
                shapeFiles.addAll(shapesByEcosystem.get(ecoSystem).get(shapeType));
            }
        }
        return shapeFiles;
    }

    @Override
    public Map<String, List<String>> getAvailableShapesCategorized(String ecoSystem) {
        if (!shapesByEcosystem.containsKey(ecoSystem)) {
            throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "missing", null, null, null);
        }
        return shapesByEcosystem.get(ecoSystem);
    }

    @Override
    public String getJSON(String ecosystem, String name) {
        if (!getShapeFiles().contains(name)) {
            throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "missing", null, null, null);
        }
        return """
                {
                    "someKey": "someValue"
                }
                """;
    }
}
