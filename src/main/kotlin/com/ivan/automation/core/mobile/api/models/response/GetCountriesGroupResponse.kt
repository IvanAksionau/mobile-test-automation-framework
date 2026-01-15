package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GetCountriesGroupResponse(
    val groupId: String,
    val countries: List<String>,
    val containers: List<Container>,
    val showLead: Boolean
) {
    data class Container(
        val id: String,
        val applicationId: String,
        val type: String,
        val country: String
    )

    data class ParentalLevel(
        val level: String,
        val country: String,
        val age: Int
    )
}
