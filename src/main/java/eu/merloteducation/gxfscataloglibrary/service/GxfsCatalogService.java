package eu.merloteducation.gxfscataloglibrary.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.merloteducation.modelslib.gxfscatalog.participants.ParticipantItem;
import eu.merloteducation.modelslib.gxfscatalog.query.GXFSQueryUriItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionCredentialSubject;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionsCreateResponse;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.participants.MerlotOrganizationCredentialSubject;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.serviceofferings.ServiceOfferingCredentialSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public GXFSCatalogListResponse
            <SelfDescriptionItem
                    <SelfDescriptionCredentialSubject>> getSelfDescriptionsByIds(String[] ids) {
        return this.gxfsCatalogClient.getSelfDescriptionList(true,
                new String[]{"ACTIVE", "REVOKED"},
                ids,
                null);
    }

    public GXFSCatalogListResponse
            <SelfDescriptionItem
                    <SelfDescriptionCredentialSubject>> getSelfDescriptionsByHashes(String[] hashes) {
        return this.gxfsCatalogClient.getSelfDescriptionList(true,
                new String[]{"ACTIVE", "REVOKED"},
                null,
                hashes);
    }

    public SelfDescriptionsCreateResponse addServiceOffering(
            ServiceOfferingCredentialSubject serviceOfferingCredentialSubject) throws JsonProcessingException {
        String signedVp = gxfsSignerService
                .presentVerifiableCredential(serviceOfferingCredentialSubject,
                        "did:web:merlot-education.eu");
        return this.gxfsCatalogClient.postAddSelfDescription(signedVp);
    }

    public ParticipantItem addParticipant(
            MerlotOrganizationCredentialSubject merlotOrganizationCredentialSubject) throws JsonProcessingException {
        String signedVp = gxfsSignerService
                .presentVerifiableCredential(merlotOrganizationCredentialSubject,
                        merlotOrganizationCredentialSubject.getId());
        return this.gxfsCatalogClient.postAddParticipant(signedVp);
    }

    public ParticipantItem updateParticipant(
            MerlotOrganizationCredentialSubject merlotOrganizationCredentialSubject) throws JsonProcessingException {
        String signedVp = gxfsSignerService
                .presentVerifiableCredential(merlotOrganizationCredentialSubject,
                        merlotOrganizationCredentialSubject.getId());
        return this.gxfsCatalogClient.putUpdateParticipant(
                merlotOrganizationCredentialSubject.getId(),
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
