package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.danubetech.verifiablecredentials.VerifiablePresentation;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryRequest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryLegalNameItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.*;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalParticipantCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.serviceofferings.GxServiceOfferingCredentialSubject;
import foundation.identity.jsonld.ConfigurableDocumentLoader;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
public class GxfsCatalogService {

    private final Logger logger = LoggerFactory.getLogger(GxfsCatalogService.class);

    private final String defaultVerificationMethod;

    private final String defaultCertPath;

    private final String defaultPrivateKey;

    private final boolean enforceCompliance;

    private final boolean enforceNotary;

    private final GxfsCatalogClient gxfsCatalogClient;

    private final GxfsSignerService gxfsSignerService;

    private final GxdchService gxdchService;

    private final WebClient webClient;

    private final ObjectMapper objectMapper;

    public GxfsCatalogService(@Autowired GxfsCatalogClient gxfsCatalogClient,
                              @Autowired GxfsSignerService gxfsSignerService,
                              @Autowired GxdchService gxdchService,
                              @Autowired WebClient webClient,
                              @Autowired ObjectMapper objectMapper,
                              @Value("${gxfscatalog.verification-method:#{null}}") String defaultVerificationMethod,
                              @Value("${gxfscatalog.cert-path:#{null}}") String defaultCertPath,
                              @Value("${gxfscatalog.private-key-path:#{null}}") String defaultPrivateKey,
                              @Value("${gxdch-services.enforce-compliance:#{false}}") boolean enforceCompliance,
                              @Value("${gxdch-services.enforce-notary:#{false}}") boolean enforceNotary) {
        this.gxfsCatalogClient = gxfsCatalogClient;
        this.gxfsSignerService = gxfsSignerService;
        this.gxdchService = gxdchService;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.defaultVerificationMethod = defaultVerificationMethod;
        this.defaultCertPath = defaultCertPath;
        this.defaultPrivateKey = defaultPrivateKey;
        this.enforceCompliance = enforceCompliance;
        this.enforceNotary = enforceNotary;
    }

    /**
     * Given the hash of a self-description in the catalog, set its status in the catalog to revoked.
     *
     * @param sdHash hash of the SD
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta revokeSelfDescriptionByHash(String sdHash) {
        return this.gxfsCatalogClient.postRevokeSelfDescriptionByHash(sdHash);
    }

    /**
     * Given the hash of a self-description in the catalog, fully delete/purge it from the catalog.
     *
     * @param sdHash hash of the SD
     */
    public void deleteSelfDescriptionByHash(String sdHash) {
        this.gxfsCatalogClient.deleteSelfDescriptionByHash(sdHash);
    }

    /**
     * Given the id of a participant in the catalog, return the respective item including the self-description.
     *
     * @param participantId hash of the SD
     * @return catalog content of the participant
     */
    public ParticipantItem getParticipantById(String participantId) {
        return this.gxfsCatalogClient.getParticipantById(participantId);
    }

