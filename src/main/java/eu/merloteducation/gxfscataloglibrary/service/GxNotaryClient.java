package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

// based on https://registrationnumber.notary.lab.gaia-x.eu/v1/docs/
public interface GxNotaryClient {

    // Registration Number VC
    @PostExchange("/registrationNumberVC")
    VerifiableCredential postRegistrationNumber(
            @RequestParam(name = "vcid", required = false) String vcid,
            @RequestBody GxLegalRegistrationNumberCredentialSubject body // RegistrationNumber formatted object as specified by the service-characteristics. Must also contain the participantID in the corresponding format.
    );
}
