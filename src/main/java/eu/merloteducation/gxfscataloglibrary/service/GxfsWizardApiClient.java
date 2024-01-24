package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;
import java.util.Map;

public interface GxfsWizardApiClient {
    @GetExchange("/getAvailableShapesCategorized")
    Map<String, List<String>> getAvailableShapesCategorized(@RequestParam(name = "ecoSystem") String ecoSystem);

    @GetExchange("/getJSON")
    String getJSON(@RequestParam(name = "name") String name);

    // TODO GET "/getAvailableShapes"
    // TODO POST "/convertFile"
    // TODO GET "/getSearchQuery/{ecoSystem}/{query}"
}
