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

import com.danubetech.keyformats.crypto.PrivateKeySigner;
import com.danubetech.keyformats.crypto.PublicKeyVerifier;
import com.danubetech.keyformats.crypto.impl.RSA_PS256_PrivateKeySigner;
import com.danubetech.keyformats.crypto.impl.RSA_PS256_PublicKeyVerifier;
import com.danubetech.verifiablecredentials.CredentialSubject;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.LdProof;
import info.weboftrust.ldsignatures.jsonld.LDSecurityKeywords;
import info.weboftrust.ldsignatures.signer.JsonWebSignature2020LdSigner;
import info.weboftrust.ldsignatures.verifier.JsonWebSignature2020LdVerifier;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class GxfsSignerService {
    private final Logger logger = LoggerFactory.getLogger(GxfsSignerService.class);

    private final ObjectMapper mapper;

    public GxfsSignerService(@Autowired ObjectMapper mapper) {
        this.mapper = mapper;
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Given a credential subject, issuer and credential id, create a corresponding unsigned verifiable credential.
     *
     * @param credentialSubject credential subject to wrap
     * @param issuer issuer of the credential
     * @param id id of the credential
     * @return verifiable credential
     * @throws CredentialPresentationException error during creation of credential
     */
    public ExtendedVerifiableCredential createVerifiableCredential(PojoCredentialSubject credentialSubject,
                                                                   URI issuer,
                                                                   URI id) throws CredentialPresentationException {
        try {
            CredentialSubject cs = CredentialSubject.fromJson(mapper.writeValueAsString(credentialSubject));
            return ExtendedVerifiableCredential.fromMap(VerifiableCredential
                    .builder()
                    .id(id)
                    .issuanceDate(Date.from(Instant.now()))
                    .credentialSubject(cs)
                    .issuer(issuer)
                    .build().getJsonObject());
        } catch (JsonProcessingException e) {
            throw new CredentialPresentationException(e.getMessage());
        }
    }

    /**
     * Given a list of verifiable credentials and a presentation ID, wrap it in an unsigned verifiable presentation.
     *
     * @param vcs list of credential subjects to wrap
     * @param id id of the presentation
     */
    public ExtendedVerifiablePresentation createVerifiablePresentation(List<ExtendedVerifiableCredential> vcs,
                                                                       URI id) {
        ExtendedVerifiablePresentation vp = ExtendedVerifiablePresentation.fromMap(VerifiablePresentation
                .builder()
                .id(id)
                .build().getJsonObject());
        vp.setVerifiableCredentials(vcs);
        return vp;
    }

    /**
     * Given a verifiable credential, sign it with the provided private key and verification method.
     * If a non-empty list of certificates is given, also check if any of them match the private key.
     *
     * @param vc credential to sign
     * @param verificationMethod method for signing
     * @param prk private key for signature
     * @param certs certificates to check the signature against (if empty list, skip check)
     * @throws CredentialSignatureException exception during the signature of the vp
     */
    public void signVerifiableCredential(VerifiableCredential vc,
                                           String verificationMethod,
                                           PrivateKey prk,
                                           List<X509Certificate> certs) throws CredentialSignatureException {
        try {
            logger.debug("Signing VC");
            LdProof vcProof = sign(vc, verificationMethod, prk);
            check(vc, vcProof, certs);
            vc.setJsonObjectKeyValue("proof", vc.getLdProof().getJsonObject());
            logger.debug("Signed");
        } catch (IOException | GeneralSecurityException | JsonLDException e) {
            throw new CredentialSignatureException(e.getMessage());
        }
    }

    /**
     * Given a verifiable presentation, sign it with the provided private key and verification method.
     * If a non-empty list of certificates is given, also check if any of them match the private key.
     *
     * @param vp presentation to sign
     * @param verificationMethod method for signing
     * @param prk private key for signature
     * @param certs certificates to check the signature against (if empty list, skip check)
     * @throws CredentialSignatureException exception during the signature of the vp
     */
    public void signVerifiablePresentation(VerifiablePresentation vp,
                                           String verificationMethod,
                                           PrivateKey prk,
                                           List<X509Certificate> certs) throws CredentialSignatureException {
        try {
            logger.debug("Signing VP");
            LdProof vpProof = sign(vp, verificationMethod, prk);
            check(vp, vpProof, certs);
            vp.setJsonObjectKeyValue("proof", vp.getLdProof().getJsonObject());
            logger.debug("Signed");
        } catch (IOException | GeneralSecurityException | JsonLDException e) {
            throw new CredentialSignatureException(e.getMessage());
        }
    }

    /**
     * Given a credential, add a signature with the provided private key and verification method.
     *
     * @param credential credential to sign
     * @param verificationMethod method for signing
     * @param prk private key for signature
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws JsonLDException          JsonLDException
     */
    private LdProof sign(JsonLDObject credential, String verificationMethod, PrivateKey prk)
            throws IOException, GeneralSecurityException, JsonLDException {
        KeyPair kp = new KeyPair(null, prk);
        PrivateKeySigner<?> privateKeySigner = new RSA_PS256_PrivateKeySigner(kp);

        JsonWebSignature2020LdSigner signer = new JsonWebSignature2020LdSigner(privateKeySigner);

        signer.setCreated(new Date());
        signer.setProofPurpose(LDSecurityKeywords.JSONLD_TERM_ASSERTIONMETHOD);
        signer.setVerificationMethod(URI.create(verificationMethod));

        return signer.sign(credential);
    }

    /**
     * Given a credential and proof, check if the signature is valid.
     *
     * @param credential credential to check
     * @param proof      proof
     * @param certs      certificates to validate the proof against
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws JsonLDException          JsonLDException
     */
    private void check(JsonLDObject credential, LdProof proof, List<X509Certificate> certs)
            throws IOException, GeneralSecurityException, JsonLDException {
        boolean certificateMatches = false;
        for (X509Certificate cert : certs) {
            PublicKey puk = cert.getPublicKey();
            PublicKeyVerifier<?> pkVerifier = new RSA_PS256_PublicKeyVerifier((RSAPublicKey) puk);
            JsonWebSignature2020LdVerifier verifier = new JsonWebSignature2020LdVerifier(pkVerifier);
            certificateMatches |= verifier.verify(credential, proof);
        }
        if (!certs.isEmpty() && !certificateMatches) {
            throw new GeneralSecurityException("No matching certificates for this signature.");
        }
    }
}
