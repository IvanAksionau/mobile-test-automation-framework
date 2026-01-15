package com.ivan.automation.core.mobile.api.wrapper

import com.ivan.automation.core.mobile.api.ServiceConstants.SERVICE_TIMEOUT_MS
import com.ivan.automation.core.mobile.utils.CustomException
import com.ivan.automation.core.mobile.utils.TestContextConstants.DASHBOARD_ADMIN_TOKEN
import com.ivan.automation.core.mobile.utils.TestContextConstants.X_CHILI_DEVICE_ID
import com.ivan.automation.core.mobile.utils.TestContextManager
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.http.Method
import io.restassured.specification.RequestSpecification

@Suppress("TooManyFunctions")
class RequestWrapper {

    private val requestSpecification: RequestSpecification = RestAssured
        .given()
        .filter(RequestLoggingFilter(LogDetail.ALL, true, System.out))
        .filter(ResponseLoggingFilter(LogDetail.ALL, true, System.out))
        .relaxedHTTPSValidation()
        .redirects().follow(false)
        .config(
            RestAssuredConfig.config().httpClient(
                HttpClientConfig.httpClientConfig()
                    .setParam("http.connection.timeout", SERVICE_TIMEOUT_MS)
                    .setParam("http.socket.timeout", SERVICE_TIMEOUT_MS)
            )
        )

    fun baseUri(baseUri: String) = apply {
        requestSpecification.baseUri(baseUri)
    }

    fun contentType(contentType: ContentType) = apply {
        requestSpecification.contentType(contentType)
    }

    fun body(body: Any) = apply {
        requestSpecification.body(body)
    }

    fun header(name: String, value: String) = apply {
        requestSpecification.header(name, value)
    }

    fun headers(headers: Map<String, String>) = apply {
        headers.forEach { (key, value) ->
            requestSpecification.header(key, value)
        }
    }

    fun deviceIdHeader() = apply {
        val deviceId = TestContextManager.getTestContext().get<String>(X_CHILI_DEVICE_ID)
        requestSpecification.header(X_CHILI_DEVICE_ID, deviceId)
    }

    fun enableRedirect() = apply {
        requestSpecification.redirects().follow(true)
    }

    fun dashBoardAuthHeader() = apply {
        requestSpecification
            .header("Authorization", TestContextManager.getTestContext().get(DASHBOARD_ADMIN_TOKEN))
    }

    fun param(name: String, value: String) = apply {
        requestSpecification.param(name, value)
    }

    fun formParams(params: Map<String, String>) = apply {
        params.forEach { (key, value) ->
            requestSpecification.formParam(key, value)
        }
    }

    fun pathParam(name: String, value: String) = apply {
        requestSpecification.pathParam(name, value)
    }

    fun queryParam(name: String, vararg values: Any) = apply {
        requestSpecification.queryParam(name, *values)
    }

    private fun sendRequest(method: Method, path: String): ResponseWrapper {
        val response = requestSpecification.basePath(path).request(method).then()
        return response?.extract()?.response()?.let { ResponseWrapper(it) }
            ?: throw CustomException("Failed to get a response from the API")
    }

    fun get(path: String) = sendRequest(Method.GET, path)

    fun post(path: String) = sendRequest(Method.POST, path)

    fun delete(path: String) = sendRequest(Method.DELETE, path)

    fun put(path: String) = sendRequest(Method.PUT, path)

    fun patch(path: String) = sendRequest(Method.PATCH, path)
}
