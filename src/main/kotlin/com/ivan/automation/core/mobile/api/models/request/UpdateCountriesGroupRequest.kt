package com.ivan.automation.core.mobile.api.models.request

import com.fasterxml.jackson.annotation.JsonProperty

data class UpdateCountriesGroupRequest(
    val countries: List<String>,
    val boxOffice: BoxOffice? = null,
    var broadcastType: String? = null,
    var contentEndDate: String? = null,
    var contentStartDate: String? = null,
    val saleability: Saleability? = null,
) {
    data class BoxOffice(
        val currency: String,
        val price: Int
    )

    data class Saleability(
        @JsonProperty("AVOD")
        val avod: List<String>? = emptyList(),

        @JsonProperty("VOD48h")
        val vod48H: List<String>? = emptyList(),

        @JsonProperty("EST")
        val est: List<String>? = emptyList(),
    )
}
