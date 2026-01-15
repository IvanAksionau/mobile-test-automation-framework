package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class RegisterDeviceResponse(
    @JsonProperty("device_token")
    val deviceToken: String
)
