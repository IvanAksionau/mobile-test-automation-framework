package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContentResponse(
    var id: String,
    var type: String,
    var productId: String,
    var orderIndex: Int,
    var channelId: String,
    var catalogRedirectId: String? = null,
    var catalogRedirectType: String? = null,
    var contentStartDate: String? = null,
    var contentEndDate: String? = null,
    var lastUpdate: String,
    var duration: Long,
    var childCount: Int,
    var productionYear: Int,
    var productionDate: String? = null,
    var originalTitle: String,
    var standardTitle: String? = null,
    var freeForStreaming: Boolean,
    var preorder: Boolean,
    var title: String? = null,
    var urlSlug: String? = null,
    var tagLine: String? = null,
    var mainGenre: Genre? = null,
    var genres: List<Genre>? = null,
    var semanticTags: List<SemanticTag>? = null,
    var shownPrices: Map<String, Any>? = null,
    var posterUrls: List<String>? = null,
    var backdropUrls: List<String>? = null,
    var backdropUrl: String? = null,
    var coverUrl: String? = null,
    var wideCoverUrl: String? = null,
    var distributors: List<String>? = null,
    var producers: List<String>? = null,
    var castAndCrew: List<String>? = null,
    var weights: List<String>? = null,
    var parentalLevel: ParentalLevel?
) {
    companion object {
        /**
         * Constant representing the number of milliseconds in one minute.
         */
        const val MILLISECONDS_PER_MINUTE: Long = 60000L
    }

    fun getDurationInMinutes(): Long {
        return duration / MILLISECONDS_PER_MINUTE
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Genre(
        var id: String,
        var channelId: String,
        var title: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SemanticTag(
        var id: String,
        var sourceId: String,
        var sourceName: String,
        var category: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ParentalLevel(
        var country: String,
        var level: String,
        var age: Int
    )
}
