package com.ivan.automation.core.mobile.api.models.request

data class UpdateContentRequestV2(
    val territoryId: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val duration: Long? = null
)