package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.*;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.RegistrationNumber;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.StringTypeValue;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.datatypes.VCard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.participants.GaxTrustLegalPersonCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.serviceofferings.GaxCoreServiceOfferingCredentialSubject;

import java.util.List;

public class GxfsCatalogClientFake implements GxfsCatalogClient {

    private SelfDescriptionItem generateBasicOfferingSdItem(String id,
                                                        String issuer,
                                                        String offeredBy,
                                                        SelfDescriptionStatus status) {
        SelfDescriptionItem item = new SelfDescriptionItem();
        item.setMeta(new SelfDescriptionMeta());
        item.getMeta().setId(id);
        item.getMeta().setIssuer(issuer);
        item.getMeta().setStatus(status.getValue());
        item.getMeta().setContent(new SelfDescription());
        item.getMeta().getContent().setId(id);
        item.getMeta().getContent().setVerifiableCredential(new SelfDescriptionVerifiableCredential());
        GaxCoreServiceOfferingCredentialSubject credentialSubject = new GaxCoreServiceOfferingCredentialSubject();
        item.getMeta().getContent().getVerifiableCredential().setCredentialSubject(credentialSubject);

        credentialSubject.setId(id);
        credentialSubject.setType("gax-core:ServiceOffering");
        credentialSubject.setOfferedBy(new NodeKindIRITypeId(offeredBy));
        return item;
    }

    private ParticipantItem generateParticipantItem(String id, String name) {
        ParticipantItem item = new ParticipantItem();
        item.setId(id);
        item.setName(name);
        item.setSelfDescription(new SelfDescription());
        item.getSelfDescription().setId(id);
        item.getSelfDescription().setVerifiableCredential(new SelfDescriptionVerifiableCredential());
        GaxTrustLegalPersonCredentialSubject credentialSubject = new GaxTrustLegalPersonCredentialSubject();
        item.getSelfDescription().getVerifiableCredential().setCredentialSubject(credentialSubject);
        credentialSubject.setId(id);
        credentialSubject.setType("gax-trust-framework:LegalPerson");
        credentialSubject.setRegistrationNumber(new RegistrationNumber());
        credentialSubject.getRegistrationNumber().setLocal(new StringTypeValue("12345"));
        VCard address = new VCard();
        address.setCountryName(new StringTypeValue("DE"));
        address.setStreetAddress(new StringTypeValue("Some Street 3"));
        address.setLocality(new StringTypeValue("Berlin"));
        address.setPostalCode(new StringTypeValue("12345"));
        credentialSubject.setHeadquarterAddress(address);
        credentialSubject.setLegalAddress(address);
        return item;
    }

    private SelfDescriptionMeta generateSdMetaNoContent(String id,
                                                        String issuer,
                                                        SelfDescriptionStatus status) {
        SelfDescriptionMeta meta = new SelfDescriptionMeta();
        meta.setContent(null);
        meta.setId(id);
        meta.setSubjectId(id);
        meta.setStatus(status.getValue());
        meta.setIssuer(issuer);
        meta.setSdHash("1234");

        return meta;
    }

    @Override
    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionList(
            String uploadTimerange,
            String statusTimerange,
            String[] issuers,
            String[] validators,
            SelfDescriptionStatus[] statuses,
            String[] ids,
            String[] hashes,
            boolean withMeta,
            boolean withContent,
            int offset,
            int limit) {

        GXFSCatalogListResponse<SelfDescriptionItem> response = new GXFSCatalogListResponse<>();


        response.setItems(List.of(
                generateBasicOfferingSdItem(
                "1234",
                "2345",
                "2345",
                SelfDescriptionStatus.ACTIVE),
                generateBasicOfferingSdItem(
                        "5678",
                        "6789",
                        "6789",
                        SelfDescriptionStatus.ACTIVE)));
        response.setTotalCount(2);

        return response;
    }

    @Override
    public SelfDescriptionMeta postAddSelfDescription(VerifiablePresentation body) {

        return generateSdMetaNoContent(
                body.getVerifiableCredential().getCredentialSubject().getId().toString(),
                body.getVerifiableCredential().getIssuer().toString(),
                SelfDescriptionStatus.ACTIVE
        );
    }

    @Override
    public SelfDescriptionItem getSelfDescriptionByHash(String sdHash) {
        return generateBasicOfferingSdItem(
                "1234",
                "2345",
                "2345",
                SelfDescriptionStatus.ACTIVE);
    }

    @Override
    public void deleteSelfDescriptionByHash(String sdHash) {
    }

    @Override
    public SelfDescriptionMeta postRevokeSelfDescriptionByHash(String sdHash) {
        return generateSdMetaNoContent(
                "1234",
                "2345",
                SelfDescriptionStatus.REVOKED
        );
    }

    @Override
    public GXFSCatalogListResponse<GXFSQueryUriItem> postQuery(
            QueryLanguage queryLanguage,
            int timeout,
            boolean withTotalCount,
            String query) {
        GXFSCatalogListResponse<GXFSQueryUriItem> response = new GXFSCatalogListResponse<>();
        GXFSQueryUriItem uriItem = new GXFSQueryUriItem();
        uriItem.setUri("2345");
        GXFSQueryUriItem uriItem2 = new GXFSQueryUriItem();
        uriItem2.setUri("6789");
        response.setItems(List.of(uriItem, uriItem2));
        response.setTotalCount(2);
        return response;
    }

    @Override
    public GXFSCatalogListResponse<ParticipantItem> getParticipants(int offset, int limit) {
        GXFSCatalogListResponse<ParticipantItem> response = new GXFSCatalogListResponse<>();
        response.setItems(List.of(
                generateParticipantItem("2345", "Participant1"),
                generateParticipantItem("6789", "Participant2")
        ));
        response.setTotalCount(2);
        return response;
    }

    @Override
    public ParticipantItem postAddParticipant(VerifiablePresentation body) {
        return generateParticipantItem(
                body.getId().toString(),
                ((StringTypeValue) body.getVerifiableCredential()
                        .getCredentialSubject().getClaims()
                        .get("gax-trust-framework:legalName")).getValue());
    }

    @Override
    public ParticipantItem getParticipantById(String participantId) {
        return generateParticipantItem(
                participantId,
                "Participant"
        );
    }

    @Override
    public ParticipantItem putUpdateParticipant(String participantId, VerifiablePresentation body) {
        return generateParticipantItem(
                participantId,
                ((StringTypeValue) body.getVerifiableCredential()
                        .getCredentialSubject().getClaims()
                        .get("gax-trust-framework:legalName")).getValue()
        );
    }

    @Override
    public ParticipantItem deleteParticipant(String participantId) {
        return generateParticipantItem(
                participantId,
                "Participant"
        );
    }

}
