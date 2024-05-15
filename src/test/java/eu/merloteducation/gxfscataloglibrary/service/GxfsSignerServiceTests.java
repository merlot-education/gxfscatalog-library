package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionCredentialSubject;
import io.netty.util.internal.StringUtil;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class GxfsSignerServiceTests {

    private SelfDescriptionCredentialSubject generateCredentialSubject() {
        SelfDescriptionCredentialSubject subject = new SelfDescriptionCredentialSubject();
        subject.setId("did:web:subject.example.com");
        subject.setType("context:type");
        subject.setContext(Map.of("context", "http://example.com"));
        return subject;
    }

    @Test
    void loadExternalCertificates() throws
            CredentialPresentationException, CredentialSignatureException {
        GxfsSignerService gxfsSignerService = new GxfsSignerService(new ObjectMapper());
        SelfDescriptionCredentialSubject cs = generateCredentialSubject();
        VerifiableCredential vc = gxfsSignerService.createVerifiableCredential(
                cs,
                URI.create("did:web:issuer.example.com"),
                URI.create(cs.getId()));
        gxfsSignerService.signVerifiableCredential(
                vc,
                "did:web:compliance.lab.gaia-x.eu",
                loadPrivateKey(),
                loadCertificates());

        VerifiablePresentation vp =
                gxfsSignerService
                        .createVerifiablePresentation(List.of(vc), vc.getId());
        gxfsSignerService.signVerifiablePresentation(
                vp,
                "did:web:compliance.lab.gaia-x.eu",
                loadPrivateKey(),
                loadCertificates());
        assertNotNull(vp);
    }

    private PrivateKey loadPrivateKey() {
        try (InputStream privateKeyStream =
                     GxfsSignerServiceTests.class.getClassLoader().getResourceAsStream("prk.ss.pem")) {
            PEMParser pemParser = new PEMParser(new InputStreamReader(privateKeyStream));
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());
            return converter.getPrivateKey(privateKeyInfo);
        } catch (IOException ignored) {
            return null;
        }
    }

    private List<X509Certificate> loadCertificates() {
        try (InputStream publicKeyStream =
                     GxfsSignerServiceTests.class.getClassLoader().getResourceAsStream("cert.ss.pem")) {
            String certString = new String(publicKeyStream.readAllBytes(), StandardCharsets.UTF_8);
            ByteArrayInputStream certStream = new ByteArrayInputStream(certString.getBytes(StandardCharsets.UTF_8));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<X509Certificate>) certFactory.generateCertificates(certStream);
        } catch (CertificateException | IOException ignored) {
            return Collections.emptyList();
        }
    }
}
