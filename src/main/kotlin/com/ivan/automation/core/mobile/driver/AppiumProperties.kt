package com.ivan.automation.core.mobile.driver

import com.ivan.automation.core.mobile.utils.CustomException
import com.ivan.automation.core.mobile.utils.PropertiesManager
import org.openqa.selenium.ScreenOrientation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object AppiumProperties {
    private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    private const val DEFAULT_FALSE = "false"
    private const val DEFAULT_TRUE = "true"
    private const val DEFAULT_WDA_STARTUP_RETRY_INTERVAL = "20"

    const val ANDROID_PLATFORM: String = "Android"
    const val IOS_PLATFORM: String = "iOS"
    val PLATFORM_NAME: String = PropertiesManager.getParameter("framework.platform.name")
    val PLATFORM_VERSION: String = PropertiesManager.getParameter("framework.platform.version")
    val DEVICE_NAME: String = PropertiesManager.getParameter("framework.device.name")
    val MOBILE_APP: String = PropertiesManager.getParameter("framework.mobile.app")
    val BS_API_URL: String = PropertiesManager.getParameter("browserstack.upload.url")
    val BS_USER: String = PropertiesManager.getParameter("framework.bs.user")
    val BS_PASSWORD: String = PropertiesManager.getParameter("framework.bs.password")
    val ORIENTATION: ScreenOrientation = if (getOptionalParameterOrDefault(
            "framework.mobile.orientation",
            ScreenOrientation.PORTRAIT.value()
        ).equals("portrait", ignoreCase = true)
    ) ScreenOrientation.PORTRAIT else ScreenOrientation.LANDSCAPE
    val DEVICE_UDID: String = PropertiesManager.getParameter("framework.device.udid")
    val HUB_URL: String = PropertiesManager.getParameter("framework.mobile.hub.url")
    val FULL_RESET: Boolean = getOptionalParameterOrDefault("framework.mobile.full.reset", DEFAULT_TRUE).toBoolean()
    val NO_RESET: Boolean = getOptionalParameterOrDefault("framework.mobile.no.reset", DEFAULT_FALSE).toBoolean()
    val ANDROID_INSTALL_TIME_OUT: Int =
        PropertiesManager.getParameter("framework.mobile.android.install.timeout").toInt()
    val ANDROID_ADB_EXEC_TIME_OUT: Int =
        PropertiesManager.getParameter("framework.mobile.android.adb.exec.timeout").toInt()
    val ANDROID_DEVICE_READY_TIME_OUT: Int =
        PropertiesManager.getParameter("framework.mobile.android.device.ready.timeout").toInt()
    val AVD_LAUNCH_TIME_OUT: Int =
        PropertiesManager.getParameter("framework.mobile.avd.launch.timeout").toInt()
    val APP_VERSION: String = getOptionalParameterOrDefault("framework.mobile.app.version", "<UnknownAppVersion>")
    val NEW_COMMAND_TIMEOUT: Int = PropertiesManager.getParameter("framework.mobile.new.command.timeout").toInt()
    val IMPLICIT_WAIT_TIMEOUT: Int = PropertiesManager.getParameter("framework.mobile.implicit.wait.timeout").toInt()
    val WDA_STARTUP_RETRY_INTERVAL: Int = getOptionalParameterOrDefault(
        "framework.mobile.ios.wda.startup.retry.interval",
        DEFAULT_WDA_STARTUP_RETRY_INTERVAL
    ).toInt()
    val MAX_RETRY_COUNT: Int = getOptionalParameterOrDefault("framework.failure.retry.test", "1").toInt()

    /**
     * Retrieves an optional parameter from the properties manager or returns the default value if
     * not found.
     *
     * @param parameterName The name of the parameter to retrieve.
     * @param defaultValue  The default value to return if the parameter is not found.
     * @return The value of the parameter or the default value if not present.
     */
    private fun getOptionalParameterOrDefault(parameterName: String, defaultValue: String): String {
        return try {
            PropertiesManager.getParameter(parameterName).takeIf { it.isNotEmpty() } ?: defaultValue
        } catch (e: CustomException) {
            logger.warn("Parameter '{}' not found: {}", parameterName, e.message)
            defaultValue
        }
    }
}
