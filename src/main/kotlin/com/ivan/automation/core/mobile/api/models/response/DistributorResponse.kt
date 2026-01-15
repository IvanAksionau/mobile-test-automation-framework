package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DistributorResponse(
    val id: String,
    val applicationId: String,
    val name: String,
    val nameSlug: String,
    val description: String? = null,
    val code: String? = null
)
