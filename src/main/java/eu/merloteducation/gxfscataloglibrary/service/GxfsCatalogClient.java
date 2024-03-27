package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryRequest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryLegalNameItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
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
    GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionList(
            @RequestParam(name = "uploadTimerange", required = false) String uploadTimerange,
            @RequestParam(name = "statusTimerange", required = false) String statusTimerange,
            @RequestParam(name = "issuers", required = false) String[] issuers,
            @RequestParam(name = "validators", required = false) String[] validators,
            @RequestParam(name = "statuses", required = false) SelfDescriptionStatus[] statuses,
            @RequestParam(name = "ids", required = false) String[] ids,
            @RequestParam(name = "hashes", required = false) String[] hashes,
            @RequestParam(name = "withMeta", required = false) boolean withMeta,
            @RequestParam(name = "withContent", required = false) boolean withContent,
            @RequestParam(name = "offset", required = false) int offset,
            @RequestParam(name = "limit", required = false) int limit
    );

    @PostExchange("/self-descriptions")
    SelfDescriptionMeta postAddSelfDescription(@RequestBody VerifiablePresentation body);

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
            @RequestParam(name = "timeout", required = false) int timeout,
            @RequestParam(name = "withTotalCount", required = false) boolean withTotalCount,
            @RequestBody QueryRequest query
    );

    // TODO Schemas
    // TODO Verification

    // Participants
    @GetExchange("/participants")
    GXFSCatalogListResponse<ParticipantItem> getParticipants(
            @RequestParam(name = "offset", required = false) int offset,
            @RequestParam(name = "limit", required = false) int limit
    );

    @PostExchange("/participants")
    ParticipantItem postAddParticipant(@RequestBody VerifiablePresentation body);

    @GetExchange("/participants/{participantId}")
    ParticipantItem getParticipantById(@PathVariable String participantId);

    @PutExchange("/participants/{participantId}")
    ParticipantItem putUpdateParticipant(
            @PathVariable String participantId,
            @RequestBody VerifiablePresentation body
    );

    @DeleteExchange("/participants/{participantId}")
    ParticipantItem deleteParticipant(@PathVariable String participantId);

    // TODO GET /participants/{participantId}/users

    // TODO Users
    // TODO Roles
    // TODO Session
}
