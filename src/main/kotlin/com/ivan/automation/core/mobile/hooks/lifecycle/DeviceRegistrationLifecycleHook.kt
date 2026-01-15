package com.ivan.automation.core.mobile.hooks.lifecycle

import com.ivan.automation.core.mobile.api.models.request.RegisterDeviceRequest
import com.ivan.automation.core.mobile.api.services.GandalfService
import com.ivan.automation.core.mobile.api.services.GateKeeperService
import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.hooks.interfaces.ITestLifecycleHook
import com.ivan.automation.core.mobile.screen.MobileBaseScreen
import com.ivan.automation.core.mobile.utils.TestContextConstants
import com.ivan.automation.core.mobile.utils.TestContextManager
import io.cucumber.java.Scenario
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Manages device registration lifecycle during test execution.
 * Handles only device registration.
 */
class DeviceRegistrationLifecycleHook : ITestLifecycleHook {

    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    private val driverManager = ThreadLocalDriverManager

    override fun beforeScenario(scenario: Scenario) {
        registerDevice()
    }

    override fun afterScenario(scenario: Scenario) {
        // No cleanup needed for device registration
    }

    private fun registerDevice() {
        val driver = driverManager.getDriver()
            ?: throw IllegalStateException("Driver must be initialized before device registration")

        val request = RegisterDeviceRequest(
            profile = "MOBILE",
            architecture = AppiumProperties.DEVICE_NAME,
            appVersion = AppiumProperties.APP_VERSION,
            osVersion = AppiumProperties.PLATFORM_VERSION,
            osName = AppiumProperties.PLATFORM_NAME,
            model = driver.capabilities.getCapability("deviceModel") as? String ?: "Unknown",
            manufacturer = driver.capabilities.getCapability("deviceManufacturer") as? String ?: "Unknown",
            store = if (MobileBaseScreen.isAndroid) "GENERIC_ANDROID" else "APPLE_APPSTORE"
        )

        val response = GateKeeperService.registerDevice(request)

        TestContextManager.getTestContext().set(TestContextConstants.X_CHILI_DEVICE_STORE, request.store)
        TestContextManager.getTestContext().set(TestContextConstants.X_CHILI_DEVICE_ID, response.deviceToken)

        val clientResolution = GandalfService.getClientResolution()
        TestContextManager.getTestContext().set(TestContextConstants.CLIENT_RESOLUTION_DATA, clientResolution)

        log.info("Device registered successfully with token: {}", response.deviceToken)
    }
}
