package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.service.annotation.PostExchange;

// based on https://compliance.lab.gaia-x.eu/v1-staging/docs
public interface GxComplianceClient {

  // Credential Offer
  @PostExchange("/api/credential-offers")
  JsonNode postCredentialOffer(
      @RequestParam(name = "vcid", required = false) String vcid,
      @RequestBody VerifiablePresentation body
  );
}
