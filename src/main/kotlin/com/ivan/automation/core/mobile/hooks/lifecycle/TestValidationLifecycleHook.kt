package com.ivan.automation.core.mobile.hooks.lifecycle

import com.ivan.automation.core.mobile.assertions.SoftAssertManager
import com.ivan.automation.core.mobile.hooks.interfaces.ITestLifecycleHook
import com.ivan.automation.core.mobile.screen.MobileBaseScreen
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.SkipException

/**
 * Manages test validation lifecycle during test execution.
 * Handles only test validation and skip logic.
 */
class TestValidationLifecycleHook : ITestLifecycleHook {
    
    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    
    override fun beforeScenario(scenario: Scenario) {
        skipTestIfIgnored(scenario)
    }
    
    override fun afterScenario(scenario: Scenario) {
        SoftAssertManager.assertAll()
    }
    
    /**
     * Checks if the test is annotated to ignore execution for a specific platform.
     * If the scenario has a relevant ignore tag, a SkipException is thrown to skip the test.
     *
     * @param scenario The scenario currently being executed.
     */
    private fun skipTestIfIgnored(scenario: Scenario) {
        val tags = scenario.sourceTagNames.toList()
        val name = scenario.name
        val skipMessage = when {
            MobileBaseScreen.isAndroid && "@ignore_android" in tags ->
                "Test '$name' skipped for Android platform due to lack of implementation."
            
            !MobileBaseScreen.isAndroid && "@ignore_iOS" in tags ->
                "Test '$name' skipped for iOS platform due to lack of implementation."
            
            "@ignore" in tags ->
                "Test '$name' was skipped because of '@ignore' annotation."
            
            else -> null
        }
        
        if (skipMessage != null) {
            log.warn(skipMessage)
            throw SkipException(skipMessage)
        }
    }
}
