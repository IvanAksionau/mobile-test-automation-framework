package com.ivan.automation.core.mobile.hooks.lifecycle

import com.ivan.automation.core.mobile.driver.factory.AppiumDriverFactory
import com.ivan.automation.core.mobile.driver.interfaces.IDriverFactory
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.hooks.interfaces.ITestLifecycleHook
import com.ivan.automation.core.mobile.utils.TestContextManager
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manages driver lifecycle during test execution.
 * Handles only driver lifecycle.
 */
class DriverLifecycleHook(
    private val driverFactory: IDriverFactory = AppiumDriverFactory()
) : ITestLifecycleHook {

    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    override fun beforeScenario(scenario: Scenario) {
        val driver = driverFactory.createDriver()
        ThreadLocalDriverManager.setDriver(driver)

        val sessionId = driver.sessionId.toString()
        TestContextManager.getTestContext().set("sessionId", sessionId)
        log.info("Driver initialized with SessionId: {}", sessionId)
    }

    override fun afterScenario(scenario: Scenario) {
        ThreadLocalDriverManager.removeDriver()
        log.info("Driver closed for scenario: {}", scenario.name)
    }
}
