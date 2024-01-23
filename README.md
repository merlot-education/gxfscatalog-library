# Gaia-X Federated Services (GXFS) Federated Catalogue (FC) client library

## Description
This Java-Spring based library intends to simplify interaction with the [federated catalogue](https://gitlab.eclipse.org/eclipse/xfsc/cat/fc-service) (currently supported in version 1.0.1).
In particular, it provides the following features:
- **Abstracted communication** with the catalogue API using simple Spring Services
- **Automated login and token refresh** of the provided user interacting with the catalogue
- **Easy self-description creation** using the ready-made models for the basic Gaia-X Trust Framework credential shapes (based on [v22.10](https://gitlab.com/gaia-x/technical-committee/service-characteristics/-/tree/v22.10))
- **Extensibility with custom models** building on top of the the Gaia-X Trust Framework models
- **Easy-to-use signature** of credentials to be published in the catalogue
- **Optional [SD Creation Wizard API](https://gitlab.eclipse.org/eclipse/xfsc/self-description-tooling/sd-creation-wizard-api) pass-through** and interaction for easy [SD Creation Wizard Frontend](https://gitlab.eclipse.org/eclipse/xfsc/self-description-tooling/sd-creation-wizard-frontend) shape retrieval

## Library structure

The most important parts for usage and extension of the library are summarized below:

```
├── src/main/java/eu/merloteducation/gxfscataloglibrary
│   ├── models
│   │   ├── participants                 # models relevant to the /participants API endpoint
│   │   ├── query                        # models relevant to the /query API endpoint
│   │   ├── selfdescriptions             # models relevant to the /self-descriptions API endpoint
│   │   │   ├── gax                      # data models of the participant/offering shapes as defined by Gaia-X
│   │   │   ├── merlot                   # exemplary extension of the gax data models for the MERLOT project
│   │   │   ├── (...)             
│   │   ├── (...)             
│   ├── service   
│   │   ├── GxfsCatalogService.java      # main exposed service for interacting with the catalogue
│   │   ├── GxfsWizardApiClient.java     # main exposed service for interacting with the wizard          
│   │   ├── *Client.java                 # internal client interfaces for the catalogue/wizard
│   │   ├── GxfsSignerService.java       # internal service for signing credentials
│   │   ├── GxfsCatalogAuthService.java  # internal service keeping the catalog user logged in
│   ├── (...)
```

## How to use

### Integration into your project
In order to retrieve packages from GitHub you need to generate a personal access token (PAT) including the "read:packages" scope using the [developer settings](https://github.com/settings/tokens).
This token and your username then need to be set in a `settings.xml` file following the following schema:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_USERNAME</username>
            <!-- Public token with `read:packages` scope -->
            <password>YOUR_TOKEN</password>
        </server>
    </servers>
</settings>
```
which can be placed in your maven root (typically `~/.m2/settings.xml`).

Afterwards you can head over to the released [packages](https://github.com/merlot-education/gxfscatalog-library/packages/) and add the library to your maven or gradle build pipeline.

### Configuration
The library expects a range of configuration options to be set in the `application.yml` of your project.
Some exemplary values can be found [here](https://github.com/merlot-education/gxfscatalog-library/blob/main/src/main/resources/application.yml).

The following table describes the expected values:

| Key                               | Description                                                                                                                                                                                                                                                                                                   |
|-----------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| gxfscatalog.base-uri              | The base url under which we can access the federated catalogue                                                                                                                                                                                                                                                |
| gxfscatalog.cert-path             | If set to the path of a certificate PEM (e.g. generated by the steps from [here](https://gitlab.com/gaia-x/data-infrastructure-federation-services/cat/fc-tools/signer/#usage)), the internal signer service will use the provided certificate instead of the default GXFS one for signing self-descriptions. |
| gxfscatalog.private-key-path      | See the previous parameter but with the private key PEM.                                                                                                                                                                                                                                                      |
| gxfswizardapi.base-uri            | Optional parameter pointing to the base url under which the SD creation wizard api can be reached if it should be used for pass-through.                                                                                                                                                                      |
| keycloak.client-id                | The client ID set in the Keycloak for the federated catalogue for authentication.                                                                                                                                                                                                                             |
| keycloak.authorization-grant-type | The grant type for authenticating at the keycloak, typically 'password'.                                                                                                                                                                                                                                      |
| keycloak.client-secret            | The client secret of the given client ID for the federated catalogue.                                                                                                                                                                                                                                         |
| keycloak.gxfscatalog-user         | The user that will interact with the catalogue through this library. This user needs sufficient permissions to access the respective endpoints (e.g. by assigning the role `Ro-MU-CA` within Keycloak for full access).                                                                                       |
| keycloak.gxfscatalog-pass         | See the previous parameter, the password of this user is required.                                                                                                                                                                                                                                            |
| keycloak.oidc-base-uri            | Can usually be left as default. Change this if the OIDC base url is configured differently in your Keycloak instance.                                                                                                                                                                                         |
| keycloak.logout-uri               | Can usually be left as default. Change this if the logout url is configured differently in your Keycloak instance.                                                                                                                                                                                            |

### Service Usage

In general to use this library in another Spring project we would include one of the services exposed by this library in some service of your project, for example like this:
```
public class MyBusinessService {

    @Autowired
    private GxfsCatalogService gxfsCatalogService;
    (...)
}
```
To understand how to use this service, let's consider a simple use case.

Say we want to create a new Participant in the catalogue (which was initialized with the Gaia-X schemas).
For this we can use the method `gxfsCatalogService.addParticipant(...)`.
As we can see in the method signature, this method expects a credential subject of type `GaxTrustLegalPersonCredentialSubject`.
Hence we could build and publish our participant to the catalogue like this:
```
public class MyBusinessService {
    (...)
    
    public void addMyNewParticipant() {
        GaxTrustLegalPersonCredentialSubject subject = new GaxTrustLegalPersonCredentialSubject();
        subject.setType("gax-trust-framework:LegalPerson");
        subject.setId("did:web:some-participant.example");
        subject.setLegalName(new StringTypeValue("My Participant Ltd."));
        
        (...) // set all other fields as required
        
        gxfsCatalogService.addParticipant(subject);
    }
    
    (...)
}
```
Thats it! At the library service call the credential subject will be automatically wrapped in a presentation, signed
with the private key given to the library and sent to the catalogue.
At this point we can easily retrieve access participant data again using the service:

```
public class MyBusinessService {
    (...)
    
    public void getMyNewParticipant() {
        ParticipantItem item = gxfsCatalogService
            .getParticipantById("did:web:some-participant.example");
        
        // since the item contains a generic self-description,
        // we will need to cast in order to access the full credential subject
        GaxTrustLegalPersonCredentialSubject credentialSubject = 
            (GaxTrustLegalPersonCredentialSubject) item
                .getSelfDescription().getVerifiableCredential().getCredentialSubject();
        
        (...) // do something with the data
    }
    
    (...)
}
```

All the other methods of the service can be used in a similar manner, 
e.g. to add/retrieve service offering self-descriptions, revoke/delete participants
and offerings and so on.

### Extension

As we saw in the previous section, the library service methods typically accept some kind
of credential subject type, e.g. a basic legal person as defined by Gaia-X or a general service offering.
We can find the respective models for these types [here](https://github.com/merlot-education/gxfscatalog-library/tree/main/src/main/java/eu/merloteducation/gxfscataloglibrary/models/selfdescriptions/gax).

Since the catalog allows to specify arbitrary schemas, we can easily extend upon this concept.
For example, we could define our own schemas that extend the basic legal person found [here](https://gitlab.com/gaia-x/technical-committee/service-characteristics/-/blob/v22.10/single-point-of-truth/yaml/gax-trust-framework/gax-participant/legal-person.yaml?ref_type=tags) 
with an e-mail address field, generate the respective ttl files and upload them to the catalog. 

Since the library has no knowledge of this field yet, we can use simple inheritance and extend upon the
`GaxTrustLegalPersonCredentialSubject` model and add it to the decorator of the generic 
`SelfDescriptionCredentialSubject` for automatic deserialization.

Once we have done this we can use our custom class for all the existing library service methods due to the
inheritance.

An exemplary extension of the basic Gaia-X schemas can be found [here](https://github.com/merlot-education/catalog-shapes/tree/main/shacl/shapes/merlot)
as well as in the code of this library [here](https://github.com/merlot-education/gxfscatalog-library/tree/main/src/main/java/eu/merloteducation/gxfscataloglibrary/models/selfdescriptions/merlot) 
which contains the models of the schemas used in the [MERLOT project](https://github.com/merlot-education).



## Limitations
Currently, the following catalogue API endpoints are not yet mapped:
- `/schemas` performing CRUD on the enrolled schemas
- `/verification` verifying a given JSON-LD document
- `/users` interacting with the user data of an enrolled Participant 
- `/roles` retrieving the available roles
- `/session` managing auth sessions