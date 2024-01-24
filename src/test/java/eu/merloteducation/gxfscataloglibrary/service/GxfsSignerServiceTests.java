package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionCredentialSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.security.cert.CertificateException;
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
    void loadExternalCertificates() throws CertificateException, IOException,
            CredentialPresentationException, CredentialSignatureException {
        GxfsSignerService gxfsSignerService = new GxfsSignerService(
                GxfsSignerServiceTests.class.getClassLoader().getResource("cert.ss.pem").getPath(),
                GxfsSignerServiceTests.class.getClassLoader().getResource("prk.ss.pem").getPath());
        VerifiablePresentation vp =
                gxfsSignerService
                        .presentVerifiableCredential(generateCredentialSubject(), "did:web:issuer.example.com");
        gxfsSignerService.signVerifiablePresentation(vp);
        assertNotNull(vp);
    }

    @Test
    void loadNonExistentCertificates() throws CertificateException, IOException, CredentialPresentationException {
        GxfsSignerService gxfsSignerService = new GxfsSignerService(
                "garbage1.ss.pem",
                "garbage2.ss.pem");
        VerifiablePresentation vp =
                gxfsSignerService
                        .presentVerifiableCredential(generateCredentialSubject(), "did:web:issuer.example.com");
        assertThrows(CredentialSignatureException.class, () -> gxfsSignerService.signVerifiablePresentation(vp));
    }
}
