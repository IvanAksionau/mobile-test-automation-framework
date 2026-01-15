package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetContentDataResponse(
    val videoAsset: VideoAsset,
    val infoButtons: InfoButtons,
    val streamingRights: StreamingRights,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class VideoAsset(
        val catalogId: String,
        val assets: List<Any>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class StreamingRights(
        val contentId: String? = null,
        val playable: Boolean
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class InfoButtons(
        val buttons: List<Button>
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Button(
        val code: String,
        val type: String,
        val message: String? = null
    )
}
