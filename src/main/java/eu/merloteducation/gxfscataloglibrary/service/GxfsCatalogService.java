package eu.merloteducation.gxfscataloglibrary.service;

import com.danubetech.verifiablecredentials.VerifiablePresentation;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryLanguage;
import eu.merloteducation.gxfscataloglibrary.models.client.QueryRequest;
import eu.merloteducation.gxfscataloglibrary.models.client.SelfDescriptionStatus;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialPresentationException;
import eu.merloteducation.gxfscataloglibrary.models.exception.CredentialSignatureException;
import eu.merloteducation.gxfscataloglibrary.models.participants.ParticipantItem;
import eu.merloteducation.gxfscataloglibrary.models.query.GXFSQueryUriItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.GXFSCatalogListResponse;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionItem;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.SelfDescriptionMeta;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.participants.GaxTrustLegalPersonCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gax.serviceofferings.GaxCoreServiceOfferingCredentialSubject;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;

@Service
public class GxfsCatalogService {

    private final Logger logger = LoggerFactory.getLogger(GxfsCatalogService.class);

    private static final String DEFAULT_DID_WEB = "did:web:compliance.lab.gaia-x.eu";

    @Autowired
    private GxfsCatalogClient gxfsCatalogClient;

    @Autowired
    private GxfsSignerService gxfsSignerService;

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
     * Given the credential subject of a self-description that inherits from gax-core:ServiceOffering,
     * wrap it in a verifiable presentation, sign it using the default verification method and key and send it to the catalog.
     *
     * @param serviceOfferingCredentialSubject service offering credential subject to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta addServiceOffering(
            GaxCoreServiceOfferingCredentialSubject serviceOfferingCredentialSubject)
            throws CredentialPresentationException, CredentialSignatureException {
        return addServiceOffering(serviceOfferingCredentialSubject, DEFAULT_DID_WEB, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-core:ServiceOffering,
     * wrap it in a verifiable presentation, sign it using the provided verification method and key and send it to the catalog.
     *
     * @param serviceOfferingCredentialSubject service offering credential subject to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @param privateKey string representation of private key to sign the SD with
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return SD meta response of the catalog
     */
    public SelfDescriptionMeta addServiceOffering(
            GaxCoreServiceOfferingCredentialSubject serviceOfferingCredentialSubject,
            String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {
        PrivateKey prk = buildPrivateKey(privateKey);
        List<X509Certificate> certificates = resolveDidWebCertificates(verificationMethod);
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(serviceOfferingCredentialSubject,
                        serviceOfferingCredentialSubject.getOfferedBy().getId());
        gxfsSignerService.signVerifiablePresentation(vp, verificationMethod, prk, certificates);
        return gxfsCatalogClient.postAddSelfDescription(vp);
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson,
     * wrap it in a verifiable presentation, sign it using the default verification method and key and send it to the catalog.
     *
     * @param participantCredentialSubject participant credential subject to insert into the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem addParticipant(GaxTrustLegalPersonCredentialSubject participantCredentialSubject)
            throws CredentialPresentationException, CredentialSignatureException {
        return addParticipant(participantCredentialSubject, DEFAULT_DID_WEB, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson,
     * wrap it in a verifiable presentation, sign it using the provided verification method and key and send it to the catalog.
     *
     * @param participantCredentialSubject participant credential subject to insert into the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @param privateKey string representation of private key to sign the SD with
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem addParticipant(GaxTrustLegalPersonCredentialSubject participantCredentialSubject,
                                          String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {
        PrivateKey prk = buildPrivateKey(privateKey);
        List<X509Certificate> certificates = resolveDidWebCertificates(verificationMethod);
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        gxfsSignerService.signVerifiablePresentation(vp, verificationMethod, prk, certificates);
        return this.gxfsCatalogClient.postAddParticipant(vp);
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson
     * and whose id already exists in the catalog, wrap it in a verifiable presentation,
     * sign it using the default verification method and key and send it to the catalog
     * to update it.
     *
     * @param participantCredentialSubject participant credential subject to update in the catalog
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem updateParticipant(GaxTrustLegalPersonCredentialSubject participantCredentialSubject)
            throws CredentialPresentationException, CredentialSignatureException {
        return updateParticipant(participantCredentialSubject, DEFAULT_DID_WEB, getDefaultPrivateKey());
    }

    /**
     * Given the credential subject of a self-description that inherits from gax-trust-framework:LegalPerson
     * and whose id already exists in the catalog, wrap it in a verifiable presentation,
     * sign it using the provided verification method and key and send it to the catalog
     * to update it.
     *
     * @param participantCredentialSubject participant credential subject to update in the catalog
     * @param verificationMethod method (e.g. a particular did) that can be used to verify the signature
     * @param privateKey string representation of private key to sign the SD with
     * @throws CredentialPresentationException exception during the presentation of the credential
     * @throws CredentialSignatureException exception during the signature of the presentation
     * @return catalog content of the participant
     */
    public ParticipantItem updateParticipant(GaxTrustLegalPersonCredentialSubject participantCredentialSubject,
                                             String verificationMethod, String privateKey)
            throws CredentialPresentationException, CredentialSignatureException {
        PrivateKey prk = buildPrivateKey(privateKey);
        List<X509Certificate> certificates = resolveDidWebCertificates(verificationMethod);
        VerifiablePresentation vp = gxfsSignerService
                .presentVerifiableCredential(participantCredentialSubject,
                        participantCredentialSubject.getId());
        gxfsSignerService.signVerifiablePresentation(vp, verificationMethod, prk, certificates);
        return this.gxfsCatalogClient.putUpdateParticipant(
                participantCredentialSubject.getId(),
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
        QueryRequest query = new QueryRequest("MATCH (p:" + participantType + ")"
                + " return p.uri ORDER BY toLower(p." + sortField + ")"
                + " SKIP " + offset + " LIMIT " + size);
        return this.gxfsCatalogClient.postQuery(
                QueryLanguage.OPENCYPHER,
                5,
                true,
                query);
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
        QueryRequest query = new QueryRequest("MATCH (p:" + participantType + ")"
            + " WHERE NOT p.uri IN " + excludedUrisString
            + " return p.uri ORDER BY toLower(p." + sortField + ")"
            + " SKIP " + offset + " LIMIT " + size);
        return this.gxfsCatalogClient.postQuery(
            QueryLanguage.OPENCYPHER,
            5,
            true,
            query);
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
     * Given a did:web, resolve it to its certificates.
     *
     * @param didWeb did:web to resolve
     * @return list of certificates
     */
    private List<X509Certificate> resolveDidWebCertificates(String didWeb) {

        if (!didWeb.startsWith("did:web:")) {
            logger.warn("Failed to validate verificationMethod {} as it is not a did:web, will skip validation.", didWeb);
            return Collections.emptyList();
        }

        if (didWeb.equals(DEFAULT_DID_WEB)) {
            logger.info("Using default verificationMethod {}, skipping web request.", didWeb);
            try {
                return buildCertficates(getDefaultCertificate());
            } catch (CredentialSignatureException e) {
                logger.warn("Failed to load default certificate, will skip validation.");
                return Collections.emptyList();
            }
        }

        // TODO fetch certificate from did endpoint
        return buildCertficates("");
    }

    /**
     * Given a string representation of the private key, convert it to it's the object representation.
     *
     * @param prk string representation of the private key
     * @return PrivateKey object
     */
    private PrivateKey buildPrivateKey(String prk) {
        try {
            PEMParser pemParser = new PEMParser(new StringReader(prk));
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(pemParser.readObject());
            return converter.getPrivateKey(privateKeyInfo);
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * Given a string representation of the certificates, convert them to a list of their object representations.
     *
     * @param certs string representation of the certificates
     * @return list of X509 certificate objects
     */
    private List<X509Certificate> buildCertficates(String certs) {
        try {
            ByteArrayInputStream certStream = new ByteArrayInputStream(certs.getBytes(StandardCharsets.UTF_8));
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            return (List<X509Certificate>) certFactory.generateCertificates(certStream);
        } catch (CertificateException ignored) {
            return Collections.emptyList();
        }
    }

    /**
     * Load the default private key corresponding to the DEFAULT_DID_WEB.
     *
     * @return string representation of private key
     * @throws CredentialSignatureException error during loading of private key
     */
    private String getDefaultPrivateKey() throws CredentialSignatureException {
        try (InputStream privateKeyStream =
                     GxfsSignerService.class.getClassLoader().getResourceAsStream("prk.ss.pem")) {
            return privateKeyStream == null ? null :
                    new String(privateKeyStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CredentialSignatureException("Failed to read default private key. " + e.getMessage());
        }
    }

    /**
     * Load the default certificate corresponding to the DEFAULT_DID_WEB.
     *
     * @return string representation of certificate
     * @throws CredentialSignatureException error during loading of private key
     */
    private String getDefaultCertificate() throws CredentialSignatureException {
        try (InputStream privateKeyStream =
                     GxfsSignerService.class.getClassLoader().getResourceAsStream("cert.ss.pem")) {
            return privateKeyStream == null ? null :
                    new String(privateKeyStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new CredentialSignatureException("Failed to read default certificate. " + e.getMessage());
        }
    }
}
