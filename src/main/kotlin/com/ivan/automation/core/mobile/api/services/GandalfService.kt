package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.BASE_URI
import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import io.qameta.allure.Step
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo

object GandalfService {

    /**
     * Get main content data.
     *
     * * @return A [ClientResolutionResponse] client resolution data.
     */
    @Step("GET - /gandalf/api/v1/configurations/client/resolution")
    fun getClientResolution(): ClientResolutionResponse {
        val response = RequestWrapper()
            .baseUri(BASE_URI)
            .deviceIdHeader()
            .get("/gandalf/api/v1/configurations/client/resolution")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse().`as`(ClientResolutionResponse::class.java)
    }
}
