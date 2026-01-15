package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.DASHBOARD_URI
import com.ivan.automation.core.mobile.api.models.response.AddShowcaseCtaLabelResponseResponse
import com.ivan.automation.core.mobile.api.models.response.ContentResponse
import com.ivan.automation.core.mobile.api.models.response.ContentResponseV2
import com.ivan.automation.core.mobile.api.models.response.DataResponse
import com.ivan.automation.core.mobile.api.models.response.ShowCaseChildrenResponse
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponseV2
import com.ivan.automation.core.mobile.api.models.response.VideoAssetResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import com.ivan.automation.core.mobile.utils.HttpRetryExecutor
import io.qameta.allure.Step
import io.restassured.http.ContentType
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo

@Suppress("TooManyFunctions")
object CmsService {

    ////////////////////////////////////////////////////////////////////////////////
    // Video Asset Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves all video assets matching the specified query.
     *
     * @param query The search query to filter video assets.
     * @return List of [VideoAssetResponse] objects matching the query.
     */
    @Step("GET - /cms/api/v1/videoAssets")
    fun getAllVideos(query: String): List<VideoAssetResponse> {
        val response = HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .dashBoardAuthHeader()
                    .baseUri(DASHBOARD_URI)
                    .queryParam("limit", "500")
                    .queryParam("query", query)
                    .get("/cms/api/v1/videoAssets")
            }
            .setCondition { respWrapper ->
                respWrapper.getResponse().statusCode == HttpStatus.SC_OK

            }
            .execute()

