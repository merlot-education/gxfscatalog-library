package eu.merloteducation.gxfscataloglibrary.service;

import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.service.annotation.PostExchange;

// based on https://compliance.lab.gaia-x.eu/v1-staging/docs
public interface GxComplianceClient {

  // Credential Offer
  @PostExchange("/api/credential-offers")
  ExtendedVerifiableCredential postCredentialOffer(
      @RequestParam(name = "vcid", required = false) String vcid,
      @RequestBody ExtendedVerifiablePresentation body
  );
}
