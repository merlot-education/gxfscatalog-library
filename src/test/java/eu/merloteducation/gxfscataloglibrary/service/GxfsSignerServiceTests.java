/*
 *  Copyright 2023-2024 Dataport AöR
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

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GxfsSignerServiceTests {

    private PojoCredentialSubject generateCredentialSubject() {
        PojoCredentialSubject subject = new PojoCredentialSubject();
        subject.setId("did:web:subject.example.com");
        return subject;
    }

    @Test
    void loadExternalCertificates() throws
            CredentialPresentationException, CredentialSignatureException {
        GxfsSignerService gxfsSignerService = new GxfsSignerService(new ObjectMapper());
        PojoCredentialSubject cs = generateCredentialSubject();
        ExtendedVerifiableCredential vc = gxfsSignerService.createVerifiableCredential(
                cs,
                URI.create("did:web:issuer.example.com"),
                URI.create(cs.getId()));
        gxfsSignerService.signVerifiableCredential(
                vc,
                "did:web:compliance.lab.gaia-x.eu",
                loadPrivateKey(),
                loadCertificates());

        ExtendedVerifiablePresentation vp =
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
