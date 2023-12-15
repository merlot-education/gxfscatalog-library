package eu.merloteducation.gxfscataloglibrary.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.PostExchange;

public interface GxfsCatalogClient {

    // offering orchestrator
    // keycloakAuthService.webCallAuthenticated(
    //                HttpMethod.POST,
    //                gxfscatalogSelfdescriptionsUri + "/" + extension.getCurrentSdHash() + "/revoke",
    //                "",
    //                null);
    @PostExchange("/self-descriptions/{sdHash}")
    void revokeSelfDescription(@PathVariable String sdHash);

    // keycloakAuthService.webCallAuthenticated(
    //                HttpMethod.DELETE,
    //                gxfscatalogSelfdescriptionsUri + "/" + extension.getCurrentSdHash(),
    //                "",
    //                null);
    @PostExchange("/self-descriptions/{sdHash}")
    void deleteSelfDescription(@PathVariable String sdHash);

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

    // response = keycloakAuthService.webCallAuthenticated(
    //                    HttpMethod.POST,
    //                    gxfscatalogSelfdescriptionsUri,
    //                    signedVp,
    //                    MediaType.APPLICATION_JSON);

    // orga orchestrator
    // String response = keycloakAuthService.webCallAuthenticated(HttpMethod.GET,
    //            URI.create(gxfscatalogParticipantsUri + "/Participant:" + id).toString(), "", null);

    // String queryResponse = keycloakAuthService.webCallAuthenticated(HttpMethod.POST, gxfscatalogQueryUri, """
    //            {
    //                "statement": "MATCH (p:MerlotOrganization) return p.uri ORDER BY toLower(p.orgaName)""" + " SKIP "
    //            + pageable.getOffset() + " LIMIT " + pageable.getPageSize() + """
    //            "
    //            }
    //            """, MediaType.APPLICATION_JSON);

    // String sdResponseString = keycloakAuthService.webCallAuthenticated(HttpMethod.GET,
    //            gxfscatalogSelfdescriptionsUri + "?statuses=ACTIVE&withContent=true&ids=" + urisString, "", null);

    // response = keycloakAuthService.webCallAuthenticated(HttpMethod.PUT,
    //                gxfscatalogParticipantsUri + "/" + targetCredentialSubject.getId(), signedVp,
    //                MediaType.APPLICATION_JSON);

    // response = keycloakAuthService.webCallAuthenticated(HttpMethod.POST, gxfscatalogParticipantsUri, signedVp,
    //                MediaType.APPLICATION_JSON);

}
