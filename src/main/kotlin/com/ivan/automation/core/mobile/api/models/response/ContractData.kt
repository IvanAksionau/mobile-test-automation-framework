package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ContractData(
    val name: String,
    val countries: List<String>,
    val validity: List<String>,
    val currency: String,
    var startDate: String? = null,
    var endDate: String? = null,
    val operationalCosts: Int? = null,
    val operationalCostsTVOD: Int? = null,
    val revenueShareMethod: String? = "CONTRACT",
    val id: String? = null
)
