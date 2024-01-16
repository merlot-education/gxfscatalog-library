package eu.merloteducation.gxfscataloglibrary.service;

import eu.merloteducation.modelslib.gxfscatalog.participants.ParticipantItem;
import eu.merloteducation.modelslib.gxfscatalog.query.GXFSQueryUriItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionsCreateResponse;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.participants.GaxTrustLegalPersonCredentialSubject;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.serviceofferings.GaxCoreServiceOfferingCredentialSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

@Service
public class GxfsCatalogService {
    @Autowired
    private GxfsCatalogClient gxfsCatalogClient;

    @Autowired
    private GxfsSignerService gxfsSignerService;

    public SelfDescriptionsCreateResponse revokeSelfDescriptionByHash(String sdHash) {
        return this.gxfsCatalogClient.postRevokeSelfDescriptionByHash(sdHash);
    }

    public void deleteSelfDescriptionByHash(String sdHash) {
        this.gxfsCatalogClient.deleteSelfDescriptionByHash(sdHash);
    }

    public ParticipantItem getParticipantById(String participantId) {
        return this.gxfsCatalogClient.getParticipantById(participantId);
    }

    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByIds(String[] ids) {
        return this.gxfsCatalogClient.getSelfDescriptionList(true,
                new String[]{"ACTIVE", "REVOKED"},
                ids,
                null);
    }

    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByHashes(String[] hashes) {
        return this.gxfsCatalogClient.getSelfDescriptionList(true,
                new String[]{"ACTIVE", "REVOKED"},
                null,
                hashes);
    }

    public SelfDescriptionsCreateResponse addServiceOffering(
            GaxCoreServiceOfferingCredentialSubject serviceOfferingCredentialSubject) throws Exception {
        String vp = gxfsSignerService
                .presentVerifiableCredential(serviceOfferingCredentialSubject,
                        serviceOfferingCredentialSubject.getOfferedBy().getId());
        String signedVp = gxfsSignerService
                .signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.postAddSelfDescription(signedVp);
    }

    public ParticipantItem addParticipant(
            GaxTrustLegalPersonCredentialSubject participantCredentialSubject) throws Exception {
        String vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        String signedVp = gxfsSignerService
                .signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.postAddParticipant(signedVp);
    }

    public ParticipantItem updateParticipant(
            GaxTrustLegalPersonCredentialSubject participantCredentialSubject) throws Exception {
        String vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        String signedVp = gxfsSignerService
                .signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.putUpdateParticipant(
                participantCredentialSubject.getId(),
                signedVp);
    }

    public GXFSCatalogListResponse<GXFSQueryUriItem> getParticipantUriPage(Pageable pageable) {
        String query = """
                {
                    "statement": "MATCH (p:MerlotOrganization) return p.uri ORDER BY toLower(p.orgaName)"""
                + " SKIP " + pageable.getOffset() + " LIMIT " + pageable.getPageSize() + """
                    "
                }
        """;
        return this.gxfsCatalogClient.postQuery(query);
    }
}
