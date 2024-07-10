/*
 *  Copyright 2023-2024 Dataport AöR
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
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.merloteducation.gxfscataloglibrary.models.credentials.CastableCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.datatypes.NodeKindIRITypeId;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalParticipantCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.serviceofferings.GxServiceOfferingCredentialSubject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExtendedCredentialTests {

    @BeforeEach
    public void setUp() {

    }

    @Test
    void createExtendedVerifiablePresentationFromVp() {
        VerifiablePresentation vp = VerifiablePresentation.builder().id(URI.create("example.com")).build();
        ExtendedVerifiablePresentation evp = ExtendedVerifiablePresentation.fromMap(vp.getJsonObject());

        assertEquals(evp.getId(), vp.getId());
    }

    @Test
    void extendedVpSingleCredential() {
        URI credId = URI.create("did:web:someid");
        VerifiableCredential vc = VerifiableCredential.builder().id(credId).build();
        VerifiablePresentation vp = VerifiablePresentation.builder()
                .id(URI.create("example.com")).verifiableCredential(vc).build();
        ExtendedVerifiablePresentation evp = ExtendedVerifiablePresentation.fromMap(vp.getJsonObject());

        assertEquals(credId, evp.getVerifiableCredentials().get(0).getId());
        assertEquals(credId, evp.getVerifiableCredential().getId());
    }

    @Test
    void extendedVpMultipleCredentials() {
        ExtendedVerifiablePresentation evp = new ExtendedVerifiablePresentation();
        ExtendedVerifiableCredential evc1 = new ExtendedVerifiableCredential();
        ExtendedVerifiableCredential evc2 = new ExtendedVerifiableCredential();
        evp.setVerifiableCredentials(List.of(evc1, evc2));

        assertEquals(2, evp.getVerifiableCredentials().size());
    }

    @Test
    void castableCredentialSubjectFromAndToPojo() throws JsonProcessingException {
        GxLegalParticipantCredentialSubject participantCs = new GxLegalParticipantCredentialSubject();
        participantCs.setId("1234");
        participantCs.setName("Test");

        CastableCredentialSubject cs = CastableCredentialSubject.fromPojo(participantCs);
        assertEquals(participantCs.getId(), cs.getId().toString());

        GxLegalParticipantCredentialSubject resultParticipantCs = cs.toPojo(GxLegalParticipantCredentialSubject.class);
        assertEquals(participantCs.getId(), resultParticipantCs.getId());
        assertEquals(participantCs.getName(), resultParticipantCs.getName());
    }

    @Test
    void extendedVpFindCsByType() throws JsonProcessingException {
        GxLegalParticipantCredentialSubject participantCs = new GxLegalParticipantCredentialSubject();
        participantCs.setId("1234");
        participantCs.setName("Test");
        participantCs.setLegalRegistrationNumber(List.of(new NodeKindIRITypeId("1234-regId")));

        ExtendedVerifiableCredential participantEvc = ExtendedVerifiableCredential.fromMap(VerifiableCredential
                .builder().id(URI.create("1234#participant"))
                .credentialSubject(CastableCredentialSubject.fromPojo(participantCs)).build().getJsonObject());

        GxLegalRegistrationNumberCredentialSubject regNumCs = new GxLegalRegistrationNumberCredentialSubject();
        regNumCs.setId("1234-regId");
        regNumCs.setLeiCode("123456");

        ExtendedVerifiableCredential regNumEvc = ExtendedVerifiableCredential.fromMap(VerifiableCredential
                .builder().id(URI.create("1234-regId#regNum"))
                .credentialSubject(CastableCredentialSubject.fromPojo(regNumCs)).build().getJsonObject());

        ExtendedVerifiablePresentation evp = new ExtendedVerifiablePresentation();
        evp.setVerifiableCredentials(List.of(participantEvc, regNumEvc));

        // extract participant claims from VP
        List<GxLegalParticipantCredentialSubject> foundParticipants =
                evp.findAllCredentialSubjectsByType(GxLegalParticipantCredentialSubject.class);
        assertEquals(1, foundParticipants.size());
        assertEquals(participantCs.getId(), foundParticipants.get(0).getId());
        assertEquals(participantCs.getName(), foundParticipants.get(0).getName());
        assertEquals(participantCs.getLegalRegistrationNumber().get(0).getId(),
                foundParticipants.get(0).getLegalRegistrationNumber().get(0).getId());

        GxLegalParticipantCredentialSubject foundParticipant = evp
                .findFirstCredentialSubjectByType(GxLegalParticipantCredentialSubject.class);
        assertNotNull(foundParticipant);
        assertEquals(participantCs.getId(), foundParticipant.getId());
        assertEquals(participantCs.getName(), foundParticipant.getName());
        assertEquals(participantCs.getLegalRegistrationNumber().get(0).getId(),
                foundParticipant.getLegalRegistrationNumber().get(0).getId());

        // extract registration number claims from VP
        List<GxLegalRegistrationNumberCredentialSubject> foundRegNums =
                evp.findAllCredentialSubjectsByType(GxLegalRegistrationNumberCredentialSubject.class);
        assertEquals(1, foundRegNums.size());
        assertEquals(regNumCs.getId(), foundRegNums.get(0).getId());
        assertEquals(regNumCs.getLeiCode(), foundRegNums.get(0).getLeiCode());

        GxLegalRegistrationNumberCredentialSubject foundRegNum =
                evp.findFirstCredentialSubjectByType(GxLegalRegistrationNumberCredentialSubject.class);
        assertEquals(regNumCs.getId(), foundRegNum.getId());
        assertEquals(regNumCs.getLeiCode(), foundRegNum.getLeiCode());

        // expect to find no offering credentials
        List<GxServiceOfferingCredentialSubject> foundOfferings =
                evp.findAllCredentialSubjectsByType(GxServiceOfferingCredentialSubject.class);
        assertTrue(foundOfferings.isEmpty());

        GxServiceOfferingCredentialSubject foundOffering =
                evp.findFirstCredentialSubjectByType(GxServiceOfferingCredentialSubject.class);
        assertNull(foundOffering);
    }
}