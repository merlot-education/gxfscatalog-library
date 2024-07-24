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

import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryRequest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionMeta;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

// based on https://gitlab.eclipse.org/eclipse/xfsc/cat/fc-service/-/blob/518b171dd342da92ad24cdd5c0349e3edb4acf18/openapi/fc_openapi.yaml
public interface GxfsCatalogClient {

    // SelfDescriptions
    @GetExchange("/self-descriptions")
    GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionList( // NOSONAR nothing we can do about the parameter count...
            @RequestParam(name = "uploadTimerange", required = false) String uploadTimerange,
            @RequestParam(name = "statusTimerange", required = false) String statusTimerange,
            @RequestParam(name = "issuers", required = false) String[] issuers,
            @RequestParam(name = "validators", required = false) String[] validators,
            @RequestParam(name = "statuses", required = false) SelfDescriptionStatus[] statuses,
            @RequestParam(name = "ids", required = false) String[] ids,
            @RequestParam(name = "hashes", required = false) String[] hashes,
            @RequestParam(name = "withMeta", required = false) Boolean withMeta,
            @RequestParam(name = "withContent", required = false) Boolean withContent,
            @RequestParam(name = "offset", required = false) Integer offset,
            @RequestParam(name = "limit", required = false) Integer limit
    );

    @PostExchange("/self-descriptions")
    SelfDescriptionMeta postAddSelfDescription(@RequestBody ExtendedVerifiablePresentation body);

    @GetExchange("/self-descriptions/{sdHash}")
    SelfDescriptionItem getSelfDescriptionByHash(@PathVariable String sdHash);

    @DeleteExchange("/self-descriptions/{sdHash}")
    void deleteSelfDescriptionByHash(@PathVariable String sdHash);

    @PostExchange("/self-descriptions/{sdHash}/revoke")
    SelfDescriptionMeta postRevokeSelfDescriptionByHash(@PathVariable String sdHash);

    // Query
    @PostExchange("/query")
    <T> GXFSCatalogListResponse<T> postQuery(
            @RequestParam(name = "queryLanguage", required = false) QueryLanguage queryLanguage,
            @RequestParam(name = "timeout", required = false) Integer timeout,
            @RequestParam(name = "withTotalCount", required = false) Boolean withTotalCount,
            @RequestBody QueryRequest query
    );

    // not implemented: Schemas
    // not implemented: Verification

    // Participants
    @GetExchange("/participants")
    GXFSCatalogListResponse<ParticipantItem> getParticipants(
            @RequestParam(name = "offset", required = false) Integer offset,
            @RequestParam(name = "limit", required = false) Integer limit
    );

    @PostExchange("/participants")
    ParticipantItem postAddParticipant(@RequestBody ExtendedVerifiablePresentation body);

    @GetExchange("/participants/{participantId}")
    ParticipantItem getParticipantById(@PathVariable String participantId);

    @PutExchange("/participants/{participantId}")
    ParticipantItem putUpdateParticipant(
            @PathVariable String participantId,
            @RequestBody ExtendedVerifiablePresentation body
    );

    @DeleteExchange("/participants/{participantId}")
    ParticipantItem deleteParticipant(@PathVariable String participantId);

    // not implemented: GET /participants/{participantId}/users

    // not implemented: Users
    // not implemented: Roles
    // not implemented: Session
}
