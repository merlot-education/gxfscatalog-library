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

package eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.PojoCredentialSubject;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringDeserializer;
import eu.merloteducation.gxfscataloglibrary.models.serialization.StringSerializer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true, value={ "type", "@context" }, allowGetters=true)
public class GxLegalRegistrationNumberCredentialSubject extends PojoCredentialSubject {

    @Getter(AccessLevel.NONE)
    public static final String TYPE_NAMESPACE = "gx";
    @Getter(AccessLevel.NONE)
    public static final String TYPE_CLASS = "legalRegistrationNumber";
    @Getter(AccessLevel.NONE)
    public static final String TYPE = TYPE_NAMESPACE + ":" + TYPE_CLASS;

    @Getter(AccessLevel.NONE)
    public static final Map<String, String> CONTEXT = Map.of(
            TYPE_NAMESPACE, "https://registry.lab.gaia-x.eu/development/api/trusted-shape-registry/v1/shapes/jsonld/trustframework#",
            "xsd", "http://www.w3.org/2001/XMLSchema#"
    );

    @JsonProperty("gx:taxID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String taxID;

    @JsonProperty("gx:EUID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String euid;

    @JsonProperty("gx:EORI")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String eori;

    @JsonProperty("gx:vatID")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String vatID;

    @JsonProperty("gx:leiCode")
    @JsonSerialize(using = StringSerializer.class)
    @JsonDeserialize(using = StringDeserializer.class)
    private String leiCode;

    @JsonProperty("type")
    @Override
    public String getType() {
        return TYPE;
    }

    @JsonProperty("@context")
    @Override
    public Map<String, String> getContext() {
        return CONTEXT;
    }

}
