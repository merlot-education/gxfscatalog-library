package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionMeta;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.participants.GaxTrustLegalPersonCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.serviceofferings.GaxCoreServiceOfferingCredentialSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GxfsCatalogService {
    @Autowired
    private GxfsCatalogClient gxfsCatalogClient;

    @Autowired
    private GxfsSignerService gxfsSignerService;

    /**
     * Given the hash of a self-description in the catalog, set its status in the catalog to revoked.
     *
     * @param sdHash hash of the SD
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta revokeSelfDescriptionByHash(String sdHash) {
        return this.gxfsCatalogClient.postRevokeSelfDescriptionByHash(sdHash);
    }

    /**
     * Given the hash of a self-description in the catalog, fully delete/purge it from the catalog.
     *
     * @param sdHash hash of the SD
     */
    public void deleteSelfDescriptionByHash(String sdHash) {
        this.gxfsCatalogClient.deleteSelfDescriptionByHash(sdHash);
    }

    /**
     * Given the id of a participant in the catalog, return the respective item including the self-description.
     *
     * @param participantId hash of the SD
     * @return catalog content of the participant
     */
    public ParticipantItem getParticipantById(String participantId) {
        return this.gxfsCatalogClient.getParticipantById(participantId);
    }

    /**
     * Given a list of ids of self-descriptions in the catalog, return a list of self-description items that match these ids.
     * By default, this method will only return SDs with the status ACTIVE.
     * If other statuses are required, use the overloaded function with the same name.
     *
     * @param ids array of ids to query the catalog for
     * @return list of SD items that match the ids
     */
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

    /**
     * Given a list of ids of self-descriptions in the catalog, return a list of self-description items that match these ids.
     * This overload allows to further specify which states the SDs shall have in the catalog.
     *
     * @param ids array of ids to query the catalog for
     * @param selfDescriptionStatuses array of wanted SD statuses
     * @return list of SD items that match the ids
     */
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

    /**
     * Given a list of hashes of self-descriptions in the catalog, return a list of self-description items that match these hashes.
     * By default, this method will only return SDs with the status ACTIVE.
     * If other statuses are required, use the overloaded function with the same name.
     *
     * @param hashes array of hashes to query the catalog for
     * @return list of SD items that match the hashes
     */
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

    /**
     * Given a list of hashes of self-descriptions in the catalog, return a list of self-description items that match these hashes.
     * This overload allows to further specify which states the SDs shall have in the catalog.
     *
     * @param hashes array of hashes to query the catalog for
     * @param selfDescriptionStatuses array of wanted SD statuses
     * @return list of SD items that match the hashes
     */
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

    /**
     * Given the credential subject of a self-description that inherits from gax-core:ServiceOffering,
     * wrap it in a verifiable presentation, sign it using the key that was provided to this library and send it to the catalog.
     *
     * @param serviceOfferingCredentialSubject service offering credential subject to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta addServiceOffering(
            GaxCoreServiceOfferingCredentialSubject serviceOfferingCredentialSubject)
            throws CredentialPresentationException, CredentialSignatureException {
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(serviceOfferingCredentialSubject,
                        serviceOfferingCredentialSubject.getOfferedBy().getId());
        gxfsSignerService.signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.postAddSelfDescription(vp);
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson,
     * wrap it in a verifiable presentation, sign it using the key that was provided to this library and send it to the catalog.
     *
     * @param participantCredentialSubject participant credential subject to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem addParticipant(GaxTrustLegalPersonCredentialSubject participantCredentialSubject)
            throws CredentialPresentationException, CredentialSignatureException {
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        gxfsSignerService.signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.postAddParticipant(vp);
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson
     * and whose id already exists in the catalog, wrap it in a verifiable presentation,
     * sign it using the key that was provided to this library and send it to the catalog
     * to update it.
     *
     * @param participantCredentialSubject participant credential subject to update in the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem updateParticipant(GaxTrustLegalPersonCredentialSubject participantCredentialSubject)
            throws CredentialPresentationException, CredentialSignatureException {
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        gxfsSignerService.signVerifiablePresentation(vp);
        return this.gxfsCatalogClient.putUpdateParticipant(
                participantCredentialSubject.getId(),
                vp);
    }

    /**
     * Given paging parameters, return a list (page) of participant URIs sorted by some field
     * corresponding to these parameters.
     * This is mainly used to access the participant self-descriptions in a second step, by using the
     * uris as IDs and requesting SDs with these IDs.
     *
     * @param participantType type of the participant to query for, e.g. LegalPerson or MerlotOrganisation
     * @param sortField field of the participant to sort by
     * @param offset paging offset
     * @param size page size
     * @return list of participant uris corresponding to the paging parameters
     */
    public GXFSCatalogListResponse<GXFSQueryUriItem> getParticipantUriPage(
            String participantType, String sortField, long offset, long size) {
        String query = """
                {
                    "statement": "MATCH (p:""" + participantType + ")"
                + " return p.uri ORDER BY toLower(p." + sortField + ")"
                + " SKIP " + offset + " LIMIT " + size + """
                    "
                }
        """;
        return this.gxfsCatalogClient.postQuery(
                QueryLanguage.OPENCYPHER,
                5,
                true,
                query);
    }
}
