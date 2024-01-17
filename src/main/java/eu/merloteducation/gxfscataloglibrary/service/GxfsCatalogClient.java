package eu.merloteducation.gxfscataloglibrary.service;

import eu.merloteducation.gxfscataloglibrary.models.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.SelfDescriptionStatus;
import eu.merloteducation.modelslib.gxfscatalog.participants.ParticipantItem;
import eu.merloteducation.modelslib.gxfscatalog.query.GXFSQueryUriItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionsCreateResponse;
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
    SelfDescriptionsCreateResponse postAddSelfDescription(@RequestBody String body);

    @GetExchange("/self-descriptions/{sdHash}")
    GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionByHash(@PathVariable String sdHash);

    @DeleteExchange("/self-descriptions/{sdHash}")
    void deleteSelfDescriptionByHash(@PathVariable String sdHash);

    @PostExchange("/self-descriptions/{sdHash}/revoke")
    SelfDescriptionsCreateResponse postRevokeSelfDescriptionByHash(@PathVariable String sdHash);

    // Query
    // TODO check if media type is necessary
    @PostExchange("/query")
    GXFSCatalogListResponse<GXFSQueryUriItem> postQuery(
            @RequestParam(name = "queryLanguage", required = false) QueryLanguage queryLanguage,
            @RequestParam(name = "timeout", required = false) int timeout,
            @RequestParam(name = "withTotalCount", required = false) boolean withTotalCount,
            @RequestBody String query
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
    ParticipantItem postAddParticipant(@RequestBody String body);

    @GetExchange("/participants/{participantId}")
    ParticipantItem getParticipantById(@PathVariable String participantId);

    @PutExchange("/participants/{participantId}")
    ParticipantItem putUpdateParticipant(@PathVariable String participantId, @RequestBody String body);

    @DeleteExchange("/participants/{participantId}")
    ParticipantItem deleteParticipant(@PathVariable String participantId);

    @GetExchange("/participants/{participantId}/users")
    ParticipantItem getParticipantUsers(
            @PathVariable String participantId,
            @RequestParam(name = "offset", required = false) int offset,
            @RequestParam(name = "limit", required = false) int limit
    );

    // TODO Users
    // TODO Roles
    // TODO Session
}
