package eu.merloteducation.gxfscataloglibrary.service;

import eu.merloteducation.modelslib.gxfscatalog.participants.ParticipantItem;
import eu.merloteducation.modelslib.gxfscatalog.query.GXFSQueryUriItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionCredentialSubject;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.modelslib.gxfscatalog.selfdescriptions.SelfDescriptionsCreateResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface GxfsCatalogClient {

    // offering orchestrator
    // keycloakAuthService.webCallAuthenticated(
    //                HttpMethod.POST,
    //                gxfscatalogSelfdescriptionsUri + "/" + extension.getCurrentSdHash() + "/revoke",
    //                "",
    //                null);
    @PostExchange("/self-descriptions/{sdHash}")
    SelfDescriptionsCreateResponse postRevokeSelfDescriptionByHash(@PathVariable String sdHash);

    // keycloakAuthService.webCallAuthenticated(
    //                HttpMethod.DELETE,
    //                gxfscatalogSelfdescriptionsUri + "/" + extension.getCurrentSdHash(),
    //                "",
    //                null);
    @DeleteExchange("/self-descriptions/{sdHash}")
    void deleteSelfDescriptionByHash(@PathVariable String sdHash);

    // String response = keycloakAuthService.webCallAuthenticated(
    //                HttpMethod.GET,
    //                gxfscatalogSelfdescriptionsUri + "?withContent=true&statuses=ACTIVE,REVOKED&ids=" + extension.getId(),
    //                "",
    //                null);
    // String response = keycloakAuthService.webCallAuthenticated(
    //                HttpMethod.GET,
    //                gxfscatalogSelfdescriptionsUri + "?withContent=true&statuses=ACTIVE&hashes=" + extensionHashes,
    //                "",
    //                null);
    // String sdResponseString = keycloakAuthService.webCallAuthenticated(HttpMethod.GET,
    //            gxfscatalogSelfdescriptionsUri + "?statuses=ACTIVE&withContent=true&ids=" + urisString, "", null);
    // TODO more generic type?
    @GetExchange("/self-descriptions")
    GXFSCatalogListResponse
            <SelfDescriptionItem
                    <SelfDescriptionCredentialSubject>> getSelfDescriptionList(
                            @RequestParam("withContent") boolean withContent,
                            @RequestParam("statuses") String[] statuses,
                            @RequestParam("ids") String[] ids,
                            @RequestParam("hashes") String[] hashes);


    // response = keycloakAuthService.webCallAuthenticated(
    //                    HttpMethod.POST,
    //                    gxfscatalogSelfdescriptionsUri,
    //                    signedVp,
    //                    MediaType.APPLICATION_JSON); // TODO is mediatype needed?
    @PostExchange("/self-descriptions")
    SelfDescriptionsCreateResponse postAddSelfDescription(@RequestBody String body);

    // orga orchestrator
    // String response = keycloakAuthService.webCallAuthenticated(HttpMethod.GET,
    //            URI.create(gxfscatalogParticipantsUri + "/Participant:" + id).toString(), "", null);
    @GetExchange("/participants/{participantId}")
    ParticipantItem getParticipantById(@PathVariable String participantId); // TODO unescape needed?

    // response = keycloakAuthService.webCallAuthenticated(HttpMethod.PUT,
    //                gxfscatalogParticipantsUri + "/" + targetCredentialSubject.getId(), signedVp,
    //                MediaType.APPLICATION_JSON); // TODO is mediatype needed?
    @PutExchange("/participants/{participantId}")
    ParticipantItem putUpdateParticipant(@PathVariable String participantId, @RequestBody String body);

    // response = keycloakAuthService.webCallAuthenticated(HttpMethod.POST, gxfscatalogParticipantsUri, signedVp,
    //                MediaType.APPLICATION_JSON); // TODO is mediatype needed?
    @PostExchange("/participants")
    ParticipantItem postAddParticipant(@RequestBody String body);

    // String queryResponse = keycloakAuthService.webCallAuthenticated(HttpMethod.POST, gxfscatalogQueryUri, """
    //            {
    //                "statement": "MATCH (p:MerlotOrganization) return p.uri ORDER BY toLower(p.orgaName)""" + " SKIP "
    //            + pageable.getOffset() + " LIMIT " + pageable.getPageSize() + """
    //            "
    //            }
    //            """, MediaType.APPLICATION_JSON); // TODO is mediatype needed?
    @PostExchange("/query")
    GXFSCatalogListResponse<GXFSQueryUriItem> postQuery(@RequestBody String query);

}
