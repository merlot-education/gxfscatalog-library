/*
 *  Copyright 2023-2024 Dataport AöR
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

import eu.merloteducation.gxfscataloglibrary.models.credentials.ExtendedVerifiableCredential;
import eu.merloteducation.gxfscataloglibrary.models.selfdescriptions.gx.participants.GxLegalRegistrationNumberCredentialSubject;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.PostExchange;

// based on https://registrationnumber.notary.lab.gaia-x.eu/v1/docs/
public interface GxNotaryClient {

    // Registration Number VC
    @PostExchange("/registrationNumberVC")
    ExtendedVerifiableCredential postRegistrationNumber(
            @RequestParam(name = "vcid", required = false) String vcid,
            @RequestBody GxLegalRegistrationNumberCredentialSubject body // RegistrationNumber formatted object as specified by the service-characteristics. Must also contain the participantID in the corresponding format.
    );
}
