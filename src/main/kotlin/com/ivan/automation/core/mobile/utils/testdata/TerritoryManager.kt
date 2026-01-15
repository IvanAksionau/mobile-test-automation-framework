package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.response.DataResponse
import com.ivan.automation.core.mobile.api.services.CmsService
import com.ivan.automation.core.mobile.driver.AppiumProperties.PLATFORM_NAME

/**
 * Manages territory creation, publishing, and deletion operations.
 */
class TerritoryManager {

    private companion object {
        val TEST_PREFIX = "QA $PLATFORM_NAME mobile test"
    }

    /**
     * Creates a new territory and optionally publishes it.
     *
     * @param publish Whether to publish the territory immediately after creation.
     * @return [DataResponse] containing the created territory ID and location.
     */
    fun createTerritory(publish: Boolean = false): DataResponse {
        val random = RandomStringGenerator.generate()

        val createTerritoryRequest = CreateTerritoryRequest(
            internalTitle = "$TEST_PREFIX internalTitle$random"
        )

        val territory = CmsService.createTerritory(createTerritoryRequest)

        if (publish) {
            CmsService.publishTerritory(territory.id)
        }

        return territory
    }

    /**
     * Unpublishes and deletes a territory.
     *
     * @param territoryId The unique identifier of the territory to delete.
     */
    fun deleteTerritory(territoryId: String) {
        CmsService.unpublishTerritory(territoryId)
        CmsService.deleteTerritory(territoryId)
    }
}
