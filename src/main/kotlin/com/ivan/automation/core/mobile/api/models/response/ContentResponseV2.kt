package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContentResponseV2(
    val id: String,
    val type: String,
    val internalTitle: String,
    val status: String,
    val urlSlug: String,
    val audit: Audit,
    val cast: List<String>,
    val languages: List<Language>,
    val defaultImages: List<String>,
    val overrideImages: Map<String, Any>,
    val videoAssetBaseFileName: String?,
    val enableSeoIndexing: Boolean,
    val taxonomies: List<Any>,
    val territoryId: String,
    val territory: Territory,
    val imageOverrideTerritoryIds: List<String>,
    val imageOverrideTerritory: List<Any>,
    var startDate: String? = null,
    var duration: Long,
    val productId: String
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
    data class Audit(
        val creationDate: String? = null,
        val lastUpdateDate: String,
        val lastUpdateAuthor: String,
        val lastPublishDate: String,
        val lastPublishAuthor: String,
        val firstPublishDate: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Language(
        val id: String,
        val title: String,
        val storyline: String? = null,
        val location: String? = null,
        val subtitle: String? = null,
        val tagline: String? = null,
        val metaTitle: String? = null,
        val metaDescription: String? = null,
        val status: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Territory(
        val id: String,
        val internalTitle: String,
        val countries: List<String>
    )
}
