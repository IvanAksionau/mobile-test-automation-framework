package com.ivan.automation.core.mobile.api.services

import com.ivan.automation.core.mobile.api.ServiceConstants.BASE_URI
import com.ivan.automation.core.mobile.api.models.response.KindResponse
import com.ivan.automation.core.mobile.api.models.response.KindTagsResponse
import com.ivan.automation.core.mobile.api.models.response.SearchContentResponse
import com.ivan.automation.core.mobile.api.models.response.SearchCountResponse
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import io.qameta.allure.Step
import org.apache.http.HttpStatus
import org.hamcrest.Matchers.equalTo

@Suppress("UNCHECKED_CAST")
object SearchService {

    private const val COMMON_FILTER_VALUE: String = "ivan-plus:archive-project-ivan-plus"

    /**
     * Performs a content search.
     *
     * @param query the search keyword or phrase to look for in content.
     * @return the deserialized [SearchContentResponse] containing search results.
     * @throws AssertionError if the response status is not HTTP 200 OK.
     */
    @Step("GET - /api/v2/search")
    fun searchContent(query: String): SearchContentResponse {
        val response = RequestWrapper()
            .deviceIdHeader()
            .baseUri(BASE_URI)
            .queryParam("limit", "20")
            .queryParam("query", query)
            .get("/api/v2/search")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse().`as`(SearchContentResponse::class.java)
    }

    /**
     * Retrieves a list of content kinds.
     *
     * @param subset the subset filter to apply when querying kinds.
     * @return a [List] of [KindResponse] objects representing each kind.
     * @throws AssertionError if the response status is not HTTP 200 OK.
     */
    @Step("GET - /api/v2/kinds")
    fun getKinds(subset: String): List<KindResponse> {
        val response = RequestWrapper()
            .deviceIdHeader()
            .enableRedirect()
            .baseUri(BASE_URI)
            .queryParam("subset", subset)
            .get("/api/v2/kinds")
            .status(equalTo(HttpStatus.SC_OK))

        val array = response.getBody().`as`(Array<KindResponse>::class.java) as? Array<KindResponse> ?: emptyArray()
        return array.toList()
    }

    /**
     * Fetches the total count of searchable content.
     *
     * Applies default filters to exclude future-dated items and order by timestamp descending.
     *
     * @return the deserialized [SearchCountResponse] containing the content count.
     * @throws AssertionError if the response status is not HTTP 200 OK.
     */
    @Step("GET - /api/v2/search/count")
    fun getContentCount(): SearchCountResponse {
        val response = RequestWrapper()
            .deviceIdHeader()
            .baseUri(BASE_URI)
            .queryParam("excludeFutureDates", true)
            .queryParam("filters", COMMON_FILTER_VALUE)
            .queryParam("orderBy", "TIMESTAMP_DESC")
            .get("/api/v2/search/count")
            .status(equalTo(HttpStatus.SC_OK))

        return response.getResponse().`as`(SearchCountResponse::class.java)
    }

    /**
     * Retrieves tag metadata for a given content kind.
     *
     * @param kindName the identifier of the kind to fetch tags for.
     * @return a [List] of [KindTagsResponse] representing each tag for the specified kind.
     * @throws AssertionError if the response status is not HTTP 200 OK.
     */
    @Step("GET - /api/v2/kinds/{kindName}/tags")
    fun getKindTags(kindName: String): List<KindTagsResponse> {
        val response = RequestWrapper()
            .deviceIdHeader()
            .baseUri(BASE_URI)
            .pathParam("kindName", kindName)
            .queryParam("filters", COMMON_FILTER_VALUE)
            .queryParam("aggregate", true)
            .get("/api/v2/kinds/{kindName}/tags")
            .status(equalTo(HttpStatus.SC_OK))

        val array =
            response.getBody().`as`(Array<KindTagsResponse>::class.java) as? Array<KindTagsResponse> ?: emptyArray()
        return array.toList()
    }
}
