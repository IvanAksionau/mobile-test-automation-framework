package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.response.ContentResponseV2
import com.ivan.automation.core.mobile.api.models.response.DataResponse
import com.ivan.automation.core.mobile.api.services.CmsService
import com.ivan.automation.core.mobile.driver.AppiumProperties.PLATFORM_NAME
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENT_RESPONSE
import com.ivan.automation.core.mobile.utils.TestContextConstants.EVENT_START_DATE
import com.ivan.automation.core.mobile.utils.TestContextManager

/**
 * Manages content creation, update, and publishing operations.
 */
class ContentManager {

    private companion object {
        const val DEFAULT_DURATION_MS = 60000
        const val LANGUAGE_CODE_EN = "en"
        val TEST_PREFIX = "QA $PLATFORM_NAME mobile test"
    }

    /**
     * Creates a new content entity.
     *
     * @param contentType The type of content to create.
     * @return [DataResponse] containing the created content ID and location.
     */
    fun createContent(contentType: String): DataResponse {
        val random = RandomStringGenerator.generate()
        val createContentRequest = CreateContentRequestV2(
            type = contentType,
            internalTitle = "$TEST_PREFIX content internal title-$random",
        )
        return CmsService.createContentV2(createContentRequest)
    }

    /**
     * Creates, updates, and publishes content with optional live event configuration.
     *
     * @param contentId The unique identifier of the content.
     * @param territoryId The territory ID to associate with the content.
     * @param liveEventState Optional live event state (PAST, CURRENT, FUTURE).
     * @return [ContentResponseV2] containing the published content details.
     */
    fun createAndPublishContent(
        contentId: String,
        territoryId: String,
        liveEventState: String = ""
    ): ContentResponseV2 {
        updateContent(contentId, territoryId, liveEventState)
        updateContentLanguages(contentId)
        CmsService.publishContentV2(contentId)

        val content = CmsService.getContentByIdV2(contentId)
        TestContextManager.getTestContext().set(CONTENT_RESPONSE, content)
        return content
    }

    /**
     * Updates content with territory and optional live event dates.
     *
     * @param contentId The unique identifier of the content.
     * @param territoryId The territory ID to associate with the content.
     * @param liveEventState Optional live event state for date calculation.
     */
    private fun updateContent(contentId: String, territoryId: String, liveEventState: String) {
        val request = if (liveEventState.isNotEmpty()) {
            val (startDate, endDate) = LiveEventDateCalculator.calculateDateRange(
                LiveEventState.valueOf(liveEventState)
            )
            TestContextManager.getTestContext().set(EVENT_START_DATE, startDate)

            UpdateContentRequestV2(
                territoryId = territoryId,
                startDate = startDate,
                endDate = endDate,
                duration = DEFAULT_DURATION_MS
            )
        } else {
            UpdateContentRequestV2(
                territoryId = territoryId,
                duration = DEFAULT_DURATION_MS
            )
        }

        CmsService.updateContentV2(contentId, request)
    }

    /**
     * Updates language-specific content information.
     *
     * @param contentId The unique identifier of the content.
     */
    private fun updateContentLanguages(contentId: String) {
        val random = RandomStringGenerator.generate()
        CmsService.updateContentLanguages(
            contentId, LANGUAGE_CODE_EN,
            UpdateContentLanguagesRequest(
                metaTitle = "$TEST_PREFIX metaTitle-$random",
                tagline = "$TEST_PREFIX description-$random",
                metaDescription = "$TEST_PREFIX metaDescription-$random",
                location = "$TEST_PREFIX location-$random",
                storyline = "$TEST_PREFIX storyline-$random",
                subtitle = "$TEST_PREFIX subtitle-$random"
            )
        )
    }

    /**
     * Unpublishes and deletes content.
     *
     * @param contentId The unique identifier of the content to delete.
     */
    fun deleteContent(contentId: String) {
        CmsService.unpublishContentV2(contentId)
        CmsService.deleteContent(contentId)
    }
}