    /**
     * Given a list of ids of self-descriptions in the catalog, return a list of self-description items that match these ids.
     * By default, this method will only return SDs with the status ACTIVE.
     * If other statuses are required, use the overloaded function with the same name.
     *
     * @param ids array of ids to query the catalog for
     * @return list of SD items that match the ids
     */
    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByIds(String[] ids) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                new SelfDescriptionStatus[]{SelfDescriptionStatus.ACTIVE},
                ids,
                null,
                true,
                true,
                0,
                ids.length);
    }

    /**
     * Given a list of ids of self-descriptions in the catalog, return a list of self-description items that match these ids.
     * This overload allows to further specify which states the SDs shall have in the catalog.
     *
     * @param ids array of ids to query the catalog for
     * @param selfDescriptionStatuses array of wanted SD statuses
     * @return list of SD items that match the ids
     */
    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByIds(String[] ids,
                                                                                 SelfDescriptionStatus[] selfDescriptionStatuses) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                selfDescriptionStatuses,
                ids,
                null,
                true,
                true,
                0,
                ids.length);
    }

    /**
     * Given a list of hashes of self-descriptions in the catalog, return a list of self-description items that match these hashes.
     * By default, this method will only return SDs with the status ACTIVE.
     * If other statuses are required, use the overloaded function with the same name.
     *
     * @param hashes array of hashes to query the catalog for
     * @return list of SD items that match the hashes
     */
    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByHashes(String[] hashes) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                new SelfDescriptionStatus[]{SelfDescriptionStatus.ACTIVE},
                null,
                hashes,
                true,
                true,
                0,
                hashes.length);
    }

    /**
     * Given a list of hashes of self-descriptions in the catalog, return a list of self-description items that match these hashes.
     * This overload allows to further specify which states the SDs shall have in the catalog.
     *
     * @param hashes array of hashes to query the catalog for
     * @param selfDescriptionStatuses array of wanted SD statuses
     * @return list of SD items that match the hashes
     */
    public GXFSCatalogListResponse<SelfDescriptionItem> getSelfDescriptionsByHashes(String[] hashes,
                                                                                    SelfDescriptionStatus[] selfDescriptionStatuses) {
        return this.gxfsCatalogClient.getSelfDescriptionList(
                null,
                null,
                null,
                null,
                selfDescriptionStatuses,
                null,
                hashes,
                true,
                true,
                0,
                hashes.length);
    }

    /**
     * Given a list of PojoCredentialSubject that correspond to a gx:ServiceOffering,
     * wrap them in a verifiable presentation, sign it using the default verification method and key and send it to the catalog.
     *
     * @param credentialSubjects List of credential subjects for this offering to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta addServiceOffering(
            List<PojoCredentialSubject> credentialSubjects)
            throws CredentialPresentationException, CredentialSignatureException {
        return addServiceOffering(credentialSubjects, defaultVerificationMethod, getDefaultPrivateKey());
    }

    /**
     * Given a list of PojoCredentialSubject that correspond to a gx:ServiceOffering,
     * wrap them in a verifiable presentation, sign it using the provided verification method and the default key and send it to the catalog.
     * The verification method must reference the certificate associated with the default key.
     *
     * @param credentialSubjects List of credential subjects for this offering to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta addServiceOffering(
            List<PojoCredentialSubject> credentialSubjects, String verificationMethod)
        throws CredentialPresentationException, CredentialSignatureException {
        return addServiceOffering(credentialSubjects, verificationMethod, getDefaultPrivateKey());
    }

    /**
     * Given a list of PojoCredentialSubject that correspond to a gx:ServiceOffering,
     * wrap them in a verifiable presentation, sign it using the provided verification method and key and send it to the catalog.
     *
     * @param credentialSubjects List of credential subjects for this offering to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @param privateKey string representation of private key to sign the SD with
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta addServiceOffering(
            List<PojoCredentialSubject> credentialSubjects,
            String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {

        // make sure there is at least one service offering CS
        List<PojoCredentialSubject> offeringCredentialSubjects =
                findAllCredentialSubjectsByType(credentialSubjects, GxServiceOfferingCredentialSubject.class);
        if (offeringCredentialSubjects.isEmpty()) {
            throw new CredentialPresentationException(
                    "Could not find " + GxServiceOfferingCredentialSubject.TYPE + " in list of credential subjects.");
        }

        String providerId = offeringCredentialSubjects.stream()
                .filter(GxServiceOfferingCredentialSubject.class::isInstance)
                .map(cs -> ((GxServiceOfferingCredentialSubject) cs).getProvidedBy().getId())
                .filter(Objects::nonNull)
                .findFirst().orElse("");

        PrivateKey prk = buildPrivateKey(privateKey);
        List<X509Certificate> certificates = resolveCertificates(verificationMethod);

        // remove credentials for compliance from overall list
        List<PojoCredentialSubject> nonCompliantCsList = new ArrayList<>(credentialSubjects);
        nonCompliantCsList.removeAll(offeringCredentialSubjects);

        // generate compliance vc potentially containing compliance credential
        ExtendedVerifiablePresentation vp = getComplianceVp(offeringCredentialSubjects,
                verificationMethod, providerId, prk, certificates);

        // copy credential list as it is likely immutable
        List<ExtendedVerifiableCredential> credentialList = new ArrayList<>(vp.getVerifiableCredentials());

        // handle remaining (non-compliant) credentials
        for (PojoCredentialSubject cs : nonCompliantCsList) {
            VerifiableCredential vc = getSignedVc(cs, providerId, verificationMethod, prk, certificates);
            credentialList.add(ExtendedVerifiableCredential.fromMap(vc.getJsonObject()));
        }

        // update credentials in vp
        vp.setVerifiableCredentials(credentialList);

        // sign verifiable presentation for catalog storage
        gxfsSignerService.signVerifiablePresentation(vp, verificationMethod, prk, certificates);

        return gxfsCatalogClient.postAddSelfDescription(vp);
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson,
     * wrap it in a verifiable presentation, sign it using the default verification method and key and send it to the catalog.
     *
     * @param credentialSubjects List of credential subjects for this participant to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem addParticipant(List<PojoCredentialSubject> credentialSubjects)
            throws CredentialPresentationException, CredentialSignatureException {
        return addParticipant(credentialSubjects, defaultVerificationMethod, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson,
     * wrap it in a verifiable presentation, sign it using the provided verification method and the default key and send it to the catalog.
     * The verification method must reference the certificate associated with the default key.
     *
     * @param credentialSubjects List of credential subjects for this participant to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem addParticipant(List<PojoCredentialSubject> credentialSubjects,
                                          String verificationMethod)
        throws CredentialPresentationException, CredentialSignatureException {
        return addParticipant(credentialSubjects, verificationMethod, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson,
     * wrap it in a verifiable presentation, sign it using the provided verification method and key and send it to the catalog.
     *
     * @param credentialSubjects List of credential subjects for this participant to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @param privateKey string representation of private key to sign the SD with
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem addParticipant(List<PojoCredentialSubject> credentialSubjects,
                                          String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {
        return this.gxfsCatalogClient.postAddParticipant(
                getSignedParticipantVp(credentialSubjects, verificationMethod, privateKey));
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson
     * and whose id already exists in the catalog, wrap it in a verifiable presentation,
     * sign it using the default verification method and key and send it to the catalog
     * to update it.
     *
     * @param credentialSubjects List of credential subjects for this participant to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem updateParticipant(List<PojoCredentialSubject> credentialSubjects)
            throws CredentialPresentationException, CredentialSignatureException {
        return updateParticipant(credentialSubjects, defaultVerificationMethod, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson
     * and whose id already exists in the catalog, wrap it in a verifiable presentation,
     * sign it using the provided verification method and the default key and send it to the catalog
     * to update it.
     * The verification method must reference the certificate associated with the default key.
     *
     * @param credentialSubjects List of credential subjects for this participant to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem updateParticipant(List<PojoCredentialSubject> credentialSubjects,
                                             String verificationMethod)
        throws CredentialPresentationException, CredentialSignatureException {
        return updateParticipant(credentialSubjects, verificationMethod, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson
     * and whose id already exists in the catalog, wrap it in a verifiable presentation,
     * sign it using the provided verification method and key and send it to the catalog
     * to update it.
     *
     * @param credentialSubjects List of credential subjects for this participant to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @param privateKey string representation of private key to sign the SD with
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem updateParticipant(List<PojoCredentialSubject> credentialSubjects,
                                             String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {

        String subjectId
                = findAllCredentialSubjectsByType(credentialSubjects, GxLegalParticipantCredentialSubject.class)
                .stream()
                .map(cs -> cs.getId())
                .findFirst().orElse("");

        ExtendedVerifiablePresentation vp
                = getSignedParticipantVp(credentialSubjects, verificationMethod, privateKey);

        return this.gxfsCatalogClient.putUpdateParticipant(
                subjectId,
                vp);
    }

    /**
     * Given paging parameters, return a list (page) of participant URIs sorted by some field
     * corresponding to these parameters.
     * This is mainly used to access the participant self-descriptions in a second step, by using the
     * uris as IDs and requesting SDs with these IDs.
     *
     * @param participantType type of the participant to query for, e.g. LegalPerson or MerlotOrganisation
     * @param sortField field of the participant to sort by
     * @param offset paging offset
     * @param size page size
     * @return list of participant uris corresponding to the paging parameters
     */
    public GXFSCatalogListResponse<GXFSQueryUriItem> getSortedParticipantUriPage(
            String participantType, String sortField, long offset, long size) {
        QueryRequest query = new QueryRequest(getMatchParticipantTypeString(participantType)
                + " return p.uri ORDER BY toLower(p." + sortField + ")"
                + " SKIP " + offset + " LIMIT " + size);
        GXFSCatalogListResponse<Map<String, Object>> response = this.gxfsCatalogClient.postQuery(
            QueryLanguage.OPENCYPHER,
            5,
            true,
            query);

        return objectMapper.convertValue(response, new TypeReference<GXFSCatalogListResponse<GXFSQueryUriItem>>() {});
    }

    /**
     * Given paging parameters and excluded uris, return a list (page) of participant URIs sorted by some field
     * corresponding to these parameters.
     * This is mainly used to access the participant self-descriptions in a second step, by using the
     * uris as IDs and requesting SDs with these IDs.
     *
     * @param participantType type of the participant to query for, e.g. LegalPerson or MerlotOrganisation
     * @param sortField field of the participant to sort by
     * @param excludedUris list of uris to exclude
     * @param offset paging offset
     * @param size page size
     * @return list of participant uris corresponding to the paging parameters
     */
    public GXFSCatalogListResponse<GXFSQueryUriItem> getSortedParticipantUriPageWithExcludedUris(
        String participantType, String sortField, List<String> excludedUris, long offset, long size) {
        String excludedUrisString = listToString(excludedUris);
        QueryRequest query = new QueryRequest(getMatchParticipantTypeString(participantType)
            + " WHERE NOT p.uri IN " + excludedUrisString
            + " return p.uri ORDER BY toLower(p." + sortField + ")"
            + " SKIP " + offset + " LIMIT " + size);
        GXFSCatalogListResponse<Map<String, Object>> response = this.gxfsCatalogClient.postQuery(
            QueryLanguage.OPENCYPHER,
            5,
            true,
            query);

        return objectMapper.convertValue(response, new TypeReference<GXFSCatalogListResponse<GXFSQueryUriItem>>() {});
    }

    /**
     * Given the uri of a participant, return (a list containing) the legal name of the participant.
     *
     * @param participantType type of the participant to query for, e.g. LegalPerson or MerlotOrganisation
     * @param participantUri uri of the participant
     * @return list containing the legal name of the participant
     */
    public GXFSCatalogListResponse<GXFSQueryLegalNameItem> getParticipantLegalNameByUri(
        String participantType, String participantUri) {
        QueryRequest query = new QueryRequest(getMatchParticipantTypeString(participantType)
            + " WHERE p.uri = \"" + participantUri + "\""
            + " return p.legalName");

        GXFSCatalogListResponse<Map<String, Object>> response = this.gxfsCatalogClient.postQuery(
            QueryLanguage.OPENCYPHER,
            5,
            true,
            query);

        return objectMapper.convertValue(response, new TypeReference<>() {
        });
    }

    private String listToString(List<String> stringList) {
        StringBuilder result = new StringBuilder("[");

        for (int i = 0; i < stringList.size(); i++) {
            result.append("\"").append(stringList.get(i)).append("\"");

            // Add a comma if it's not the last element
            if (i < stringList.size() - 1) {
                result.append(", ");
            }
        }

        result.append("]");

        return result.toString();
    }

    /**
     * Given a verification method, resolve it to its certificates.
     * Currently, only did:web is supported. If the given method is anything else, it will return an empty list.
     *
     * @param verificationMethod method to resolve
     * @throws CredentialSignatureException failed to resolve given did:web certificate
     * @return list of certificates
     */
    private List<X509Certificate> resolveCertificates(String verificationMethod) throws CredentialSignatureException {

        if (!verificationMethod.startsWith("did:web:")) {
            logger.warn("Failed to validate verificationMethod {} as it is not a did:web, will skip validation.", verificationMethod);
            return Collections.emptyList();
        }

        if (verificationMethod.equals(defaultVerificationMethod)) {
            logger.info("Using default verificationMethod {}, skipping web request.", verificationMethod);
            try {
                return buildCertficates(getDefaultCertificate());
            } catch (CredentialSignatureException e) {
                logger.warn("Failed to load default certificate, will skip validation.");
                return Collections.emptyList();
            }
        }

        // at this point we have an unknown did:web, try to resolve it
        try {
            JsonNode didDocument = requestDidDocument(verificationMethod);
            ArrayNode verificationMethods = (ArrayNode) didDocument.get("verificationMethod");
            for (JsonNode vm : verificationMethods) {
                String vmId = vm.get("id").asText();
                if (vmId.equals(verificationMethod)) {
                    return buildCertficates(requestCertificate(vm));
                }
            }
            throw new CredentialSignatureException("Could not find certificate for given verification method " +
                    verificationMethod);
        } catch (NullPointerException | WebClientResponseException e) {
            throw new CredentialSignatureException("Error during did:web resolving: " + e.getMessage());
        }
    }

    /**
     * Request the did.json document for a given did:web
     *
     * @param verificationMethod did:web
     * @return did document json
     */
    private JsonNode requestDidDocument(String verificationMethod) {
        // resolve did:web certificate
        String didWeb = verificationMethod
                .replace("did:web:", "") // remove did type prefix
                .replaceFirst("#.*", ""); // remove verification method reference
        String didDocumentUri = getDidDocumentUri(didWeb);
        JsonNode didDocument = webClient.get().uri(didDocumentUri).retrieve().bodyToMono(JsonNode.class).block();
        return Objects.requireNonNull(didDocument, "Failed to retrieve did-document at " + didDocumentUri);
    }

    /**
     * Given the domain part of the did:web, return the resulting URI.
     * See <a href="https://w3c-ccg.github.io/did-method-web/#read-resolve">did-web specification</a> for reference.
     *
     * @param didWeb did:web without prefix and key reference
     * @return did web URI
     */
    private static String getDidDocumentUri(String didWeb) {
        boolean containsSubpath = didWeb.contains(":");
        StringBuilder didDocumentUriBuilder = new StringBuilder();
        didDocumentUriBuilder.append(didWeb
                .replace(":", "/") // Replace ":" with "/" in the method specific identifier to
                                                    // obtain the fully qualified domain name and optional path.
                .replace("%3A", ":")); // If the domain contains a port percent decode the colon.

        // Generate an HTTPS URL to the expected location of the DID document by prepending https://.
        didDocumentUriBuilder.insert(0, "https://");
        if (!containsSubpath) {
            // If no path has been specified in the URL, append /.well-known.
            didDocumentUriBuilder.append("/.well-known");
        }
        // Append /did.json to complete the URL.
        didDocumentUriBuilder.append("/did.json");

        return didDocumentUriBuilder.toString();
    }

    /**
     * Request the certificate that is linked in the given verification method entry of a did.json document.
     *
     * @param didDocumentEntry verification method entry of did.json
     * @return string representation of corresponding certificate
     */
    private String requestCertificate(JsonNode didDocumentEntry) {
        // resolve did:web certificate
        String certUrl = didDocumentEntry.get("publicKeyJwk").get("x5u").asText();
        String certificate = webClient.get().uri(certUrl)
                .retrieve().bodyToMono(String.class).block();
        return Objects.requireNonNull(certificate, "Failed to retrieve certificate at " + certUrl);
    }

    /**
     * Given a string representation of the private key, convert it to it's the object representation.
     *
     * @param prk string representation of the private key
     * @throws CredentialSignatureException failed to decode private key
     * @return PrivateKey object
     */
    private PrivateKey buildPrivateKey(String prk) throws CredentialSignatureException {
        try {
            PEMParser pemParser = new PEMParser(new StringReader(prk));
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());
            return converter.getPrivateKey(privateKeyInfo);
        } catch (IOException e) {
            throw new CredentialSignatureException("Failed to parse private key. " + e.getMessage());
        }
    }

    /**
     * Given a string representation of the certificates, convert them to a list of their object representations.
     *
     * @param certs string representation of the certificates
     * @throws CredentialSignatureException failed to decode certificate
     * @return list of X509 certificate objects
     */
    private List<X509Certificate> buildCertficates(String certs) throws CredentialSignatureException {
        try {
            ByteArrayInputStream certStream = new ByteArrayInputStream(certs.getBytes(StandardCharsets.UTF_8));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<X509Certificate>) certFactory.generateCertificates(certStream);
        } catch (CertificateException e) {
            throw new CredentialSignatureException("Failed to parse certificate. " + e.getMessage());
        }
    }

    /**
     * Load the default private key corresponding to the defaultVerificationMethod.
     *
     * @return string representation of private key
     * @throws CredentialSignatureException error during loading of private key
     */
    private String getDefaultPrivateKey() throws CredentialSignatureException {
        try (InputStream privateKeyStream = StringUtil.isNullOrEmpty(defaultPrivateKey) ?
                GxfsCatalogService.class.getClassLoader().getResourceAsStream("prk.ss.pem")
                : new FileInputStream(defaultPrivateKey)) {
            return privateKeyStream == null ? null :
                    new String(privateKeyStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CredentialSignatureException("Failed to read default private key. " + e.getMessage());
        }
    }

    /**
     * Load the default certificate corresponding to the defaultVerificationMethod.
     *
     * @return string representation of certificate
     * @throws CredentialSignatureException error during loading of private key
     */
    private String getDefaultCertificate() throws CredentialSignatureException {
        try (InputStream certificateStream = StringUtil.isNullOrEmpty(defaultCertPath) ?
                GxfsCatalogService.class.getClassLoader().getResourceAsStream("cert.ss.pem")
                : new FileInputStream(defaultCertPath)) {
            return new String(Objects.requireNonNull(certificateStream,
                    "Certificate input stream is null.").readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new CredentialSignatureException("Failed to read default certificate. " + e.getMessage());
        }
    }

    private String getMatchParticipantTypeString(String participantType) {
        return "MATCH (p:" + participantType + ")";
    }

    public <T extends PojoCredentialSubject> List<PojoCredentialSubject> findAllCredentialSubjectsByType(
            List<PojoCredentialSubject> credentialSubjects, Class<T> type) {
        List<PojoCredentialSubject> matchingCsList = new ArrayList<>();
        for (PojoCredentialSubject cs : credentialSubjects) {
            if (type.isInstance(cs)) {
                matchingCsList.add(cs);
            }
        }
        return matchingCsList;
    }

    private ExtendedVerifiableCredential getSignedRegistrationNumberVc(GxLegalRegistrationNumberCredentialSubject cs,
                                                                       String issuer,
                                                                       String verificationMethod,
                                                                       PrivateKey prk,
                                                                       List<X509Certificate> certificates)
            throws CredentialPresentationException, CredentialSignatureException {
        // let notary sign registration number
        VerifiableCredential credential = gxdchService.verifyRegistrationNumber(cs);
        if (credential == null) {
            // if notary did not sign and we enforce, throw exception
            if (enforceNotary) {
                throw new CredentialPresentationException("Given registration number credential subject failed GXDCH Notary check");
            }
            // else notary has not attested registration number, we sign it ourselves
            credential = getSignedVc(cs, issuer, verificationMethod, prk, certificates);
        } else {
            // notary has signed but we need to clean up the result
            // remove @context from proof as it is wrong
            Map<String, Object> proofObj = credential.getLdProof().getJsonObject();
            proofObj.remove("@context");
            credential.setJsonObjectKeyValue("proof", proofObj);
            // patch for context, make it resolvable as it is disabled by default
            ((ConfigurableDocumentLoader) credential.getDocumentLoader()).setEnableHttp(true);
            ((ConfigurableDocumentLoader) credential.getDocumentLoader()).setEnableHttps(true);
        }
        return ExtendedVerifiableCredential.fromMap(credential.getJsonObject());
    }

    private ExtendedVerifiableCredential getSignedVc(PojoCredentialSubject cs,
                                                     String issuer,
                                                     String verificationMethod,
                                                     PrivateKey prk,
                                                     List<X509Certificate> certificates)
            throws CredentialPresentationException, CredentialSignatureException {
        // create credential from pojo CS and sign it
        VerifiableCredential credential = gxfsSignerService.createVerifiableCredential(
                cs,
                URI.create(issuer),
                URI.create(cs.getId() + "#" + cs.getType())); // set vc id to cs id
        gxfsSignerService
                .signVerifiableCredential(credential, verificationMethod, prk, certificates); // sign vc
        return ExtendedVerifiableCredential.fromMap(credential.getJsonObject());
    }

    private ExtendedVerifiablePresentation getComplianceVp(List<PojoCredentialSubject> csList,
                                                           String issuer,
                                                           String verificationMethod,
                                                           PrivateKey prk,
                                                           List<X509Certificate> certificates)
            throws CredentialSignatureException, CredentialPresentationException {

        List<VerifiableCredential> complianceVcs = new ArrayList<>();
        String subjectId = "";
        // iterate over given CS and handle them if relevant
        for (PojoCredentialSubject cs : csList) {
            if (cs instanceof GxLegalRegistrationNumberCredentialSubject registrationNumberCs) {
                complianceVcs.add(
                        getSignedRegistrationNumberVc(registrationNumberCs, issuer, verificationMethod, prk, certificates)
                );
            } else if (cs instanceof GxLegalParticipantCredentialSubject
                    || cs instanceof GxServiceOfferingCredentialSubject) {
                complianceVcs.add(
                        getSignedVc(cs, issuer, verificationMethod, prk, certificates)
                );
                subjectId = cs.getId();
            }
        }

        // set up a VP for the compliance service
        ExtendedVerifiablePresentation complianceVp = gxfsSignerService.createVerifiablePresentation(
                complianceVcs, // insert credentials into vp
                URI.create(subjectId + "#sd")); // set vp id to first proper cs id for now

        // verify compliance with compliance service
        VerifiableCredential complianceResult = gxdchService.checkCompliance(complianceVp);

        if (complianceResult == null) {
            if (enforceCompliance) {
                throw new CredentialPresentationException("Provided credential subjects failed GXDCH compliance check.");
            }
            log.warn("Compliance was not attested for the given VP.");
        } else {
            log.info("Received compliance credential result: {}", complianceResult);
            complianceVp.getVerifiableCredentials()
                    .add(ExtendedVerifiableCredential.fromMap(complianceResult.getJsonObject()));
        }

        return complianceVp;
    }

    private ExtendedVerifiablePresentation getSignedParticipantVp(List<PojoCredentialSubject> credentialSubjects,
                                                                  String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {
        // make sure there is at least one legal participant CS
        List<PojoCredentialSubject> participantCsList =
                findAllCredentialSubjectsByType(credentialSubjects, GxLegalParticipantCredentialSubject.class);
        if (participantCsList.isEmpty()) {
            throw new CredentialPresentationException(
                    "Could not find " + GxLegalParticipantCredentialSubject.TYPE + " in list of credential subjects.");
        }

        // make sure there is at least one legal registration number CS
        List<PojoCredentialSubject> registrationNumberCsList =
                findAllCredentialSubjectsByType(credentialSubjects, GxLegalRegistrationNumberCredentialSubject.class);
        if (registrationNumberCsList.isEmpty()) {
            throw new CredentialPresentationException(
                    "Could not find " + GxLegalRegistrationNumberCredentialSubject.TYPE + " in list of credential subjects.");
        }

        // create private key and certificate instances
        PrivateKey prk = buildPrivateKey(privateKey);
        List<X509Certificate> certificates = resolveCertificates(verificationMethod);

        // TODO incorporate Gaia-X TnC into SD
        gxdchService.getGxTnCs();

        // get id of participant from first participant cs
        String participantId = participantCsList.stream()
                .map(PojoCredentialSubject::getId)
                .filter(Objects::nonNull)
                .findFirst().orElse("");

        // collect all credentials that are relevant for the compliance service
        List<PojoCredentialSubject> complianceVcs = Stream
                .concat(participantCsList.stream(), registrationNumberCsList.stream()).toList();

        // remove compliance credentials from overall list
        List<PojoCredentialSubject> nonCompliantCsList = new ArrayList<>(credentialSubjects);
        nonCompliantCsList.removeAll(participantCsList);
        nonCompliantCsList.removeAll(registrationNumberCsList);

        // create vp with compliance relevant credentials plus attestation if successful
        ExtendedVerifiablePresentation vp
                = getComplianceVp(complianceVcs, participantId, verificationMethod, prk, certificates);

        // copy credential list as it is likely immutable
        List<ExtendedVerifiableCredential> credentialList = new ArrayList<>(vp.getVerifiableCredentials());

        // handle other (non-compliant) credentials
        for (PojoCredentialSubject cs : nonCompliantCsList) {
            VerifiableCredential vc = getSignedVc(cs, participantId, verificationMethod, prk, certificates);
            credentialList.add(ExtendedVerifiableCredential.fromMap(vc.getJsonObject()));
        }

        // update credential list in vp
        vp.setVerifiableCredentials(credentialList);

        // sign verifiable presentation for catalog storage
        gxfsSignerService.signVerifiablePresentation(vp, verificationMethod, prk, certificates);
        return vp;
    }
}
