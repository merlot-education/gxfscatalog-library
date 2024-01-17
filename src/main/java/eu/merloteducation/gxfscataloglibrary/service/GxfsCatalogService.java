package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.SelfDescriptionStatus;
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
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                new SelfDescriptionStatus[]{SelfDescriptionStatus.ACTIVE},
                ids,
                null,
                true,
                true,
                0,
                ids.length);
    }

    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByIds(String[] ids,
                                                                                 SelfDescriptionStatus[] selfDescriptionStatuses) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                selfDescriptionStatuses,
                ids,
                null,
                true,
                true,
                0,
                ids.length);
    }

    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByHashes(String[] hashes) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                new SelfDescriptionStatus[]{SelfDescriptionStatus.ACTIVE},
                null,
                hashes,
                true,
                true,
                0,
                hashes.length);
    }

    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByHashes(String[] hashes,
                                                                                    SelfDescriptionStatus[] selfDescriptionStatuses) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                selfDescriptionStatuses,
                null,
                hashes,
                true,
                true,
                0,
                hashes.length);
    }

    public SelfDescriptionsCreateResponse addServiceOffering(
            GaxCoreServiceOfferingCredentialSubject serviceOfferingCredentialSubject) throws Exception {
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(serviceOfferingCredentialSubject,
                        serviceOfferingCredentialSubject.getOfferedBy().getId());
        gxfsSignerService.signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.postAddSelfDescription(vp);
    }

    public ParticipantItem addParticipant(
            GaxTrustLegalPersonCredentialSubject participantCredentialSubject) throws Exception {
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        gxfsSignerService.signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.postAddParticipant(vp);
    }

    public ParticipantItem updateParticipant(
            GaxTrustLegalPersonCredentialSubject participantCredentialSubject) throws Exception {
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        gxfsSignerService.signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.putUpdateParticipant(
                participantCredentialSubject.getId(),
                vp);
    }

    public GXFSCatalogListResponse<GXFSQueryUriItem> getParticipantUriPage(Pageable pageable) {
        String query = """
                {
                    "statement": "MATCH (p:MerlotOrganization) return p.uri ORDER BY toLower(p.orgaName)"""
                + " SKIP " + pageable.getOffset() + " LIMIT " + pageable.getPageSize() + """
                    "
                }
        """;
        return this.gxfsCatalogClient.postQuery(
                null,
                5,
                true,
                query);
    }
}
