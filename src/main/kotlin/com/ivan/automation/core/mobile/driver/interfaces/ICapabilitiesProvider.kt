package com.ivan.automation.core.mobile.driver.interfaces

import com.ivan.automation.core.mobile.driver.AppiumProperties
import org.openqa.selenium.Capabilities

/**
 * Interface for providing platform-specific capabilities.
 * Follows the Open/Closed Principle (OCP) - open for extension, closed for modification.
 */
interface ICapabilitiesProvider {
    /**
     * Provides platform-specific capabilities for driver initialization.
     *
     * @return Configured Capabilities object
     */
    fun getCapabilities(): Capabilities

    /**
     * Checks if this provider supports the given platform.
     *
     * @param platformName The platform name to check
     * @return true if this provider supports the platform, false otherwise
     */
    fun supports(platformName: String): Boolean

    fun isRemoteSession(): Boolean {
        return AppiumProperties.BS_USER.isNotEmpty() && AppiumProperties.BS_PASSWORD.isNotEmpty()
    }
}
