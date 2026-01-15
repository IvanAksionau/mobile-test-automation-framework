package com.ivan.automation.core.mobile.screen

import com.ivan.automation.core.mobile.driver.AppiumProperties.ANDROID_PLATFORM
import com.ivan.automation.core.mobile.driver.AppiumProperties.PLATFORM_NAME
import com.ivan.automation.core.mobile.driver.AppiumProperties.PLATFORM_VERSION
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import io.appium.java_client.pagefactory.AppiumFieldDecorator
import org.openqa.selenium.support.PageFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Base class for all mobile screen objects.
 * Provides common functionality for page object initialization and driver management.
 * Use ThreadLocalDriverManager for centralized driver management.
 */
abstract class MobileBaseScreen protected constructor() {
    private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    /**
     * Constructs a MobileBaseScreen object and initializes page elements.
     * Uses ThreadLocalDriverManager to get the driver instance.
     */
    init {
        val driver = ThreadLocalDriverManager.getDriver()
        try {
            PageFactory.initElements(AppiumFieldDecorator(driver), this)
        } catch (e: IllegalStateException) {
            logger.error(
                "PageFactory initialization failed due to an illegal state. "
                        + "Possible issues: Driver not initialized properly. Exception: {}",
                e.message, e
            )
        }
    }

    companion object {
        val isAndroid: Boolean
            /**
             * Checks if the current platform is Android.
             *
             * @return `true` if the current platform is Android, `false` otherwise.
             */
            get() = PLATFORM_NAME.equals(ANDROID_PLATFORM, ignoreCase = true)

        val osVersion: Double
            get() = PLATFORM_VERSION.toDouble()
    }
}
