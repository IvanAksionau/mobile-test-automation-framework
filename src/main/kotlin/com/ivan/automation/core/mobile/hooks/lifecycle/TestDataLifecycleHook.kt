package com.ivan.automation.core.mobile.hooks.lifecycle

import com.ivan.automation.core.mobile.api.services.GandalfService
import com.ivan.automation.core.mobile.api.services.GateKeeperService
import com.ivan.automation.core.mobile.hooks.interfaces.ITestLifecycleHook
import com.ivan.automation.core.mobile.utils.TestContextConstants
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.TestDataManager
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manages test data lifecycle during test execution.
 * Handles only test data setup and cleanup.
 */
class TestDataLifecycleHook : ITestLifecycleHook {
    
    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    
    override fun beforeScenario(scenario: Scenario) {
        if (shouldSkipDataSetup(scenario)) {
            log.debug("Skipping test data setup for ignored scenario: {}", scenario.name)
            return
        }
        
        setupTestData()
    }
    
    override fun afterScenario(scenario: Scenario) {
        if (shouldSkipDataSetup(scenario)) {
            log.debug("Skipping test data cleanup for ignored scenario: {}", scenario.name)
            return
        }
        
        cleanupTestData()
    }
    
    private fun setupTestData() {
        // Get CMS admin token
        val request = GetAdminTokenRequest(
            xDeviceId = TestContextManager.getTestContext().get(TestContextConstants.X_CHILI_DEVICE_ID)
        )
        val token = GateKeeperService.getDashBoardAdminToken(request)
        TestContextManager.getTestContext().set(TestContextConstants.DASHBOARD_ADMIN_TOKEN, "Bearer $token")
        
        // Initialize showcases data keeper
        val showcasesList: List<String> = emptyList()
        TestContextManager.getTestContext().set(TestContextConstants.SHOWCASES_LIST, showcasesList)
        
        // Initialize contents data keeper
        val contentsList: List<String> = emptyList()
        TestContextManager.getTestContext().set(TestContextConstants.CONTENTS_LIST, contentsList)
        
        log.info("Test data setup completed")
    }
    
    private fun cleanupTestData() {
        TestDataManager.cleanupShowcasesData()
        TestDataManager.cleanupContentsData()
        TestDataManager.cleanupTerritoryData()
        log.info("Test data cleanup completed")
    }
    
    private fun shouldSkipDataSetup(scenario: Scenario): Boolean {
        return scenario.sourceTagNames.contains("@ignore")
    }
}
