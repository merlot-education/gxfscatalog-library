package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryRequest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryLegalNameItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.*;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.GxVcard;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.LegalParticipantCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.LegalRegistrationNumberCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.serviceofferings.ServiceOfferingCredentialSubject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        ServiceOfferingCredentialSubject credentialSubject = new ServiceOfferingCredentialSubject();
        credentialSubject.setId(id);
        credentialSubject.setType("gx:ServiceOffering");
        credentialSubject.setProvidedBy(new NodeKindIRITypeId(offeredBy));

        SelfDescriptionVerifiableCredential vc = new SelfDescriptionVerifiableCredential();
        vc.setCredentialSubject(credentialSubject);

        item.getMeta().getContent().setVerifiableCredential(List.of(vc));
        return item;
    }

    private ParticipantItem generateParticipantItem(String id, String name) {
        ParticipantItem item = new ParticipantItem();
        item.setId(id);
        item.setName(name);
        item.setSelfDescription(new SelfDescription());
        item.getSelfDescription().setId(id);

        LegalParticipantCredentialSubject participantCs = new LegalParticipantCredentialSubject();
        participantCs.setId(id + "#legalParticipant");
        participantCs.setName(name);
        participantCs.setLegalRegistrationNumber(List.of(new NodeKindIRITypeId("did:web:1234")));
        GxVcard address = new GxVcard();
        address.setCountryCode("DE");
        address.setCountrySubdivisionCode("DE-BER");
        address.setLocality("Berlin");
        address.setStreetAddress("Some Street 3");
        address.setPostalCode("12345");
        participantCs.setLegalAddress(address);
        participantCs.setHeadquarterAddress(address);
        SelfDescriptionVerifiableCredential participantVc = new SelfDescriptionVerifiableCredential();
        participantVc.setId(id);
        participantVc.setCredentialSubject(participantCs);

        LegalRegistrationNumberCredentialSubject registrationNumberCs = new LegalRegistrationNumberCredentialSubject();
        registrationNumberCs.setId(id + "#legalRegistrationNumber");
        registrationNumberCs.setVatID(List.of("FR79537407926"));
        SelfDescriptionVerifiableCredential registrationNumberVc = new SelfDescriptionVerifiableCredential();
        registrationNumberVc.setId(id);
        registrationNumberVc.setCredentialSubject(registrationNumberCs);

        item.getSelfDescription().setVerifiableCredential(List.of(participantVc, registrationNumberVc));

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
                body.getVerifiableCredential().getCredentialSubject().getJsonObject().get("id").toString(),
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
    public <T> GXFSCatalogListResponse<T> postQuery(QueryLanguage queryLanguage, int timeout, boolean withTotalCount,
        QueryRequest query) {
        String statement = query.getStatement();

        if (statement.contains("return p.legalName")) {
            String id = findId(statement);

            List<GXFSQueryLegalNameItem> legalNames = participantItems.stream().filter(pi -> pi.getId().equals(id))
                .map(pi -> {
                    LegalParticipantCredentialSubject cs = getLegalParticipantCredentialSubject(pi);
                    GXFSQueryLegalNameItem legalNameItem = new GXFSQueryLegalNameItem();
                    if (cs != null) {
                        legalNameItem.setLegalName(cs.getName());
                    }
                    return legalNameItem;
                }).toList();

            GXFSCatalogListResponse<GXFSQueryLegalNameItem> response = new GXFSCatalogListResponse<>();
            response.setItems(legalNames);
            response.setTotalCount(legalNames.size());
            return (GXFSCatalogListResponse<T>) response;
        } else if (statement.contains("return p.uri")) {
            List<String> excludedUris = findListOfIds(query.getStatement());

            List<GXFSQueryUriItem> uris = participantItems.stream().filter(pi -> !excludedUris.contains(pi.getId()))
                .map(pi -> {
                    GXFSQueryUriItem uriItem = new GXFSQueryUriItem();
                    uriItem.setUri(pi.getId());
                    return uriItem;
                }).toList();

            GXFSCatalogListResponse<GXFSQueryUriItem> response = new GXFSCatalogListResponse<>();
            response.setItems(uris);
            response.setTotalCount(uris.size());
            return (GXFSCatalogListResponse<T>) response;
        } else {
            return null;
        }
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
                body.getVerifiableCredential().getCredentialSubject().getJsonObject().get("id").toString(),
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
        LegalParticipantCredentialSubject cs = getLegalParticipantCredentialSubject(item);
        if (cs != null) {
            cs.setName(((Map<String, String>) body.getVerifiableCredential().getCredentialSubject().getClaims()
                    .get("gx:name")).get("@value"));
        }
        return item;
    }

    @Override
    public ParticipantItem deleteParticipant(String participantId) {
        checkError(participantId);
        ParticipantItem item = findParticipantItemById(participantId);
        participantItems.remove(item);
        return item;
    }

    private List<String> findListOfIds(String query){
        String regex = "IN \\[([^\\]]*)\\] return";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query);

        List<String> ids = new ArrayList<>();

        if (matcher.find()){
            String matched = matcher.group(1).replace("\"", "");

            ids = Arrays.stream(matched.split(", ")).toList();
        }

        return ids;
    }

    private String findId(String query){
        String regex = "p\\.uri\\s*=\\s*([^\\s]*)\\s*return";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(query);

        List<String> ids = new ArrayList<>();

        while (matcher.find()) {
            String id = matcher.group(1).replace("\"", "");;
            ids.add(id);
        }

        return ids.get(0);
    }

    private LegalParticipantCredentialSubject getLegalParticipantCredentialSubject(ParticipantItem pi) {
        return pi.getSelfDescription().getVerifiableCredential().stream()
                .filter(vc -> vc.getCredentialSubject() instanceof LegalParticipantCredentialSubject)
                .map(vc -> (LegalParticipantCredentialSubject) vc.getCredentialSubject()).findFirst()
                .orElse(null);
    }

}
