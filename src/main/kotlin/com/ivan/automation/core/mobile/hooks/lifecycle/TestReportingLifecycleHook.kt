package com.ivan.automation.core.mobile.hooks.lifecycle

import com.ivan.automation.core.mobile.allure.AllureLogger
import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.hooks.interfaces.ITestLifecycleHook
import io.cucumber.java.Scenario
import org.openqa.selenium.JavascriptExecutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manages test reporting lifecycle during test execution.
 * Handles only test reporting and evidence capture.
 */
class TestReportingLifecycleHook : ITestLifecycleHook {

    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    override fun beforeScenario(scenario: Scenario) {
        // No setup needed for reporting
    }

    override fun afterScenario(scenario: Scenario) {
        attachEvidenceIfFailed(scenario)
        setBrowserStackStatus(scenario)
    }

    private fun attachEvidenceIfFailed(scenario: Scenario) {
        if (scenario.isFailed) {
            try {
                AllureLogger.attachScreenShot("Screenshot with the error")
                log.info("Screenshot attached for failed scenario: {}", scenario.name)
            } catch (e: IllegalStateException) {
                log.warn("Cannot take screenshot, driver not initialized: {}", e.message)
            }
        }
    }

    private fun setBrowserStackStatus(scenario: Scenario) {
        if (AppiumProperties.BS_USER.isEmpty()) {
            return
        }

        val driver = try {
            ThreadLocalDriverManager.getDriver()
        } catch (e: IllegalStateException) {
            log.warn("Cannot set BrowserStack status, driver not initialized: {}", e.message)
            return
        }

        val failed = scenario.isFailed
        val status = if (failed) "failed" else "passed"
        val reason = if (failed) "One or more steps are failed" else "All scenario steps are passed"

        try {
            (driver as JavascriptExecutor).executeScript(
                """
                browserstack_executor: {
                  "action": "setSessionStatus",
                  "arguments": {
                    "status": "$status",
                    "reason": "$reason"
                  }
                }
                """.trimIndent()
            )
            log.info("BrowserStack status set to: {}", status)
        } catch (e: Exception) {
            log.error("Failed to set BrowserStack status: ${e.message}", e)
        }
    }
}
