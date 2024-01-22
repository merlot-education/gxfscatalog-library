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
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;

public class GxfsCatalogClientFake implements GxfsCatalogClient {

    private final List<SelfDescriptionItem> selfDescriptionItems = new ArrayList<>();

    private final List<ParticipantItem> participantItems = new ArrayList<>();

    private SelfDescriptionItem generateBasicOfferingSdItem(String id,
                                                        String issuer,
                                                        String offeredBy,
                                                        SelfDescriptionStatus status) {
        SelfDescriptionItem item = new SelfDescriptionItem();
        item.setMeta(new SelfDescriptionMeta());
        item.getMeta().setId(id);
        item.getMeta().setSdHash(id);
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
        credentialSubject.setLegalName(new StringTypeValue(name));
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

    private void checkError(String request) {
        if (request.equals("error")) {
            throw new WebClientResponseException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "error", null, null, null);
        }
    }

    private SelfDescriptionItem findSelfDescriptionItemByHash(String sdHash) {
        List<SelfDescriptionItem> result =
                selfDescriptionItems.stream().filter(sdi -> sdi.getMeta().getSdHash().equals(sdHash)).toList();
        if (result.isEmpty()) {
            throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "missing", null, null, null);
        }
        return result.get(0);
    }

    private ParticipantItem findParticipantItemById(String participantId) {
        List<ParticipantItem> result =
                participantItems.stream().filter(pi -> pi.getId().equals(participantId)).toList();
        if (result.isEmpty()) {
            throw new WebClientResponseException(HttpStatus.NOT_FOUND.value(), "missing", null, null, null);
        }
        return result.get(0);
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
        List<String> reqIds = ids == null ? Collections.emptyList() : Arrays.asList(ids);
        List<String> reqHashes = hashes == null ? Collections.emptyList() : Arrays.asList(hashes);
        List<String> reqStatuses = Arrays.asList(statuses).stream().map(s -> s.getValue()).toList();
        List<SelfDescriptionItem> items = selfDescriptionItems.stream().filter(sdi ->
                reqStatuses.contains(sdi.getMeta().getStatus()) &&
                (reqIds.contains(sdi.getMeta().getId()) || reqHashes.contains(sdi.getMeta().getSdHash()))
                ).toList();

        response.setItems(items);
        response.setTotalCount(items.size());

        return response;
    }

    @Override
    public SelfDescriptionMeta postAddSelfDescription(VerifiablePresentation body) {
        SelfDescriptionItem item = generateBasicOfferingSdItem(
                body.getVerifiableCredential().getCredentialSubject().getJsonObject().get("@id").toString(),
                body.getVerifiableCredential().getIssuer().toString(),
                body.getVerifiableCredential().getIssuer().toString(),
                SelfDescriptionStatus.ACTIVE
        );
        selfDescriptionItems.add(item);
        return item.getMeta();
    }

    @Override
    public SelfDescriptionItem getSelfDescriptionByHash(String sdHash) {
        checkError(sdHash);
        return findSelfDescriptionItemByHash(sdHash);
    }

    @Override
    public void deleteSelfDescriptionByHash(String sdHash) {
        checkError(sdHash);
        SelfDescriptionItem item = findSelfDescriptionItemByHash(sdHash);
        selfDescriptionItems.remove(item);
    }

    @Override
    public SelfDescriptionMeta postRevokeSelfDescriptionByHash(String sdHash) {
        checkError(sdHash);
        SelfDescriptionItem item = findSelfDescriptionItemByHash(sdHash);
        item.getMeta().setStatus(SelfDescriptionStatus.REVOKED.getValue());
        return item.getMeta();
    }

    @Override
    public GXFSCatalogListResponse<GXFSQueryUriItem> postQuery(
            QueryLanguage queryLanguage,
            int timeout,
            boolean withTotalCount,
            String query) {
        List<GXFSQueryUriItem> uris = participantItems.stream().map(pi -> {
            GXFSQueryUriItem uriItem = new GXFSQueryUriItem();
            uriItem.setUri(pi.getId());
            return uriItem;
        }).toList();
        GXFSCatalogListResponse<GXFSQueryUriItem> response = new GXFSCatalogListResponse<>();
        response.setItems(uris);
        response.setTotalCount(uris.size());
        return response;
    }

    @Override
    public GXFSCatalogListResponse<ParticipantItem> getParticipants(int offset, int limit) {
        GXFSCatalogListResponse<ParticipantItem> response = new GXFSCatalogListResponse<>();
        response.setItems(participantItems);
        response.setTotalCount(participantItems.size());
        return response;
    }

    @Override
    public ParticipantItem postAddParticipant(VerifiablePresentation body) {
        ParticipantItem item = generateParticipantItem(
                body.getVerifiableCredential().getCredentialSubject().getJsonObject().get("@id").toString(),
                ((Map<String, String>) body.getVerifiableCredential().getCredentialSubject().getClaims()
                        .get("gax-trust-framework:legalName")).get("@value"));
        participantItems.add(item);
        return item;
    }

    @Override
    public ParticipantItem getParticipantById(String participantId) {
        checkError(participantId);
        return findParticipantItemById(participantId);
    }

    @Override
    public ParticipantItem putUpdateParticipant(String participantId, VerifiablePresentation body) {
        checkError(participantId);
        ParticipantItem item = findParticipantItemById(participantId);
        GaxTrustLegalPersonCredentialSubject credentialSubject =
                (GaxTrustLegalPersonCredentialSubject)
                        item.getSelfDescription().getVerifiableCredential().getCredentialSubject();
        credentialSubject.setLegalName(new StringTypeValue(
                ((Map<String, String>) body.getVerifiableCredential().getCredentialSubject().getClaims()
                        .get("gax-trust-framework:legalName")).get("@value")));
        return item;
    }

    @Override
    public ParticipantItem deleteParticipant(String participantId) {
        checkError(participantId);
        ParticipantItem item = findParticipantItemById(participantId);
        participantItems.remove(item);
        return item;
    }

}
