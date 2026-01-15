package com.ivan.automation.core.mobile.api.models.request

data class GetAdminTokenRequest(
    val xDeviceId: String,
    val grantType: String = "password",
    val username: String = "admin",
    val password: String = "admin",
    val scope: String = "CMS"
)