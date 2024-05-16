package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

// based on https://registrationnumber.notary.lab.gaia-x.eu/v1/docs/
public interface GxNotaryClient {

    // Registration Number VC
    @PostExchange("/registrationNumberVC")
    JsonNode postRegistrationNumber(
            @RequestParam(name = "vcid", required = false) String vcid,
            @RequestBody JsonNode body // RegistrationNumber formated object as specified by the service-characteristics. Must also contain the participantID in the corresponding format.
    );
}
