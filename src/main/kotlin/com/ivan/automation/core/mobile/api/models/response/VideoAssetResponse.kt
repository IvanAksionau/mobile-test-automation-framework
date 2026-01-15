package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class VideoAssetResponse(
    val id: String,
    val lastUpdate: String,
    val status: String,
    val type: String,
    val defaultLanguage: String,
    val missingTranslation: Boolean,
    val title: String,
    val duration: Int,
    val baseFilename: String,
    val technicalTitle: String? = null,
    val technicalTitleUrlSlug: String? = null,
    val publishCountries: Map<String, String>? = null,
)
