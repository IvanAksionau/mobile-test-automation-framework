package com.ivan.automation.core.mobile.api.models.request

data class AddShowcaseChildrenRequest(
    val id: String,
    val type: String,
    val position: Int
)