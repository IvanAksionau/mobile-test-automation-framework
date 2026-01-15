package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.BASE_URI
import com.ivan.automation.core.mobile.api.models.response.ContentResponse
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import com.ivan.automation.core.mobile.utils.HttpRetryExecutor
import com.ivan.automation.core.mobile.utils.TestContextConstants.X_CHILI_DEVICE_STORE
import com.ivan.automation.core.mobile.utils.TestContextManager
import io.qameta.allure.Step
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo

@Suppress("UNCHECKED_CAST")
object EntertainmentService {

    /**
     * Get the list of child showcases by showcase id.
     *
     * @param showcaseId id of parent showcase.
     * @param isUsePlacement indicate if 'isUsePlacement' query parameter is used.
     * @return List of ShowCaseResponse objects
     */
    @Step("GET - /entertainment/api/v1/showcases/{showcaseId}/child")
    fun getShowcaseChildList(showcaseId: String, isUsePlacement: Boolean = false): List<ShowCaseResponse> {
        val response = RequestWrapper()
            .deviceIdHeader()
            .header("x-chili-device-profile", "MOBILE")
            .header(X_CHILI_DEVICE_STORE, TestContextManager.getTestContext().get(X_CHILI_DEVICE_STORE))
            .baseUri(BASE_URI)
            .pathParam("showcaseId", showcaseId)
            .apply {
                if (isUsePlacement) {
                    queryParam("usePlacement", true)
                }
            }
            .get("/entertainment/api/v1/showcases/{showcaseId}/child")
            .status(equalTo(HttpStatus.SC_OK))

        val showcaseArray =
            response.getBody().`as`(Array<ShowCaseResponse>::class.java) as? Array<ShowCaseResponse> ?: emptyArray()
        return showcaseArray.toList()
    }

    /**
     * Get the list of child showcases by showcase id,
     * filtering to get showcases of the SHOWCASE type within child items.
     *
     * @param showcaseId id of parent showcase.
     * @return List of ShowCaseResponse objects
     */
    @Step("GET - /entertainment/api/v1/showcases/{showcaseId}/child")
    fun getShowcaseChildListFiltered(showcaseId: String): List<ShowCaseResponse> {
        val showcaseArray = getShowcaseChildList(showcaseId)
        var filtered = showcaseArray.toList().filter { showCase -> showCase.type == "SHOWCASE" }
        filtered = filtered.filter { showCase -> getShowcaseChildList(showCase.id).isNotEmpty() }
        return filtered
    }

    /**
     * Get the list of child showcases by showcase id.
     *
     * Polls the child endpoint of a given showcase until a child with the specified title
     * appears in the response (HTTP 200). After the condition is met, waits an additional
     * 10 seconds before returning.
     *
     * @param showcaseId  the ID of the showcase to query
     * @param childTitle  the title of the child item to wait for
     * @return            true once the child with [childTitle] is present
     */
    @Step("GET - /entertainment/api/v1/showcases/{showcaseId}/child")
    fun waitTillShowcaseChildAdded(showcaseId: String, childTitle: String): Boolean {
        HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .deviceIdHeader()
                    .header("x-chili-device-profile", "MOBILE")
                    .header(X_CHILI_DEVICE_STORE, TestContextManager.getTestContext().get(X_CHILI_DEVICE_STORE))
                    .baseUri(BASE_URI)
                    .pathParam("showcaseId", showcaseId)
                    .queryParam("usePlacement", false)
                    .queryParam("limit", "10")
                    .get("/entertainment/api/v1/showcases/{showcaseId}/child")
                    .status(equalTo(HttpStatus.SC_OK))
            }
            .setCondition { respWrapper ->
                val showcaseArray =
                    respWrapper.getResponse().getBody()
                        .`as`(Array<ShowCaseResponse>::class.java) as? Array<ShowCaseResponse> ?: emptyArray()
                showcaseArray.any { case -> case.title == childTitle }
            }
            .execute()

        return true
    }

    /**
     * Get the showcase data by showcase id.
     *
     * @param showcaseId id of showcase.
     * @return ShowCaseResponse objects
     */
    @Step("GET - /entertainment/api/v1/showcases/{showcaseId}")
    fun getShowcaseById(showcaseId: String): ShowCaseResponse? {
        val response = RequestWrapper()
            .deviceIdHeader()
            .baseUri(BASE_URI)
            .pathParam("showcaseId", showcaseId)
            .get("/entertainment/api/v1/showcases/{showcaseId}")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse().`as`(ShowCaseResponse::class.java)
    }

    /**
     * Get the content data by content id.
     *
     * @param contentId id of content.
     * @return ContentResponse objects
     */
    @Step("GET - /entertainment/api/v1/contents/{contentId}")
    fun getContentById(contentId: String): ContentResponse {
        val response = HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .deviceIdHeader()
                    .baseUri(BASE_URI)
                    .pathParam("contentId", contentId)
                    .get("/entertainment/api/v1/contents/{contentId}")
            }
            .setCondition { respWrapper ->
                respWrapper.getResponse().statusCode == HttpStatus.SC_OK

            }
            .execute()
        return response.getResponse().`as`(ContentResponse::class.java)
    }
}
