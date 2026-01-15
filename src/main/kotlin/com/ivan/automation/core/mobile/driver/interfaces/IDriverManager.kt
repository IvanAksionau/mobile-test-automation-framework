package com.ivan.automation.core.mobile.driver.interfaces

import io.appium.java_client.AppiumDriver

/**
 * Interface for managing driver lifecycle.
 * Follows the Single Responsibility Principle (SRP) - manages only driver lifecycle.
 */
interface IDriverManager {
    /**
     * Gets the current thread's driver instance.
     *
     * @return AppiumDriver instance
     * @throws IllegalStateException if driver is not initialized
     */
    fun getDriver(): AppiumDriver

    /**
     * Sets the driver instance for the current thread.
     *
     * @param driver The AppiumDriver instance to set
     */
    fun setDriver(driver: AppiumDriver)

    /**
     * Removes and quits the driver for the current thread.
     */
    fun removeDriver()

    /**
     * Checks if a driver is currently active for this thread.
     *
     * @return true if driver exists, false otherwise
     */
    fun hasDriver(): Boolean
}
