package com.ivan.automation.core.mobile.driver.capabilities

import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.driver.interfaces.ICapabilitiesProvider
import com.ivan.automation.core.mobile.utils.BrowserStackUtil
import io.appium.java_client.android.options.UiAutomator2Options
import org.openqa.selenium.Capabilities
import java.time.Duration

/**
 * Provides Android-specific capabilities.
 * Handles only Android capabilities.
 */
class AndroidCapabilitiesProvider : ICapabilitiesProvider {

    private companion object {
        const val DEFAULT_CONNECTION_RETRY_COUNT = 5
        const val APP_WAIT_DURATION = 300000
        const val U2_SERVER_LAUNCH_TIMEOUT_MS = 100_000
        const val U2_SERVER_INSTALL_TIMEOUT_MS = 200_000
        const val ANDROID_INSTALL_TIMEOUT_MINUTES = 10L
        const val ANDROID_PLATFORM = "Android"
    }

    override fun getCapabilities(): Capabilities {
        val automator2Options = UiAutomator2Options()
            .setDeviceName(AppiumProperties.DEVICE_NAME)
            .setPlatformName(AppiumProperties.PLATFORM_NAME)
            .setPlatformVersion(AppiumProperties.PLATFORM_VERSION)
            .setFullReset(AppiumProperties.FULL_RESET)
            .setNoReset(AppiumProperties.NO_RESET)
            .setOrientation(AppiumProperties.ORIENTATION)
            .setNewCommandTimeout(Duration.ofMinutes(AppiumProperties.NEW_COMMAND_TIMEOUT.toLong()))
            .setAndroidInstallTimeout(Duration.ofSeconds(AppiumProperties.ANDROID_INSTALL_TIME_OUT.toLong()))
            .setAdbExecTimeout(Duration.ofSeconds(AppiumProperties.ANDROID_ADB_EXEC_TIME_OUT.toLong()))
            .setAvdReadyTimeout(Duration.ofMinutes(AppiumProperties.ANDROID_DEVICE_READY_TIME_OUT.toLong()))
            .setAvdLaunchTimeout(Duration.ofSeconds(AppiumProperties.AVD_LAUNCH_TIME_OUT.toLong()))
            .setApp(getAppPath())

        automator2Options.setCapability("connectionRetryCount", DEFAULT_CONNECTION_RETRY_COUNT)
        automator2Options.setCapability("appWaitDuration", APP_WAIT_DURATION)
        automator2Options.setCapability("disableIdLocatorAutocompletion", true)
        automator2Options.setCapability("skipDeviceInitialization", false)
        automator2Options.setCapability("uiautomator2ServerLaunchTimeout", U2_SERVER_LAUNCH_TIMEOUT_MS)
        automator2Options.setCapability("uiautomator2ServerInstallTimeout", U2_SERVER_INSTALL_TIMEOUT_MS)
        automator2Options.setCapability("allowTestPackages", true)
        automator2Options.setCapability("skipLocaleReset", true)
        automator2Options.setAndroidInstallTimeout(Duration.ofMinutes(ANDROID_INSTALL_TIMEOUT_MINUTES))
        automator2Options.setLanguage("en")
        automator2Options.setLocale("US")

        if (isRemoteSession()) {
            automator2Options.setCapability("bstack:options", BrowserStackOptionsProvider.getBrowserStackOptions())
        }

        return automator2Options
    }

    override fun supports(platformName: String): Boolean {
        return platformName.equals(ANDROID_PLATFORM, ignoreCase = true)
    }

    private fun getAppPath(): String {
        return if (isRemoteSession()) {
            BrowserStackUtil.uploadIpaFile(AppiumProperties.MOBILE_APP)
        } else {
            AppiumProperties.MOBILE_APP
        }
    }
}
