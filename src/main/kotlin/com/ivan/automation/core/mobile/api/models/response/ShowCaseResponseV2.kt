package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ShowCaseResponseV2(
    val id: String,
    val type: String,
    val internalTitle: String,
    val status: String,
    val urlSlug: String,
    val audit: Audit,
    val showcaseType: String,
    val languages: List<ShowcaseLanguage>,
    val defaultImages: List<String>,
    val overrideImages: Map<String, Any>,
    val enableSeoIndexing: Boolean,
    val taxonomies: List<Any>,
    val enableCallToAction: Boolean,
    val imageOverrideTerritoryIds: List<String>,
    val imageOverrideTerritory: List<Any>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Audit(
        val creationDate: String,
        val lastUpdateDate: String,
        val lastUpdateAuthor: String,
        val lastPublishDate: String?,
        val lastPublishAuthor: String?,
        val firstPublishDate: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ShowcaseLanguage(
        val id: String,
        val title: String,
        val status: String
    )
}
