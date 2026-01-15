package com.ivan.automation.core.mobile.api.models.request

data class RegisterDeviceRequest(
    val profile: String,
    val architecture: String,
    val appVersion: String,
    val osVersion: String,
    val osName: String,
    val model: String,
    val manufacturer: String,
    val store: String
)