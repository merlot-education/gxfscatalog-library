package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.List;
import java.util.Map;

public interface GxfsWizardApiClient {
    @GetExchange("/getAvailableShapesCategorized")
    Map<String, List<String>> getAvailableShapesCategorized(@PathVariable String ecoSystem);

    @GetExchange("/getJSON")
    String getJSON(@PathVariable String name);

    // TODO GET "/getAvailableShapes"
    // TODO POST "/convertFile"
    // TODO GET "/getSearchQuery/{ecoSystem}/{query}"
}
