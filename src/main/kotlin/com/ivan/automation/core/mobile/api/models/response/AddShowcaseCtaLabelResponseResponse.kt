package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AddShowcaseCtaLabelResponseResponse(
    val id: String,
    val label: String,
    val status: String,
    val defaultLanguage: Boolean
)
