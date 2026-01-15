package com.ivan.automation.core.mobile.driver.capabilities

import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.driver.interfaces.ICapabilitiesProvider
import com.ivan.automation.core.mobile.utils.BrowserStackUtil
import io.appium.java_client.ios.options.XCUITestOptions
import org.openqa.selenium.Capabilities
import java.time.Duration

/**
 * Provides iOS-specific capabilities.
 * Handles only iOS capabilities.
 */
class IOSCapabilitiesProvider : ICapabilitiesProvider {

    private companion object {
        const val DEFAULT_CONNECTION_RETRY_COUNT = 5
        const val DEFAULT_WAIT_FOR_IDLE_TIMEOUT = 5000
        const val IOS_PLATFORM = "iOS"
        const val BUNDLE_ID = "com.ivan.plus.ios"
    }

    override fun getCapabilities(): Capabilities {
        val xcuiTestOptions = XCUITestOptions()
            .setUdid(AppiumProperties.DEVICE_UDID)
            .setPlatformName(AppiumProperties.PLATFORM_NAME)
            .setDeviceName(AppiumProperties.DEVICE_NAME)
            .setPlatformVersion(AppiumProperties.PLATFORM_VERSION)
            .setFullReset(false)
            .setNoReset(false)
            .setOrientation(AppiumProperties.ORIENTATION)
            .setNewCommandTimeout(Duration.ofMinutes(AppiumProperties.NEW_COMMAND_TIMEOUT.toLong()))
            .setWdaStartupRetryInterval(
                Duration.ofSeconds(AppiumProperties.WDA_STARTUP_RETRY_INTERVAL.toLong())
            )
            .setLocale("en_US")
            .setApp(getAppPath())

        xcuiTestOptions.setCapability("connectionRetryCount", DEFAULT_CONNECTION_RETRY_COUNT)
        xcuiTestOptions.setCapability("wdaLaunchTimeout", AppiumProperties.AVD_LAUNCH_TIME_OUT)
        xcuiTestOptions.setCapability("waitForIdleTimeout", DEFAULT_WAIT_FOR_IDLE_TIMEOUT)
        xcuiTestOptions.setCapability("showXcodeLog", true)
        xcuiTestOptions.setCapability("allowProvisioningUpdates", true)
        xcuiTestOptions.setCapability("bundleId", BUNDLE_ID)

        if (isRemoteSession()) {
            xcuiTestOptions.setCapability("bstack:options", BrowserStackOptionsProvider.getBrowserStackOptions())
        }

        return xcuiTestOptions
    }

    override fun supports(platformName: String): Boolean {
        return platformName.equals(IOS_PLATFORM, ignoreCase = true)
    }

    private fun getAppPath(): String {
        return if (isRemoteSession()) {
            BrowserStackUtil.uploadIpaFile(AppiumProperties.MOBILE_APP)
        } else {
            AppiumProperties.MOBILE_APP
        }
    }
}
