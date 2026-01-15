package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonProperty

data class GetAdminTokenResponse(
    @JsonProperty("token_type")
    val tokenType: String,

    @JsonProperty("expires_in")
    val expiresIn: Int,

    @JsonProperty("refresh_token")
    val refreshToken: String,

    @JsonProperty("x_application_id")
    val xApplicationId: String,

    @JsonProperty("x_user_id")
    val xUserId: String,

    @JsonProperty("x_username")
    val xUsername: String,

    @JsonProperty("access_token")
    val accessToken: String,

    val scope: String
)
