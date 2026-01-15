package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class KindResponse(
    val kindName: String,
    val label: String,
    val isSearchable: Boolean
)
