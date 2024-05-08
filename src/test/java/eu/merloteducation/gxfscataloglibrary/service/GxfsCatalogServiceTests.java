package eu.merloteducation.gxfscataloglibrary.service;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryLegalNameItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionMeta;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.RegistrationNumber;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.VCard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.participants.GaxTrustLegalPersonCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.serviceofferings.GaxCoreServiceOfferingCredentialSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
@WireMockTest(httpsPort = 8101, httpsEnabled = true)
class GxfsCatalogServiceTests {

    @Autowired
    private GxfsCatalogService gxfsCatalogService;

    @MockBean
    private GxfsCatalogAuthService gxfsCatalogAuthService;

    @MockBean
    private GxfsCatalogClient gxfsCatalogClient;

    @MockBean
    private GxfsWizardApiService gxfsWizardApiService;

    private String privateKey;

    private GaxCoreServiceOfferingCredentialSubject generateOfferingCredentialSubject(String id, String offeredBy) {
        GaxCoreServiceOfferingCredentialSubject credentialSubject = new GaxCoreServiceOfferingCredentialSubject();
        credentialSubject.setType("gax-core:ServiceOffering");
        credentialSubject.setId(id);
        credentialSubject.setOfferedBy(new NodeKindIRITypeId(offeredBy));
        credentialSubject.setContext(new HashMap<>());
        return credentialSubject;
    }

    private GaxTrustLegalPersonCredentialSubject generateParticipantCredentialSubject(String id, String name) {
        GaxTrustLegalPersonCredentialSubject credentialSubject = new GaxTrustLegalPersonCredentialSubject();
        credentialSubject.setType("gax-trust-framework:LegalPerson");
        credentialSubject.setId(id);
        credentialSubject.setRegistrationNumber(new RegistrationNumber());
        credentialSubject.getRegistrationNumber().setLocal("12345");
        credentialSubject.setLegalName(name);
        VCard address = new VCard();
        address.setCountryName("DE");
        address.setStreetAddress("Some Street 3");
        address.setLocality("Berlin");
        address.setPostalCode("12345");
        credentialSubject.setHeadquarterAddress(address);
        credentialSubject.setLegalAddress(address);
        return credentialSubject;
    }


    @BeforeEach
    public void setUp() throws SSLException {
        // reset catalog client fake between each test
        ReflectionTestUtils.setField(gxfsCatalogService, "gxfsCatalogClient", new GxfsCatalogClientFake());

        String didJson = "";
        try (InputStream didStream =
                     GxfsCatalogServiceTests.class.getClassLoader().getResourceAsStream("exampledid.json")) {
            didJson = new String(didStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
        stubFor(get("/1234/did.json")
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(didJson)));

        String cert = "";
        try (InputStream certStream =
                     GxfsCatalogServiceTests.class.getClassLoader().getResourceAsStream("cert.ss.pem")) {
            cert = new String(certStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
        stubFor(get("/1234/somecert.pem")
                .willReturn(ok()
                        .withBody(cert)));
        stubFor(get("/.well-known/cert.pem")
            .willReturn(ok()
                .withBody(cert)));

        privateKey = "";
        try (InputStream prkStream =
                     GxfsCatalogServiceTests.class.getClassLoader().getResourceAsStream("prk.ss.pem")) {
            privateKey = new String(prkStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    @Test
    void revokeValidSelfDescriptionByHash() throws Exception {
        String offeringId = "1234";
        SelfDescriptionMeta meta =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject(offeringId, "2345"));

        GXFSCatalogListResponse<SelfDescriptionItem> items =
                gxfsCatalogService.getSelfDescriptionsByIds(new String[]{meta.getId()});
        assertEquals(1, items.getTotalCount());
        assertEquals(SelfDescriptionStatus.ACTIVE.getValue(), items.getItems().get(0).getMeta().getStatus());

        meta = gxfsCatalogService.revokeSelfDescriptionByHash(meta.getSdHash());
        assertNotNull(meta);
        assertEquals(offeringId, meta.getId());
        assertEquals(SelfDescriptionStatus.REVOKED.getValue(), meta.getStatus());
    }

    @Test
    void revokeNonExistentSelfDescriptionByHash()  {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.revokeSelfDescriptionByHash("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void revokeSelfDescriptionByHashCatalogError()  {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.revokeSelfDescriptionByHash("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }

    @Test
    void deleteValidSelfDescriptionByHash() throws Exception {
        String offeringId = "1234";
        SelfDescriptionMeta meta =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject(offeringId, "2345"));

        GXFSCatalogListResponse<SelfDescriptionItem> items =
                gxfsCatalogService.getSelfDescriptionsByIds(new String[]{meta.getId()});
        assertEquals(1, items.getTotalCount());
        assertEquals(SelfDescriptionStatus.ACTIVE.getValue(), items.getItems().get(0).getMeta().getStatus());

        gxfsCatalogService.deleteSelfDescriptionByHash(meta.getSdHash());

        items =
                gxfsCatalogService.getSelfDescriptionsByIds(new String[]{meta.getId()});
        assertEquals(0, items.getTotalCount());
    }

    @Test
    void deleteNonExistentSelfDescriptionByHash()  {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.deleteSelfDescriptionByHash("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void deleteSelfDescriptionByHashCatalogError()  {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.deleteSelfDescriptionByHash("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }

    @Test
    void getExistingSelfDescriptionsByIds() throws Exception {
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"));
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("2", "2345"));
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("3", "2345"));

        GXFSCatalogListResponse<SelfDescriptionItem> items =
                gxfsCatalogService.getSelfDescriptionsByIds(new String[]{"1", "2"});
        assertEquals(2, items.getTotalCount());
    }

    @Test
    void getExistingRevokedSelfDescriptionsByIds() throws Exception {
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"));
        SelfDescriptionMeta meta =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("2", "2345"));
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("3", "2345"));

        gxfsCatalogService.revokeSelfDescriptionByHash(meta.getSdHash());

        GXFSCatalogListResponse<SelfDescriptionItem> items =
                gxfsCatalogService.getSelfDescriptionsByIds(new String[]{"1", "2"},
                        new SelfDescriptionStatus[]{SelfDescriptionStatus.REVOKED});
        assertEquals(1, items.getTotalCount());
    }

    @Test
    void getExistingSelfDescriptionsByHashes() throws Exception {
        SelfDescriptionMeta meta1 =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"));
        SelfDescriptionMeta meta2 =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("2", "2345"));
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("3", "2345"));

        GXFSCatalogListResponse<SelfDescriptionItem> items =
                gxfsCatalogService.getSelfDescriptionsByHashes(new String[]{
                        meta1.getSdHash(),
                        meta2.getSdHash()});
        assertEquals(2, items.getTotalCount());
    }

