package com.ivan.automation.core.mobile.api.models.request

data class CreateContentRequestV2(
    val internalTitle: String,
    val type: String,
    val territoryId: String,
    val videoAssetBaseFileName: String? = null
)