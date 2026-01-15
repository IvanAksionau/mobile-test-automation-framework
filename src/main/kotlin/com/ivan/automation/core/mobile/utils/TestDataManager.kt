package com.ivan.automation.core.mobile.utils

import com.ivan.automation.core.mobile.api.models.response.ContentResponseV2
import com.ivan.automation.core.mobile.api.models.response.DataResponse
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponseV2
import com.ivan.automation.core.mobile.utils.testdata.ContentManager
import com.ivan.automation.core.mobile.utils.testdata.RandomStringGenerator
import com.ivan.automation.core.mobile.utils.testdata.ShowcaseManager
import com.ivan.automation.core.mobile.utils.testdata.TerritoryManager
import com.ivan.automation.core.mobile.utils.testdata.TestDataCleanupManager

/**
 * Facade for managing test data operations in mobile automation.
 *
 * This class provides a simplified interface to various test data management operations
 * including content, showcase, and territory creation, publishing, and cleanup.
 * It follows the Facade pattern to provide a unified interface to the subsystem of
 * specialized managers.
 *
 */
object TestDataManager {

    const val SHORT_STRING_LENGTH: Int = 10

    private val contentManager = ContentManager()
    private val showcaseManager = ShowcaseManager()
    private val territoryManager = TerritoryManager()
    private val cleanupManager = TestDataCleanupManager(
        contentManager,
        showcaseManager,
        territoryManager
    )

    // ========== Cleanup Operations ==========

    /**
     * Remove items added to top home banner container.
     */
    fun cleanupTopHomeBannerData() {
        cleanupManager.cleanupTopHomeBannerData()
    }

    /**
     * Remove created showcases.
     */
    fun cleanupShowcasesData() {
        cleanupManager.cleanupShowcasesData()
    }

    /**
     * Remove created contents.
     */
    fun cleanupContentsData() {
        cleanupManager.cleanupContentsData()
    }

    /**
     * Remove created territories.
     */
    fun cleanupTerritoryData() {
        cleanupManager.cleanupTerritoryData()
    }

    // ========== Content Operations ==========

    /**
     * Creates a new content entity.
     *
     * @param contentType The type of content to create.
     * @return [DataResponse] containing the created content ID and location.
     */
    fun createContent(contentType: String): DataResponse {
        return contentManager.createContent(contentType)
    }

    /**
     * Creates, updates, and publishes content with optional live event configuration.
     *
     * @param contentId The unique identifier of the content.
     * @param territoryId The territory ID to associate with the content.
     * @param liveEventState Optional live event state (PAST, CURRENT, FUTURE).
     * @return [ContentResponseV2] containing the published content details.
     */
    fun createPublishContent(
        contentId: String,
        territoryId: String,
        liveEventState: String = ""
    ): ContentResponseV2 {
        return contentManager.createAndPublishContent(contentId, territoryId, liveEventState)
    }

    // ========== Territory Operations ==========

    /**
     * Creates a new territory and optionally publishes it.
     *
     * @param publish Whether to publish the territory immediately after creation.
     * @return [DataResponse] containing the created territory ID and location.
     */
    fun createTerritory(publish: Boolean): DataResponse {
        return territoryManager.createTerritory(publish)
    }

    // ========== Showcase Operations ==========

    /**
     * Creates a showcase with language information and territory configuration.
     *
     * @param territoryId The territory ID to associate with the showcase.
     * @return [ShowCaseResponseV2] containing the created showcase details.
     */
    fun createTheShowcase(territoryId: String): ShowCaseResponseV2 {
        return showcaseManager.createShowcase(territoryId)
    }

    // ========== Utility Operations ==========

    /**
     * Generate a random alphanumeric string of the specified length.
     *
     * @param length The desired length of the random string.
     * @return A random alphanumeric string of the given length.
     */
    fun getRandomString(length: Int): String {
        return RandomStringGenerator.generate(length)
    }
}
