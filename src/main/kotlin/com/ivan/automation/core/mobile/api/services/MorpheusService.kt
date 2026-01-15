package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.BASE_URI
import com.ivan.automation.core.mobile.api.models.request.GetContentDataRequest
import com.ivan.automation.core.mobile.api.models.response.GetContentDataResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import com.ivan.automation.core.mobile.utils.HttpRetryExecutor
import io.qameta.allure.Step
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo
import java.time.Duration

object MorpheusService {

    private const val CONTENT_PLAYABLE_TIMEOUT_SECONDS = 350L
    private const val POLLING_INTERVAL_MS = 3_500L

    /**
     * Polls the content endpoint until the returned content becomes playable.
     *
     * Sends a POST request with the given [GetContentDataRequest] body to `/morpheus/api/v4/content`,
     * retrying on failure until an HTTP 200 is received and not empty buttons list in the response.
     *
     * @param request  the payload containing content identifiers and parameters for the request
     * @return         true once the content's `streamingRights.playable` is true
     */
    @Step("POST - /morpheus/api/v4/content")
    fun waitUntilContentIsPlayable(request: GetContentDataRequest): Boolean {
        HttpRetryExecutor
            .setPool(Duration.ofSeconds(CONTENT_PLAYABLE_TIMEOUT_SECONDS), POLLING_INTERVAL_MS)
            .setExpression {
                RequestWrapper()
                    .deviceIdHeader()
                    .header("x-chili-user-country", "US")
                    .baseUri(BASE_URI)
                    .contentType(ContentType.JSON)
                    .body(request)
                    .post("/morpheus/api/v4/content")
            }
            .setCondition { respWrapper ->
                respWrapper.status(equalTo(HttpStatus.SC_OK))
                !(respWrapper.getResponse().`as`(GetContentDataResponse::class.java).infoButtons.buttons).isEmpty()

            }
            .execute()
        return true
    }
}
