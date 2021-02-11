/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.nequi.banking.client;

import com.amazonaws.mobileconnectors.apigateway.annotation.Operation;
import com.amazonaws.mobileconnectors.apigateway.annotation.Parameter;
import com.amazonaws.mobileconnectors.apigateway.annotation.Service;
import com.google.gson.JsonObject;

@Service(endpoint = "https://api.nequi.com/agents/v2")
public interface NequiBankingAgentsGatewayClient {

    @Operation(path = "/-services-cashinservice-cashin", method = "POST")
    JsonObject servicesCashinserviceCashinPost(
        JsonObject body,
        @Parameter(name = "Authorization", location = "header") String authorization
    );

    @Operation(path = "/-services-cashoutservice-cashout", method = "POST")
    JsonObject servicesCashoutserviceCashoutPost(
        JsonObject body,
        @Parameter(name = "Authorization", location = "header") String authorization
    );

    @Operation(path = "/-services-cashoutservice-cashoutconsult", method = "POST")
    JsonObject servicesCashoutserviceCashoutconsultPost(
        JsonObject body,
        @Parameter(name = "Authorization", location = "header") String authorization
    );

    @Operation(path = "/-services-clientservice-validateclient", method = "POST")
    JsonObject servicesClientserviceValidateclientPost(
        JsonObject body,
        @Parameter(name = "Authorization", location = "header") String authorization
    );

    @Operation(path = "/-services-reverseservices-reversetransaction", method = "POST")
    JsonObject servicesReverseservicesReversetransactionPost(
        JsonObject body,
        @Parameter(name = "Authorization", location = "header") String authorization
    );

    @Operation(path = "/-services-keysservice-getpublic", method = "POST")
    String servicesKeysserviceGetpublicPost(
        @Parameter(name = "Authorization", location = "header") String authorization
    );
}