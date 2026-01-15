package com.ivan.automation.core.mobile.api.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ShowCaseResponse(
    val id: String,
    val type: String,
    val broadcastType: String? = "",
    val catalogRedirectId: String? = null,
    val catalogRedirectType: String? = null,
    val title: String,
    val standardTitle: String? = null,
    val description: String? = null,
    val showcaseType: String? = null,
    val details: Details? = null,
    val titleTemplateValues: TitleTemplateValues? = null,
    val template: List<Template>? = null,
    val callToAction: CallToAction? = null,
    val semanticTags: List<SemanticTag>? = null
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Details(
        val title: String,
        val description: String? = null,
        val callToAction: CallToAction? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class SemanticTag(
        val id: String,
        val sourceId: String? = null,
        val category: String? = null,
        val sourceName: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class CallToAction(
        val label: String? = null,
        val redirectId: String? = null,
        val redirectType: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class TitleTemplateValues(
        val country: String? = null,
        val association: String? = null,
        val match: String? = null,
        val season: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Template(
        val kind: String? = null,
        val value: String? = null
    )
}
