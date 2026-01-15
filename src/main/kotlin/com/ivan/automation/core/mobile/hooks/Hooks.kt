package com.ivan.automation.core.mobile.hooks

import com.ivan.automation.core.mobile.hooks.lifecycle.DeviceRegistrationLifecycleHook
import com.ivan.automation.core.mobile.hooks.lifecycle.DriverLifecycleHook
import com.ivan.automation.core.mobile.hooks.lifecycle.TestContextLifecycleHook
import com.ivan.automation.core.mobile.hooks.lifecycle.TestDataLifecycleHook
import com.ivan.automation.core.mobile.hooks.lifecycle.TestReportingLifecycleHook
import com.ivan.automation.core.mobile.hooks.lifecycle.TestValidationLifecycleHook
import com.ivan.automation.core.mobile.listener.TestListener
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.TestDataManager
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import org.testng.annotations.Listeners

/**
 * Main Cucumber hooks class that orchestrates test lifecycle using SOLID-compliant lifecycle hooks.
 * Each lifecycle concern is delegated to a specialized hook following the Single Responsibility Principle.
 */
@Suppress("TooManyFunctions")
@Listeners(TestListener::class)
class Hooks {

    private val validationHook = TestValidationLifecycleHook()
    private val contextHook = TestContextLifecycleHook()
    private val driverHook = DriverLifecycleHook()
    private val deviceHook = DeviceRegistrationLifecycleHook()
    private val testDataHook = TestDataLifecycleHook()
    private val reportingHook = TestReportingLifecycleHook()

    /**
     * Prepares the test environment for a new scenario.
     * Delegates to specialized lifecycle hooks following the Single Responsibility Principle.
     *
     * @param scenario The scenario currently being executed.
     */
    @Before(order = 0)
    fun before(scenario: Scenario) {
        driverHook.beforeScenario(scenario)
        deviceHook.beforeScenario(scenario)
        validationHook.beforeScenario(scenario)
        contextHook.beforeScenario(scenario)
    }

    /**
     * Prepares the context of the API test data management.
     * Only executed for scenarios tagged with @run_with_test_data.
     */
    @Before("@run_with_test_data", order = 1)
    fun prepareApiDataConfig(scenario: Scenario) {
        if (!scenario.sourceTagNames.contains("@ignore")) {
            testDataHook.beforeScenario(scenario)
            TestDataManager.cleanupTopHomeBannerData()
        }
    }

    /**
     * Cleanup the data managed during test execution.
     * Only executed for scenarios tagged with @run_with_test_data.
     */
    @After("@run_with_test_data", order = 2)
    fun cleanupTestData(scenario: Scenario) {
        if (!scenario.sourceTagNames.contains("@ignore")) {
            testDataHook.afterScenario(scenario)
        }
    }

    /**
     * Provides a mechanism to close the driver after the scenario has completed.
     * Attaches evidence to the report if the scenario has failed and sets the status for BrowserStack execution.
     *
     * @param scenario The Cucumber [Scenario] object representing the test scenario.
     */
    @After(order = 1)
    fun after(scenario: Scenario) {
        validationHook.afterScenario(scenario)
        reportingHook.afterScenario(scenario)
        driverHook.afterScenario(scenario)
        TestContextManager.cleanTestContext()
    }
}
