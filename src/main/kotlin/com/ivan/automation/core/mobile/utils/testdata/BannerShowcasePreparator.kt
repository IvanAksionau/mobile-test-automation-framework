package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.services.CmsService
import com.ivan.automation.core.mobile.utils.TestContext
import com.ivan.automation.core.mobile.utils.TestContextConstants.CLIENT_RESOLUTION_DATA
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENTS_ID_LIST
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_ID_LIST
import com.ivan.automation.core.mobile.utils.TestContextConstants.TERRITORY_ID_LIST
import com.ivan.automation.core.mobile.utils.TestContextManager

/**
 * Prepares showcases for the top banner container.
 */
class BannerShowcasePreparator {

    private companion object {
        const val CTA_TYPE_CONTENT = "CONTENT"
        const val CTA_LANGUAGE_EN = "en"
        const val CTA_LABEL_PREFIX = "QA mobile test CTA label"
        const val SHOWCASE_TYPE = "Showcase"
        const val DEFAULT_POSITION = 1000
    }

    /**
     * Prepares and publishes a showcase with content for the top banner.
     *
     * @param territoryId Territory ID for the showcase.
     * @param showcaseId Showcase ID to configure.
     * @param contentId Content ID to link to the showcase.
     */
    fun prepareShowcaseWithContent(
        territoryId: String,
        showcaseId: String,
        contentId: String
    ) {
        val context = TestContextManager.getTestContext()
        val random = RandomStringGenerator.generate()
        val bannerShowcaseId = context.get<ClientResolutionResponse>(CLIENT_RESOLUTION_DATA).bannerShowcaseId

        // Create and configure CTA
        val callToAction = createCallToAction(showcaseId)
        linkContentToCta(showcaseId, callToAction.id, contentId)
        addCtaLabel(showcaseId, callToAction.id, random)

        // Configure device profile
        CmsService.createDeviceProfile(showcaseId, DeviceProfileFactory.createMobileOnlyProfile())

        // Publish showcase
        CmsService.publishShowcaseV2(showcaseId)

        // Add to banner and publish
        addShowcaseToBanner(bannerShowcaseId, showcaseId)
        CmsService.publishShowcaseV2(bannerShowcaseId)

        // Store IDs in context
        storeIdsInContext(context, showcaseId, contentId, territoryId)
    }

    /**
     * Creates a call-to-action for the showcase.
     */
    private fun createCallToAction(showcaseId: String) = CmsService.createCallToActionV2(
        showcaseId,
        CreateCallToActionRequestV2(
            showcaseId = showcaseId,
            type = CTA_TYPE_CONTENT
        )
    )

    /**
     * Links content to the call-to-action.
     */
    private fun linkContentToCta(showcaseId: String, ctaId: String, contentId: String) {
        CmsService.updateCallToActionV2(
            showcaseId,
            ctaId,
            UpdateShowcaseCTARequest(redirectEntityId = contentId)
        )
    }

    /**
     * Adds a label to the call-to-action.
     */
    private fun addCtaLabel(showcaseId: String, ctaId: String, randomSuffix: String) {
        CmsService.addShowcaseCtaLabel(
            showcaseId,
            ctaId,
            AddShowcaseCtaLabelRequest(
                label = "$CTA_LABEL_PREFIX$randomSuffix",
                language = CTA_LANGUAGE_EN
            )
        )
    }

    /**
     * Adds the showcase to the banner container.
     */
    private fun addShowcaseToBanner(bannerShowcaseId: String, showcaseId: String) {
        CmsService.addShowcaseChildren(
            bannerShowcaseId,
            AddShowcaseChildrenRequest(
                id = showcaseId,
                type = SHOWCASE_TYPE,
                position = DEFAULT_POSITION
            )
        )
    }

    /**
     * Stores created IDs in the test context.
     */
    private fun storeIdsInContext(
        context: TestContext,
        showcaseId: String,
        contentId: String,
        territoryId: String
    ) {
        context.appendListItem(SHOWCASES_ID_LIST, showcaseId)
        context.appendListItem(CONTENTS_ID_LIST, contentId)
        context.appendListItem(TERRITORY_ID_LIST, territoryId)
    }
}

/**
 * Extension function to append an item to a list in the test context.
 */
private fun <T> TestContext.appendListItem(key: String, item: T) {
    val list = getOrDefault(key, emptyList<T>()).toMutableList()
    list.add(item)
    set(key, list)
}
