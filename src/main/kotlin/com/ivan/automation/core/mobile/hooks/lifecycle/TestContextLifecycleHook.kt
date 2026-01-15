package com.ivan.automation.core.mobile.hooks.lifecycle

import com.ivan.automation.core.mobile.hooks.interfaces.ITestLifecycleHook
import com.ivan.automation.core.mobile.utils.TestContextManager
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manages test context lifecycle during test execution.
 * Handles only test context initialization.
 */
class TestContextLifecycleHook : ITestLifecycleHook {
    
    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    
    override fun beforeScenario(scenario: Scenario) {
        log.info("Starting scenario: {}", scenario.name)
        TestContextManager.getTestContext().set("ScenarioName", scenario.name)
        TestContextManager.getTestContext().set("tags", scenario.sourceTagNames)
    }
    
    override fun afterScenario(scenario: Scenario) {
        log.debug("Test context cleanup for scenario: {}", scenario.name)
    }
}