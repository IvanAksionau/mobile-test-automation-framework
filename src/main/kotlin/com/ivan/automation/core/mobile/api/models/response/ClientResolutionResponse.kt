package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClientResolutionResponse(
    val mainMenuIdMobile: String,
    val videoShowcasesForHome: List<String>,
    val defaultShowcaseSearch: String,
    val defaultShowcaseArchive: String,
    val bannerShowcaseId: String,
    val liveScheduleShowcaseId: String,
    val iosChiliApiBaseUrl: String,
    val simpleContentShowcaseId: String,
    val mobileShowcaseExplore: String,
    val welcomeAndroidSplashId: String,
    val welcomeModalShowcaseId: String,
    val mobileShowcaseArchive: String,
    val mainMenuIdWebsite: String,
    val chiliHost: String,
    val homeShowcaseId: String
)
