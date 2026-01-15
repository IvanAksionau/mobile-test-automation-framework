package com.ivan.automation.core.mobile.driver.manager

import com.ivan.automation.core.mobile.driver.interfaces.IDriverManager
import io.appium.java_client.AppiumDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Thread-safe driver manager using ThreadLocal storage.
 * Implemented as a singleton object to ensure single instance across the application.
 * Manages only driver lifecycle.
 */
object ThreadLocalDriverManager : IDriverManager {

    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    private val driverThreadLocal = ThreadLocal<AppiumDriver>()

    override fun getDriver(): AppiumDriver {
        val driver = driverThreadLocal.get()
            ?: throw IllegalStateException("Driver not initialized. Ensure driver is set before creating screen objects.")
        return driver
    }

    override fun setDriver(driver: AppiumDriver) {
        driverThreadLocal.set(driver)
        log.debug("Driver set for thread: ${Thread.currentThread().name}")
    }

    override fun removeDriver() {
        val driver = driverThreadLocal.get()
        if (driver != null) {
            try {
                driver.quit()
                log.info("Driver quit successfully for thread: ${Thread.currentThread().name}")
            } catch (e: Exception) {
                log.error("Error quitting driver: ${e.message}", e)
            } finally {
                driverThreadLocal.remove()
                log.debug("Driver removed from thread: ${Thread.currentThread().name}")
            }
        } else {
            log.warn("No driver instance found for thread: ${Thread.currentThread().name}")
        }
    }

    override fun hasDriver(): Boolean {
        return driverThreadLocal.get() != null
    }
}
