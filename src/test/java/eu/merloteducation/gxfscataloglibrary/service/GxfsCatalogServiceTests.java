package eu.merloteducation.gxfscataloglibrary.service;

import eu.merloteducation.gxfscataloglibrary.config.GxfsCatalogLibConfig;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionMeta;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NodeKindIRITypeId;
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

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@EnableConfigurationProperties
public class GxfsCatalogServiceTests {

    @Autowired
    private GxfsCatalogService gxfsCatalogService;

    @MockBean
    private GxfsCatalogAuthService gxfsCatalogAuthService;

    @MockBean
    private GxfsCatalogClient gxfsCatalogClient;

    @MockBean
    private GxfsCatalogLibConfig gxfsCatalogLibConfig;

    private GaxCoreServiceOfferingCredentialSubject generateOfferingCredentialSubject(String id, String offeredBy) {
        GaxCoreServiceOfferingCredentialSubject credentialSubject = new GaxCoreServiceOfferingCredentialSubject();
        credentialSubject.setType("gax-core:ServiceOffering");
        credentialSubject.setId(id);
        credentialSubject.setOfferedBy(new NodeKindIRITypeId(offeredBy));
        credentialSubject.setContext(new HashMap<>());
        return credentialSubject;
    }


    @BeforeEach
    public void setUp() {
        // reset catalog client fake between each test
        ReflectionTestUtils.setField(gxfsCatalogService, "gxfsCatalogClient", new GxfsCatalogClientFake());
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

        meta = gxfsCatalogService.revokeSelfDescriptionByHash(meta.getId());
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
        SelfDescriptionMeta meta3 =
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
        SelfDescriptionMeta meta3 =
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
    void addInvalidServiceOffering() {
        assertThrows(NullPointerException.class, () ->
                gxfsCatalogService.addServiceOffering(new GaxCoreServiceOfferingCredentialSubject()));
    }

}
