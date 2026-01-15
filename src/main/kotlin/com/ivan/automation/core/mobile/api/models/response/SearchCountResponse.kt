package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchCountResponse(
    val totalCount: String
)
