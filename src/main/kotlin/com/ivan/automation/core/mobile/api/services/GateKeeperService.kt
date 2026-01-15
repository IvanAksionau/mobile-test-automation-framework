package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.BASE_URI
import com.ivan.automation.core.mobile.api.models.request.GetAdminTokenRequest
import com.ivan.automation.core.mobile.api.models.request.RegisterDeviceRequest
import com.ivan.automation.core.mobile.api.models.response.GetAdminTokenResponse
import com.ivan.automation.core.mobile.api.models.response.RegisterDeviceResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import com.ivan.automation.core.mobile.utils.HttpRetryExecutor
import io.qameta.allure.Step
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo

object GateKeeperService {

    /**
     * Registers the device with the backend to obtain a unique device token.
     *
     * Retries on failure based on the HttpRetryExecutor configuration.
     *
     * @param request the [RegisterDeviceRequest] containing device details.
     * @return a [RegisterDeviceResponse] with the generated device ID token.
     * @throws AssertionError if the final response status is not HTTP 200 OK.
     */
    @Step("POST - /api/v2/devices")
    fun registerDevice(request: RegisterDeviceRequest): RegisterDeviceResponse {
        val response = HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .baseUri(BASE_URI)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/api/v2/devices")
            }
            .setCondition { respWrapper -> respWrapper.getResponse().statusCode() == HttpStatus.SC_OK }
            .execute()

        return response.getResponse().`as`(RegisterDeviceResponse::class.java)
    }

    /**
     * Requests an admin access token for the dashboard via OAuth2.
     *
     * Sends form-encoded credentials and device ID, returning the
     * bearer token for subsequent admin-level API calls.
     *
     * @param request the [GetAdminTokenRequest] containing grant type,
     *                user credentials, device ID, and optional scope.
     * @return the access token [String] from the [GetAdminTokenResponse].
     * @throws AssertionError if the response status is not HTTP 200 OK.
     */
    @Step("POST - /gatekeeper/api/v1/oauth2/token")
    fun getDashBoardAdminToken(request: GetAdminTokenRequest): String {
        val formParams = mapOf(
            "grant_type" to request.grantType,
            "username" to request.username,
            "password" to request.password,
            "x_device_id" to request.xDeviceId,
            "scope" to request.scope
        )

        val response = RequestWrapper()
            .baseUri(BASE_URI)
            .contentType(ContentType.URLENC)
            .headers(
                mapOf(
                    "x-chili-user-country" to "IT",
                    "x-chili-service-use-infinite-token" to "true"
                )
            )
            .formParams(formParams)
            .post("/gatekeeper/api/v1/oauth2/token")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse()
            .`as`(GetAdminTokenResponse::class.java)
            .accessToken
    }
}
