package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.keyformats.crypto.PrivateKeySigner;
import com.danubetech.keyformats.crypto.PublicKeyVerifier;
import com.danubetech.keyformats.crypto.impl.RSA_PS256_PrivateKeySigner;
import com.danubetech.keyformats.crypto.impl.RSA_PS256_PublicKeyVerifier;
import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionCredentialSubject;
import foundation.identity.jsonld.JsonLDException;
import foundation.identity.jsonld.JsonLDObject;
import info.weboftrust.ldsignatures.LdProof;
import info.weboftrust.ldsignatures.jsonld.LDSecurityKeywords;
import info.weboftrust.ldsignatures.signer.JsonWebSignature2020LdSigner;
import info.weboftrust.ldsignatures.verifier.JsonWebSignature2020LdVerifier;
import io.netty.util.internal.StringUtil;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
public class GxfsSignerService {
    private final Logger logger = LoggerFactory.getLogger(GxfsSignerService.class);
    private final PrivateKey prk;
    private final List<X509Certificate> certs;

    public GxfsSignerService(@Value("${gxfscatalog.cert-path}") String certPath,
                             @Value("${gxfscatalog.private-key-path}") String privateKeyPath) throws IOException, CertificateException {
        try (InputStream privateKeyStream = StringUtil.isNullOrEmpty(privateKeyPath) ?
                GxfsSignerService.class.getClassLoader().getResourceAsStream("prk.ss.pem")
                : new FileInputStream(privateKeyPath)) {
            PEMParser pemParser = new PEMParser(new InputStreamReader(privateKeyStream));
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());
            prk = converter.getPrivateKey(privateKeyInfo);
        }

        try (InputStream publicKeyStream = StringUtil.isNullOrEmpty(certPath) ?
                GxfsSignerService.class.getClassLoader().getResourceAsStream("cert.ss.pem")
                : new FileInputStream(certPath)) {
            String certString = new String(publicKeyStream.readAllBytes(), StandardCharsets.UTF_8);
            ByteArrayInputStream certStream = new ByteArrayInputStream(certString.getBytes(StandardCharsets.UTF_8));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            certs = (List<X509Certificate>) certFactory.generateCertificates(certStream);
        }
    }

    /**
     * Given a credential subject and an issuer, wrap it in an unsigned verifiable presentation.
     *
     * @param credentialSubject credential subject to wrap
     * @param issuer issuer of the presentation
     * @throws JsonProcessingException json mapping exception
     */
    public VerifiablePresentation presentVerifiableCredential(SelfDescriptionCredentialSubject credentialSubject,
                                              String issuer) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String credentialSubjectJson = mapper.writeValueAsString(credentialSubject);
        return VerifiablePresentation.fromJson("""
            {
                "@context": ["https://www.w3.org/2018/credentials/v1"],
                "@id": "http://example.edu/verifiablePresentation/self-description1",
                "type": ["VerifiablePresentation"],
                "verifiableCredential": {
                    "@context": ["https://www.w3.org/2018/credentials/v1"],
                    "@id": "https://www.example.org/ServiceOffering.json",
                    "@type": ["VerifiableCredential"],
                    "issuer": \"""" + issuer + """
            ",
            "issuanceDate": \"""" + OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT) + """
            ",
            "credentialSubject":\s""" + credentialSubjectJson + """
                }
            }
            """);
    }

    /**
     * Given a verifiable presentation, sign it with the key provided to the service.
     *
     * @param vp presentation to sign
     * @throws JsonLDException signature/check exception
     * @throws GeneralSecurityException signature/check exception
     * @throws IOException signature/check exception
     */
    public void signVerifiablePresentation(VerifiablePresentation vp)
            throws JsonLDException, GeneralSecurityException, IOException {
        Security.addProvider(new BouncyCastleProvider());
        VerifiableCredential vc = vp.getVerifiableCredential();

        logger.debug("Signing VC");
        LdProof vcProof = sign(vc);
        check(vc, vcProof);
        logger.debug("Signed");

        vc.setJsonObjectKeyValue("proof", vc.getLdProof().getJsonObject());
        vp.setJsonObjectKeyValue("verifiableCredential", vc.getJsonObject());

        logger.debug("Signing VP");
        LdProof vpProof = sign(vp);
        check(vp, vpProof);
        logger.debug("Signed");

        vp.setJsonObjectKeyValue("proof", vp.getLdProof().getJsonObject());
    }

    private LdProof sign(JsonLDObject credential)
            throws IOException, GeneralSecurityException, JsonLDException {
        KeyPair kp = new KeyPair(null, prk);
        PrivateKeySigner<?> privateKeySigner = new RSA_PS256_PrivateKeySigner(kp);

        JsonWebSignature2020LdSigner signer = new JsonWebSignature2020LdSigner(privateKeySigner);

        signer.setCreated(new Date());
        signer.setProofPurpose(LDSecurityKeywords.JSONLD_TERM_ASSERTIONMETHOD);
        signer.setVerificationMethod(URI.create("did:web:merlot-education.eu"));

        return signer.sign(credential);
    }

    /**
     * Given a credential and proof, check if the signature is valid.
     *
     * @param credential credential to check
     * @param proof      proof
     * @throws IOException              IOException
     * @throws GeneralSecurityException GeneralSecurityException
     * @throws JsonLDException          JsonLDException
     */
    private void check(JsonLDObject credential, LdProof proof)
            throws IOException, GeneralSecurityException, JsonLDException {
        for (X509Certificate cert : certs) {
            PublicKey puk = cert.getPublicKey();
            PublicKeyVerifier<?> pkVerifier = new RSA_PS256_PublicKeyVerifier((RSAPublicKey) puk);
            JsonWebSignature2020LdVerifier verifier = new JsonWebSignature2020LdVerifier(pkVerifier);
            verifier.verify(credential, proof);
        }
    }
}
