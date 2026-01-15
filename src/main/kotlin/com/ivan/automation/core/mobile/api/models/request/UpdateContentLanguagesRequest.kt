package com.ivan.automation.core.mobile.api.models.request

data class UpdateContentLanguagesRequest(
    val title: String? = null,
    val storyline: String? = null,
    val metaTitle: String? = null,
    val metaDescription: String? = null,
    val tagline: String? = null,
    val location: String? = null,
    val subtitle: String? = null
)