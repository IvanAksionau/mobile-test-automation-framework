package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ShowCaseChildrenResponse(
    val pagination: Pagination,
    val results: List<Result>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Pagination(
        val currentPage: Int,
        val nextPage: Int?,
        val lastPage: Int,
        val totalCount: Int
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Result(
        val id: String,
        val type: String,
        val internalTitle: String,
        val title: String,
        val audience: String,
        val status: String,
        val audit: Audit,
        val languagesStatus: Map<String, String>,
        val territory: Territory
    )

    data class Audit(
        val creationDate: String,
        val lastUpdateDate: String,
        val lastUpdateAuthor: String,
        val lastPublishDate: String?,
        val lastPublishAuthor: String?,
        val firstPublishDate: String?
    )

    data class Territory(
        val id: String,
        val internalTitle: String,
        val countries: List<String>
    )
}