    @Test
    void getExistingRevokedSelfDescriptionsByHashes() throws Exception {
        SelfDescriptionMeta meta1 =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"));
        SelfDescriptionMeta meta2 =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("2", "2345"));
        gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("3", "2345"));

        gxfsCatalogService.revokeSelfDescriptionByHash(meta2.getSdHash());

        GXFSCatalogListResponse<SelfDescriptionItem> items =
                gxfsCatalogService.getSelfDescriptionsByHashes(new String[]{
                        meta1.getSdHash(),
                        meta2.getSdHash()},
                        new SelfDescriptionStatus[]{SelfDescriptionStatus.REVOKED});
        assertEquals(1, items.getTotalCount());
    }

    @Test
    void addValidServiceOffering() throws Exception {
        SelfDescriptionMeta meta =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"));
        assertNotNull(meta);
    }

    @Test
    void addValidServiceOfferingExternalKey() throws Exception {
        SelfDescriptionMeta meta =
                gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"),
                        "did:web:localhost%3A8101:1234#JWK2020", privateKey);
        assertNotNull(meta);
    }

    @Test
    void addValidServiceOfferingMerlotVerificationMethod() throws Exception {
        SelfDescriptionMeta meta =
            gxfsCatalogService.addServiceOffering(generateOfferingCredentialSubject("1", "2345"),
                "did:web:localhost%3A8101:1234#MERLOTJWK2020");
        assertNotNull(meta);
    }

    @Test
    void addInvalidServiceOffering() {
        GaxCoreServiceOfferingCredentialSubject subject = new GaxCoreServiceOfferingCredentialSubject();
        assertThrows(NullPointerException.class, () ->
                gxfsCatalogService.addServiceOffering(subject));
    }

    @Test
    void getExistingParticipantById() throws Exception {
        gxfsCatalogService.addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"));

        ParticipantItem item = gxfsCatalogService.getParticipantById("2345");
        assertNotNull(item);
    }

    @Test
    void getMissingParticipantById() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.getParticipantById("missing"));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void getParticipantByIdCatalogError() {
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.getParticipantById("error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
    }

    @Test
    void addValidParticipant() throws Exception {
        ParticipantItem item = gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"));
        assertNotNull(item);
    }

    @Test
    void addValidParticipantValidExternalKey() throws Exception {
        ParticipantItem item = gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                        "did:web:localhost%3A8101:1234#JWK2020",
                        privateKey);
        assertNotNull(item);
    }

    @Test
    void addValidParticipantValidMerlotVerificationMethod() throws Exception {
        ParticipantItem item = gxfsCatalogService
            .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                "did:web:localhost%3A8101:1234#MERLOTJWK2020");
        assertNotNull(item);
    }

    @Test
    void addValidParticipantExternalKeyMissingCert() {
        assertThrows(CredentialSignatureException.class,
                () -> gxfsCatalogService.addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                        "did:web:localhost%3A8101:1234#someotherkey",
                        privateKey));
    }

    @Test
    void addValidParticipantExternalKeyUnknownDid() {
        assertThrows(CredentialSignatureException.class,
                () -> gxfsCatalogService.addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                        "did:web:example.org:1234",
                        privateKey));
    }

    @Test
    void addValidParticipantExternalKeyInvalidPrivateKey() {
        assertThrows(CredentialSignatureException.class,
                () -> gxfsCatalogService.addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                        "did:web:localhost%3A8101:1234#JWK2020",
                        "garbage"));
    }

    @Test
    void addValidParticipantExternalKeyNotDidWeb() throws CredentialSignatureException, CredentialPresentationException {
        ParticipantItem item = gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                        "did:other:123",
                        privateKey);
        assertNotNull(item);
    }

    @Test
    void addInvalidParticipant() {
        GaxTrustLegalPersonCredentialSubject subject = new GaxTrustLegalPersonCredentialSubject();
        assertThrows(NullPointerException.class, () ->
                gxfsCatalogService.addParticipant(subject));
    }

    @Test
    void updateExistingParticipant() throws Exception {
        ParticipantItem item = gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"));

        GaxTrustLegalPersonCredentialSubject credentialSubject = (GaxTrustLegalPersonCredentialSubject) item
                .getSelfDescription().getVerifiableCredential().getCredentialSubject();
        credentialSubject.setLegalName("MyNewParticipant");
        ParticipantItem item2 = gxfsCatalogService.updateParticipant(credentialSubject);
        assertNotNull(item2);
        assertNotEquals("MyParticipant", ((GaxTrustLegalPersonCredentialSubject) item2.getSelfDescription()
                .getVerifiableCredential().getCredentialSubject()).getLegalName());
    }

    @Test
    void updateExistingParticipantExternalKey() throws Exception {
        ParticipantItem item = gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                        "did:web:localhost%3A8101:1234#JWK2020", privateKey);

        GaxTrustLegalPersonCredentialSubject credentialSubject = (GaxTrustLegalPersonCredentialSubject) item
                .getSelfDescription().getVerifiableCredential().getCredentialSubject();
        credentialSubject.setLegalName("MyNewParticipant");
        ParticipantItem item2 = gxfsCatalogService.updateParticipant(credentialSubject,
                "did:web:localhost%3A8101:1234#JWK2020", privateKey);
        assertNotNull(item2);
        assertNotEquals("MyParticipant", ((GaxTrustLegalPersonCredentialSubject) item2.getSelfDescription()
                .getVerifiableCredential().getCredentialSubject()).getLegalName());
    }

    @Test
    void updateExistingParticipantMerlotVerificationMethod() throws Exception {
        ParticipantItem item = gxfsCatalogService
            .addParticipant(generateParticipantCredentialSubject("2345", "MyParticipant"),
                "did:web:localhost%3A8101:1234#MERLOTJWK2020");

        GaxTrustLegalPersonCredentialSubject credentialSubject = (GaxTrustLegalPersonCredentialSubject) item
            .getSelfDescription().getVerifiableCredential().getCredentialSubject();
        credentialSubject.setLegalName("MyNewParticipant");
        ParticipantItem item2 = gxfsCatalogService.updateParticipant(credentialSubject,
            "did:web:localhost%3A8101:1234#MERLOTJWK2020");
        assertNotNull(item2);
        assertNotEquals("MyParticipant", ((GaxTrustLegalPersonCredentialSubject) item2.getSelfDescription()
            .getVerifiableCredential().getCredentialSubject()).getLegalName());
    }

    @Test
    void updateNonExistentParticipant() {
        GaxTrustLegalPersonCredentialSubject subject =
                generateParticipantCredentialSubject("missing", "MyParticipant");
        WebClientResponseException e = assertThrows(WebClientResponseException.class,
                () -> gxfsCatalogService.updateParticipant(subject));
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }

    @Test
    void getParticipantsUriPage() throws Exception {
        for (int i = 0; i < 3; i++) {
            gxfsCatalogService
                    .addParticipant(generateParticipantCredentialSubject("" + i, "MyParticipant"));
        }
        GXFSCatalogListResponse<GXFSQueryUriItem> uriPage = gxfsCatalogService.getSortedParticipantUriPage(
                "LegalPerson", "legalName", 0, 3);

        assertEquals(3, uriPage.getTotalCount());
        assertEquals(3, uriPage.getItems().size());
    }

    @Test
    void getParticipantsUriPageWithExcludedUris() throws Exception {
        for (int i = 0; i < 3; i++) {
            gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("" + i, "MyParticipant"));
        }
        GXFSCatalogListResponse<GXFSQueryUriItem> uriPage = gxfsCatalogService.getSortedParticipantUriPageWithExcludedUris(
            "LegalPerson", "legalName", List.of("0", "1"), 0, 3);

        assertEquals(1, uriPage.getTotalCount());
        assertEquals(1, uriPage.getItems().size());
    }

    @Test
    void getParticipantLegalNameByUri() throws Exception {
        for (int i = 0; i < 3; i++) {
            gxfsCatalogService
                .addParticipant(generateParticipantCredentialSubject("" + i, "MyParticipant" + i));
        }

        GXFSCatalogListResponse<GXFSQueryLegalNameItem> legalNames = gxfsCatalogService.getParticipantLegalNameByUri(
            "LegalPerson", "2");

        assertEquals(1, legalNames.getTotalCount());
        assertEquals(1, legalNames.getItems().size());
        assertEquals("MyParticipant2", legalNames.getItems().get(0).getLegalName());
    }

}
