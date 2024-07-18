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

package eu.merloteducation.gxfscataloglibrary.models.credentials;

import com.danubetech.verifiablecredentials.VerifiableCredential;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public class ExtendedVerifiableCredential extends VerifiableCredential {

    @JsonCreator
    public ExtendedVerifiableCredential() {
    }

    protected ExtendedVerifiableCredential(Map<String, Object> map) {
        super(map);
    }

    @Override
    public CastableCredentialSubject getCredentialSubject() {
        return CastableCredentialSubject.getFromJsonLDObject(this);
    }

    public static ExtendedVerifiableCredential fromMap(Map<String, Object> map) {
        return new ExtendedVerifiableCredential(map);
    }

    public static ExtendedVerifiableCredential fromJson(String json) {
        return new ExtendedVerifiableCredential(readJson(json));
    }
}
