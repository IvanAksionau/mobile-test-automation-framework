package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KindTagsResponse(
    val slug: String,
    val title: String,
    val id: String,
    val contentsCount: Int,
    val standardTitle: String,
    val sourceIds: List<String>
)
