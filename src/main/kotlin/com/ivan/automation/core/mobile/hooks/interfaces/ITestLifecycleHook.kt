package com.ivan.automation.core.mobile.hooks.interfaces

import io.cucumber.java.Scenario

/**
 * Interface for test lifecycle hooks.
 */
interface ITestLifecycleHook {
    /**
     * Executes before a test scenario starts.
     *
     * @param scenario The Cucumber scenario
     */
    fun beforeScenario(scenario: Scenario)
    
    /**
     * Executes after a test scenario completes.
     *
     * @param scenario The Cucumber scenario
     */
    fun afterScenario(scenario: Scenario)
}