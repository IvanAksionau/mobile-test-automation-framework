package com.ivan.automation.core.mobile.utils

import com.ivan.automation.core.mobile.api.ServiceConstants.SERVICE_TIMEOUT_MS
import com.ivan.automation.core.mobile.driver.AppiumProperties
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.path.json.exception.JsonPathException
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Utility object for interacting with BrowserStack cloud services.
 */
object BrowserStackUtil {
    private val LOGGER = LoggerFactory.getLogger(BrowserStackUtil::class.java)

    /**
     * Uploads an .ipa file to BrowserStack App Automate using RestAssured.
     *
     * @param ipaPath local path to the .ipa file
     * @return the capability value (e.g., "bs://<app_id>") to set as the app path in BrowserStack
     * @throws IllegalArgumentException if the file is not found
     * @throws RuntimeException on upload or parsing failure
     */
    @JvmStatic
    fun uploadIpaFile(ipaPath: String): String {
        val file = File(ipaPath)
        require(file.exists()) { "File not found: $ipaPath" }

        // Perform upload request
        val response = RestAssured
            .given()
            .filter(RequestLoggingFilter(LogDetail.HEADERS, true, System.out))
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
            .baseUri(AppiumProperties.BS_API_URL)
            .auth().preemptive().basic(AppiumProperties.BS_USER, AppiumProperties.BS_PASSWORD)
            .multiPart("file", file)
            .post()
        // Handle non-200 responses and parse JSON
        val appUrl = when {
            response.statusCode != HttpStatus.SC_OK -> {
                val body = response.body.asString()
                throw CustomException(
                    "HTTP error during upload: status=${response.statusCode}, response body=$body"
                )
            }

            else -> try {
                response.jsonPath().getString("app_url")
            } catch (e: JsonPathException) {
                val raw = response.body.asString()
                throw CustomException(
                    "Failed to extract 'app_url' from JSON response: $raw",
                    e
                )
            }
        }
        LOGGER.info("IPA uploaded to BrowserStack with url: $appUrl")
        return appUrl
    }
}