        @Suppress("UNCHECKED_CAST")
        val showcaseArray =
            response.getBody().`as`(Array<VideoAssetResponse>::class.java) as Array<VideoAssetResponse>
        return showcaseArray.toList()
    }

    /**
     * Retrieves the parent content items associated with a specific video asset.
     *
     * @param contentId The unique identifier of the video asset.
     * @return List of [ContentResponse] objects representing parent content items.
     */
    @Step("GET - /cms/api/v1/contents")
    fun getVideoParents(contentId: String): List<ContentResponse> {
        val response = RequestWrapper()
            .dashBoardAuthHeader()
            .baseUri(DASHBOARD_URI)
            .queryParam("videoAsset", contentId)
            .get("/cms/api/v1/contents")
            .status(equalTo(HttpStatus.SC_OK))

        @Suppress("UNCHECKED_CAST")
        val showcaseArray =
            response.getBody().`as`(Array<ContentResponse>::class.java) as Array<ContentResponse>
        return showcaseArray.toList()
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Showcase Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new showcase in the CMS.
     *
     * @param request The showcase creation request containing showcase details.
     * @return [DataResponse] containing the created showcase ID and location.
     */
    @Step("POST - /cms/api/v2/showcases")
    fun createShowcaseV2(request: CreateShowcaseV2Request): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/cms/api/v2/showcases")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Retrieves detailed information about a specific showcase.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @return [ShowCaseResponseV2] containing showcase details.
     */
    @Step("GET - /cms/api/v2/showcases/{showcaseId}")
    fun getShowcaseV2(showcaseId: String): ShowCaseResponseV2 {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("showcaseId", showcaseId)
            .get("/cms/api/v2/showcases/{showcaseId}")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse().`as`(ShowCaseResponseV2::class.java)
    }

    /**
     * Retrieves a parent showcases that contain the specified showcase as a child.
     *
     * @param showcaseId The unique identifier of the child showcase.
     * @return [ShowCaseChildrenResponse] containing parent showcase information.
     */
    @Step("GET - /cms/api/v2/showcases/parents/{showcaseId}")
    fun getShowcaseParents(showcaseId: String): ShowCaseChildrenResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .queryParam("filters", "children:$showcaseId")
            .get("/cms/api/v2/showcases")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse().`as`(ShowCaseChildrenResponse::class.java)
    }

    /**
     * Updates an existing showcase with new information.
     *
     * @param showcaseId The unique identifier of the showcase to update.
     * @param request The update request containing new showcase information.
     */
    @Step("PATCH - /cms/api/v2/showcases/{showcaseId}")
    fun updateShowcaseV2(showcaseId: String, request: UpdateShowcaseRequestV2) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(request)
            .pathParam("showcaseId", showcaseId)
            .patch("/cms/api/v2/showcases/{showcaseId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Updates language-specific information for a showcase.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @param languageId The language code to update.
     * @param request The update request containing new language information.
     */
    @Step("PATCH - /cms/api/v2/showcases/{showcaseId}/languages/{languageId}")
    fun updateShowcaseLanguages(showcaseId: String, languageId: String, request: UpdateShowcaseLanguagesRequest) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("showcaseId", showcaseId)
            .pathParam("languageId", languageId)
            .body(request)
            .patch("/cms/api/v2/showcases/{showcaseId}/languages/{languageId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Publishes a showcase, making it visible to end users.
     *
     * @param showcaseId The unique identifier of the showcase to publish.
     * @return [DataResponse] containing publication details, or null if publication fails.
     */
    @Step("POST - /cms/api/v2/showcases/{showcaseId}/published")
    fun publishShowcaseV2(showcaseId: String): DataResponse? {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("showcaseId", showcaseId)
            .contentType(ContentType.JSON)
            .post("/cms/api/v2/showcases/{showcaseId}/published")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Unpublishes a showcase, removing it from public visibility.
     *
     * @param showcaseId The unique identifier of the showcase to unpublish.
     */
    @Step("DELETE - /cms/api/v2/showcases/{showcaseId}/published")
    fun unpublishShowcaseV2(showcaseId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("showcaseId", showcaseId)
            .contentType(ContentType.JSON)
            .delete("/cms/api/v2/showcases/{showcaseId}/published")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Adds a child showcase or content to a parent showcase.
     *
     * @param showcaseId The unique identifier of the parent showcase.
     * @param request The request containing child details and position.
     * @return [DataResponse] containing the operation result.
     */
    @Step("POST - /cms/api/v2/showcases/{showcaseId}/children")
    fun addShowcaseChildren(showcaseId: String, request: AddShowcaseChildrenRequest): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("showcaseId", showcaseId)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/cms/api/v2/showcases/{showcaseId}/children")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Removes a child showcase from a parent showcase.
     *
     * @param showcaseId The unique identifier of the parent showcase.
     * @param childId The unique identifier of the child showcase to remove.
     */
    @Step("DELETE - /cms/api/v2/showcases/{showcaseId}/children/{childId}")
    fun deleteShowcaseChildren(showcaseId: String, childId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("showcaseId", showcaseId)
            .pathParam("childId", childId)
            .delete("/cms/api/v2/showcases/{showcaseId}/children/{childId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Creates a device profile for the specified showcase.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @param request The device profile creation request.
     */
    @Step("PUT - /cms/api/v2/showcases/{showcaseId}/deviceProfiles")
    fun createDeviceProfile(showcaseId: String, request: CreateDeviceProfileRequest) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("showcaseId", showcaseId)
            .body(request)
            .put("/cms/api/v2/showcases/{showcaseId}/devices")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Deletes the specified showcase.
     *
     * @param showcaseId The unique identifier of the showcase to delete.
     */
    @Step("DELETE - /cms/api/v2/showcases/{showcaseId}")
    fun deleteShowcase(showcaseId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("showcaseId", showcaseId)
            .contentType(ContentType.JSON)
            .delete("/cms/api/v2/showcases/{showcaseId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Territory Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new territory in the CMS.
     *
     * @param request The territory creation request containing territory details.
     * @return [DataResponse] containing the created territory ID and location.
     */
    @Step("POST - /cms/api/v2/territories")
    fun createTerritory(request: CreateTerritoryRequest): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/cms/api/v2/territories")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Publishes a territory, making it active for content distribution.
     *
     * @param territoryId The unique identifier of the territory to publish.
     * @return [DataResponse] containing publication details.
     */
    @Step("POST - /cms/api/v2/territories/{territoryId}/published")
    fun publishTerritory(territoryId: String): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("territoryId", territoryId)
            .post("/cms/api/v2/territories/{territoryId}/published")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Unpublishes a territory, deactivating it from content distribution.
     *
     * @param territoryId The unique identifier of the territory to unpublish.
     */
    @Step("DELETE - /cms/api/v2/territories/{territoryId}/published")
    fun unpublishTerritory(territoryId: String) {
        HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .baseUri(DASHBOARD_URI)
                    .dashBoardAuthHeader()
                    .pathParam("territoryId", territoryId)
                    .contentType(ContentType.JSON)
                    .delete("/cms/api/v2/territories/{territoryId}/published")
            }
            .setCondition { respWrapper ->
                respWrapper.getResponse().statusCode == HttpStatus.SC_NO_CONTENT

            }
            .execute()
    }

    /**
     * Deletes a territory from the CMS.
     *
     * @param territoryId The unique identifier of the territory to delete.
     */
    @Step("DELETE - /cms/api/v2/territories/{territoryId}")
    fun deleteTerritory(territoryId: String) {
        HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .baseUri(DASHBOARD_URI)
                    .dashBoardAuthHeader()
                    .pathParam("territoryId", territoryId)
                    .contentType(ContentType.JSON)
                    .delete("/cms/api/v2/territories/{territoryId}")
            }
            .setCondition { respWrapper ->
                respWrapper.getResponse().statusCode == HttpStatus.SC_NO_CONTENT

            }
            .execute()
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Call-to-Action Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @param showcaseId The unique identifier of the showcase.
     * @param request The CTA creation request containing CTA details.
     * @ return [DataResponse] containing the created CTA ID and location.
     */
    @Step("POST - /cms/api/v2/showcases/{showcaseId}/callToAction")
    fun createCallToActionV2(showcaseId: String, request: CreateCallToActionRequestV2): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("showcaseId", showcaseId)
            .contentType(ContentType.JSON)
            .body(request)
            .post("/cms/api/v2/showcases/{showcaseId}/callToAction")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Updates an existing call-to-action for a showcase.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @param callToActionId The unique identifier of the CTA to update.
     * @param request The update request containing new CTA information.
     */
    @Step("PATCH - /cms/api/v2/showcases/{showcaseId}/callToAction/{callToActionId}")
    fun updateCallToActionV2(showcaseId: String, callToActionId: String, request: UpdateShowcaseCTARequest) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("showcaseId", showcaseId)
            .pathParam("callToActionId", callToActionId)
            .body(request)
            .patch("/cms/api/v2/showcases/{showcaseId}/callToAction/{callToActionId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Adds or updates labels for a showcase call-to-action.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @param callToActionId The unique identifier of the CTA.
     * @param request The label request containing label text and language.
     * @return List of [AddShowcaseCtaLabelResponseResponse] containing updated labels.
     */
    @Step("PUT - /cms/api/v2/showcases/{showcaseId}/callToAction/{callToActionId}/labels")
    fun addShowcaseCtaLabel(
        showcaseId: String,
        callToActionId: String,
        request: AddShowcaseCtaLabelRequest
    ): List<AddShowcaseCtaLabelResponseResponse> {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("showcaseId", showcaseId)
            .pathParam("callToActionId", callToActionId)
            .body(request)
            .put("/cms/api/v2/showcases/{showcaseId}/callToAction/{callToActionId}/labels")
            .status(equalTo(HttpStatus.SC_OK))

        @Suppress("UNCHECKED_CAST")
        val countriesGroupArray = response.getBody()
            .`as`(Array<AddShowcaseCtaLabelResponseResponse>::class.java)
                as Array<AddShowcaseCtaLabelResponseResponse>
        return countriesGroupArray.toList()
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Content Operations
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new content in the CMS.
     *
     * @param request The content creation request containing content details.
     * @return [DataResponse] containing the created content ID and location.
     */
    @Step("POST - /cms/api/v2/contents")
    fun createContentV2(request: CreateContentRequestV2): DataResponse {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(request)
            .post("/cms/api/v2/contents")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Retrieves detailed information about specific content.
     *
     * @param contentId The unique identifier of the content.
     * @return [ContentResponseV2] containing content details.
     */
    @Step("GET - /cms/api/v2/contents/{contentId}")
    fun getContentByIdV2(contentId: String): ContentResponseV2 {
        val response = HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .deviceIdHeader()
                    .baseUri(DASHBOARD_URI)
                    .dashBoardAuthHeader()
                    .pathParam("contentId", contentId)
                    .get("/cms/api/v2/contents/{contentId}")
            }
            .setCondition { respWrapper ->
                respWrapper.getResponse().statusCode == HttpStatus.SC_OK

            }
            .execute()
        return response.getResponse().`as`(ContentResponseV2::class.java)
    }

    /**
     * Updates existing content with new information.
     *
     * @param contentId The unique identifier of the content to update.
     * @param request The update request containing new content information.
     */
    @Step("PATCH - /cms/api/v2/contents/{contentId}")
    fun updateContentV2(contentId: String, request: UpdateContentRequestV2) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .body(request)
            .pathParam("contentId", contentId)
            .patch("/cms/api/v2/contents/{contentId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Updates language-specific information for content.
     *
     * @param contentId The unique identifier of the content.
     * @param languageId The language code to update.
     * @param request The update request containing new language information.
     */
    @Step("PATCH - /cms/api/v2/contents/{contentId}/languages/{languageId}")
    fun updateContentLanguages(contentId: String, languageId: String, request: UpdateContentLanguagesRequest) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .contentType(ContentType.JSON)
            .pathParam("contentId", contentId)
            .pathParam("languageId", languageId)
            .body(request)
            .patch("/cms/api/v2/contents/{contentId}/languages/{languageId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Publishes content, making it visible to end users.
     *
     * @param contentId The unique identifier of the content to publish.
     * @return [DataResponse] containing publication details, or null if publication fails.
     */
    @Step("POST - /cms/api/v2/contents/{contentId}/published")
    fun publishContentV2(contentId: String): DataResponse? {
        val response = RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("contentId", contentId)
            .contentType(ContentType.JSON)
            .post("/cms/api/v2/contents/{contentId}/published")
            .status(equalTo(HttpStatus.SC_CREATED))

        return response.getResponse().`as`(DataResponse::class.java)
    }

    /**
     * Unpublishes content, removing it from public visibility.
     *
     * @param contentId The unique identifier of the content to unpublish.
     */
    @Step("DELETE - /cms/api/v2/contents/{contentId}/published")
    fun unpublishContentV2(contentId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("contentId", contentId)
            .contentType(ContentType.JSON)
            .delete("/cms/api/v2/contents/{contentId}/published")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }

    /**
     * Deletes the specified content.
     *
     * @param contentId The unique identifier of the content to delete.
     */
    @Step("DELETE - /cms/api/v2/contents/{contentId}")
    fun deleteContent(contentId: String) {
        RequestWrapper()
            .baseUri(DASHBOARD_URI)
            .dashBoardAuthHeader()
            .pathParam("contentId", contentId)
            .contentType(ContentType.JSON)
            .delete("/cms/api/v2/contents/{contentId}")
            .status(equalTo(HttpStatus.SC_NO_CONTENT))
    }
}
