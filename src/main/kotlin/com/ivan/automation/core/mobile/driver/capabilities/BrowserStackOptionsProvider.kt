package com.ivan.automation.core.mobile.driver.capabilities

import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.utils.TestContextManager

/**
 * Provides BrowserStack-specific options.
 * Handles only BrowserStack configuration.
 */
object BrowserStackOptionsProvider {

    private const val DEFAULT_IDLE_TIMEOUT = 120
    private const val APPIUM_VERSION = "2.6.0"

    /**
     * Constructs a map of BrowserStack-specific options for Appium sessions.
     *
     * @return A map containing BrowserStack options.
     */
    fun getBrowserStackOptions(): HashMap<String, Any> {
        val browserstackOptions = HashMap<String, Any>()
        val buildName = StringBuilder()
            .append("FIFA+  - ")
            .append(AppiumProperties.APP_VERSION)
            .append(", ")
            .append(AppiumProperties.DEVICE_NAME)
            .append(", ")
            .append(AppiumProperties.PLATFORM_NAME)
            .append(" ")
            .append(AppiumProperties.PLATFORM_VERSION)
            .toString()

        browserstackOptions["buildName"] = buildName
        browserstackOptions["networkLogs"] = true
        browserstackOptions["sessionName"] = TestContextManager.getTestContext().get("ScenarioName")
        browserstackOptions["debug"] = true
        browserstackOptions["interactiveDebugging"] = true
        browserstackOptions["idleTimeout"] = DEFAULT_IDLE_TIMEOUT
        browserstackOptions["appiumVersion"] = APPIUM_VERSION
        browserstackOptions["noBlankPolling"] = true

        return browserstackOptions
    }
}
