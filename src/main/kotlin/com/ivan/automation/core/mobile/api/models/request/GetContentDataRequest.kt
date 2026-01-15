package com.ivan.automation.core.mobile.api.models.request

data class GetContentDataRequest(
    val contentId: String,
    val type: String,
    val productId: String
)