package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.service.annotation.GetExchange;

// based on https://registry.lab.gaia-x.eu/v1/docs
public interface GxRegistryClient {

    // Terms and Conditions
    @GetExchange("/api/termsAndConditions")
    JsonNode getGxTermsAndConditions();
}
