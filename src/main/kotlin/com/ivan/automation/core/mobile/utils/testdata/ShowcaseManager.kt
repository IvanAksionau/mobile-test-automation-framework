package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.enums.ShowcaseDataTypeV2
import com.ivan.automation.core.mobile.api.models.request.CreateShowcaseV2Request
import com.ivan.automation.core.mobile.api.models.request.UpdateShowcaseLanguagesRequest
import com.ivan.automation.core.mobile.api.models.request.UpdateShowcaseRequestV2
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponseV2
import com.ivan.automation.core.mobile.api.services.CmsService
import com.ivan.automation.core.mobile.driver.AppiumProperties.PLATFORM_NAME
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_ID
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_TITLE
import com.ivan.automation.core.mobile.utils.TestContextManager

/**
 * Manages showcase creation, update, publishing, and deletion operations.
 */
class ShowcaseManager {

    private companion object {
        const val LANGUAGE_CODE_EN = "en"
        val TEST_PREFIX = "QA $PLATFORM_NAME mobile test"
    }

    /**
     * Creates a showcase with language information and territory configuration.
     *
     * @param territoryId The territory ID to associate with the showcase.
     * @return [ShowCaseResponseV2] containing the created showcase details.
     */
    fun createShowcase(territoryId: String): ShowCaseResponseV2 {
        val random = RandomStringGenerator.generate()

        // Create a showcase
        val showcaseResponse = CmsService.createShowcaseV2(
            CreateShowcaseV2Request(
                internalTitle = "$TEST_PREFIX internalTitle$random",
                type = ShowcaseDataTypeV2.SHOWCASE.name
            )
        )

        // Add showcase languages
        updateShowcaseLanguages(showcaseResponse.id, random)

        // Add showcase territory and enable CallToAction
        updateShowcaseConfiguration(showcaseResponse.id, territoryId)

        // Retrieve and store showcase details
        val showcaseV2 = CmsService.getShowcaseV2(showcaseResponse.id)
        storeShowcaseInContext(showcaseV2)

        return showcaseV2
    }

    /**
     * Updates language-specific showcase information.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @param randomSuffix Random suffix for unique test data.
     */
    private fun updateShowcaseLanguages(showcaseId: String, randomSuffix: String) {
        CmsService.updateShowcaseLanguages(
            showcaseId, LANGUAGE_CODE_EN,
            UpdateShowcaseLanguagesRequest(
                metaTitle = "$TEST_PREFIX metaTitle_$randomSuffix",
                description = "$TEST_PREFIX description_$randomSuffix",
                metaDescription = "$TEST_PREFIX metaDescription_$randomSuffix"
            )
        )
    }

    /**
     * Updates showcase configuration with territory and CTA settings.
     *
     * @param showcaseId The unique identifier of the showcase.
     * @param territoryId The territory ID to associate with the showcase.
     */
    private fun updateShowcaseConfiguration(showcaseId: String, territoryId: String) {
        CmsService.updateShowcaseV2(
            showcaseId,
            UpdateShowcaseRequestV2(
                territoryId = territoryId,
                enableCallToAction = true
            )
        )
    }

    /**
     * Stores showcase information in test context for later retrieval.
     *
     * @param showcase The showcase to store in context.
     */
    private fun storeShowcaseInContext(showcase: ShowCaseResponseV2) {
        val context = TestContextManager.getTestContext()
        context.set(SHOWCASES_ID, showcase.id)
        context.set(SHOWCASES_TITLE, showcase.internalTitle)
    }

    /**
     * Unpublishes and deletes a showcase, including removing it from parent showcases.
     *
     * @param showcaseId The unique identifier of the showcase to delete.
     */
    fun deleteShowcase(showcaseId: String) {
        CmsService.unpublishShowcaseV2(showcaseId)

        // Remove showcase from its parents
        val showcaseParentsList = CmsService.getShowcaseParents(showcaseId).results
        showcaseParentsList.forEach { parent ->
            CmsService.deleteShowcaseChildren(parent.id, showcaseId)
        }

        CmsService.deleteShowcase(showcaseId)
    }
}
