package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchContentResponse(
    val results: List<SearchResult>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SearchResult(
        val id: String,
        val channelId: String,
        val type: String,
        val catalogRedirectId: String,
        val catalogRedirectType: String,
        val standardTitle: String,
        val title: String,
        val urlSlug: String,
        val backdropUri: String?,
        val freeMode: String? = null,
        val subtitle: String?,
        val wideCoverUrl: String?,
        val contentStartDate: String?,
        val contentEndDate: String?,
        val broadcastType: String?,
        val storyLine: String?,
        val tagLine: String?,
        val relatedGenreIds: List<String>
    )
}
