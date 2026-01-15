package com.ivan.automation.core.mobile.driver.interfaces

import io.appium.java_client.AppiumDriver

/**
 * Factory interface for creating Appium drivers.
 * Follows the Dependency Inversion Principle (DIP) by depending on abstractions.
 */
interface IDriverFactory {
    /**
     * Creates and configures an AppiumDriver instance.
     *
     * @return Configured AppiumDriver instance
     * @throws com.ivan.automation.core.mobile.utils.CustomException if driver creation fails
     */
    fun createDriver(): AppiumDriver
}
