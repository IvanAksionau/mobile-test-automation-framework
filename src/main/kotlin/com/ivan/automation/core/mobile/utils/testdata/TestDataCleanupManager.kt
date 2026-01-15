package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.services.EntertainmentService
import com.ivan.automation.core.mobile.driver.AppiumProperties.PLATFORM_NAME
import com.ivan.automation.core.mobile.utils.TestContextConstants.CLIENT_RESOLUTION_DATA
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENTS_ID_LIST
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_ID_LIST
import com.ivan.automation.core.mobile.utils.TestContextConstants.TERRITORY_ID_LIST
import com.ivan.automation.core.mobile.utils.TestContextManager

/**
 * Manages cleanup operations for test data.
 */
class TestDataCleanupManager(
    private val contentManager: ContentManager,
    private val showcaseManager: ShowcaseManager,
    private val territoryManager: TerritoryManager
) {

    private companion object {
        val QA_MOBILE_TEST_TITLE = "QA $PLATFORM_NAME mobile test"
    }

    /**
     * Removes items added to top home banner container.
     * Cleans up showcases and their associated content that match the test title pattern.
     */
    fun cleanupTopHomeBannerData() {
        val clientResolution = TestContextManager.getTestContext()
            .get<ClientResolutionResponse>(CLIENT_RESOLUTION_DATA)
        val topBannerShowcaseChildList = EntertainmentService.getShowcaseChildList(
            clientResolution.bannerShowcaseId
        )

        topBannerShowcaseChildList.forEach { showcase ->
            if (showcase.title.contains(QA_MOBILE_TEST_TITLE)) {
                showcaseManager.deleteShowcase(showcase.id)
                showcase.callToAction?.redirectId?.let { redirectId ->
                    contentManager.deleteContent(redirectId)
                }
            }
        }
    }

    /**
     * Removes all created showcases stored in test context.
     */
    fun cleanupShowcasesData() {
        val showcasesList = TestContextManager.getTestContext().get<List<String>>(SHOWCASES_ID_LIST)
        showcasesList.forEach { showcaseId ->
            showcaseManager.deleteShowcase(showcaseId)
        }
    }

    /**
     * Removes all created contents stored in test context.
     */
    fun cleanupContentsData() {
        val contentsList = TestContextManager.getTestContext().get<List<String>>(CONTENTS_ID_LIST)
        contentsList.forEach { contentId ->
            contentManager.deleteContent(contentId)
        }
    }

    /**
     * Removes all created territories stored in test context.
     */
    fun cleanupTerritoryData() {
        val territoryList = TestContextManager.getTestContext().get<List<String>>(TERRITORY_ID_LIST)
        territoryList.forEach { territoryId ->
            territoryManager.deleteTerritory(territoryId)
        }
    }
}
