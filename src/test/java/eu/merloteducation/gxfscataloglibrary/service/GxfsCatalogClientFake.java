/*
 *  Copyright 2024 Dataport. All rights reserved. Developed as part of the MERLOT project.
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

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryRequest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.credentials.CastableCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryLegalNameItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.*;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalParticipantCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GxfsCatalogClientFake implements GxfsCatalogClient {

    private final List<SelfDescriptionItem> selfDescriptionItems = new ArrayList<>();

    private final List<ParticipantItem> participantItems = new ArrayList<>();

    private ExtendedVerifiablePresentation createVpFromCsList(List<PojoCredentialSubject> csList, URI issuer) {
        List<ExtendedVerifiableCredential> vcList = new ArrayList<>();

        for (PojoCredentialSubject pojoCs : csList) {
            CastableCredentialSubject cs;
            try {
                cs = CastableCredentialSubject.fromPojo(pojoCs);
            } catch (JsonProcessingException e) {
                cs = new CastableCredentialSubject();
            }
            VerifiableCredential vc = VerifiableCredential
                    .builder()
                    .id(URI.create(cs.getId() + "#" + pojoCs.getType()))
                    .issuanceDate(Date.from(Instant.now()))
                    .credentialSubject(cs)
                    .issuer(issuer)
                    .build();
            ExtendedVerifiableCredential evc = ExtendedVerifiableCredential.fromMap(vc.getJsonObject());
            vcList.add(evc);
        }
        ExtendedVerifiablePresentation vp = new ExtendedVerifiablePresentation();
        vp.setVerifiableCredentials(vcList);
        vp.setJsonObjectKeyValue("id", csList.get(0).getId() + "#vp");
        return vp;
    }

    private SelfDescriptionItem generateBasicOfferingSdItem(String id,
                                                        String issuer,
                                                            ExtendedVerifiablePresentation vp,
                                                        SelfDescriptionStatus status) {
        SelfDescriptionItem item = new SelfDescriptionItem();
        item.setMeta(new SelfDescriptionMeta());
        item.getMeta().setId(id);
        item.getMeta().setSdHash(id);
        item.getMeta().setIssuer(issuer);
        item.getMeta().setStatus(status.getValue());

        item.getMeta().setContent(vp);

        return item;
    }

    private ParticipantItem generateParticipantItem(String id, String name, ExtendedVerifiablePresentation vp) {
        ParticipantItem item = new ParticipantItem();
        item.setId(id);
        item.setName(name);
        item.setSelfDescription(vp);

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
    public SelfDescriptionMeta postAddSelfDescription(ExtendedVerifiablePresentation body) {
        SelfDescriptionItem item;
            item = generateBasicOfferingSdItem(
                    body.getVerifiableCredentials().get(0).getCredentialSubject().getId().toString(),
                    body.getVerifiableCredentials().get(0).getIssuer().toString(),
                    body,
                    SelfDescriptionStatus.ACTIVE);
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
                    GxLegalParticipantCredentialSubject cs = pi.getSelfDescription()
                            .findFirstCredentialSubjectByType(GxLegalParticipantCredentialSubject.class);
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
    public ParticipantItem postAddParticipant(ExtendedVerifiablePresentation body) {
        ParticipantItem item = generateParticipantItem(
                getLegalParticipantCredentialSubject(body).getId(),
                getLegalParticipantCredentialSubject(body).getName(),
                body);
        participantItems.add(item);
        return item;
    }

    @Override
    public ParticipantItem getParticipantById(String participantId) {
        checkError(participantId);
        return findParticipantItemById(participantId);
    }

    @Override
    public ParticipantItem putUpdateParticipant(String participantId, ExtendedVerifiablePresentation body) {
        checkError(participantId);
        ParticipantItem item = findParticipantItemById(participantId);
        GxLegalParticipantCredentialSubject cs = getLegalParticipantCredentialSubject(item.getSelfDescription());
        GxLegalRegistrationNumberCredentialSubject regCs =
                getLegalRegistrationNumberCredentialSubject(item.getSelfDescription());
        if (cs != null && regCs != null) {
            cs.setName(getLegalParticipantCredentialSubject(body).getName());
            ExtendedVerifiablePresentation vp = createVpFromCsList(List.of(cs, regCs),
                    item.getSelfDescription().getVerifiableCredentials().get(0).getIssuer());
            item.setSelfDescription(vp);
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

    private GxLegalParticipantCredentialSubject getLegalParticipantCredentialSubject(ExtendedVerifiablePresentation vp) {
        return vp.findFirstCredentialSubjectByType(GxLegalParticipantCredentialSubject.class);
    }

    private GxLegalRegistrationNumberCredentialSubject getLegalRegistrationNumberCredentialSubject(ExtendedVerifiablePresentation vp) {
        return vp.findFirstCredentialSubjectByType(GxLegalRegistrationNumberCredentialSubject.class);
    }

}
