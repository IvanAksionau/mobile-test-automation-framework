package com.ivan.automation.core.mobile.driver.factory

import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.driver.capabilities.AndroidCapabilitiesProvider
import com.ivan.automation.core.mobile.driver.capabilities.IOSCapabilitiesProvider
import com.ivan.automation.core.mobile.driver.interfaces.ICapabilitiesProvider
import com.ivan.automation.core.mobile.driver.interfaces.IDriverFactory
import com.ivan.automation.core.mobile.utils.CustomException
import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.ios.IOSDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.net.Urls
import org.openqa.selenium.remote.http.ClientConfig
import org.openqa.selenium.remote.http.HttpClient
import java.time.Duration

/**
 * Factory for creating Appium drivers.
 * Follows the Factory Pattern and Dependency Inversion Principle (DIP).
 */
class AppiumDriverFactory : IDriverFactory {

    private companion object {
        const val DEFAULT_HTTP_READ_TIMEOUT_MINUTES: Long = 5
    }

    private val capabilitiesProviders: List<ICapabilitiesProvider> = listOf(
        AndroidCapabilitiesProvider(),
        IOSCapabilitiesProvider()
    )

    /**
     * Creates and configures an AppiumDriver instance.
     *
     * @return Configured AppiumDriver instance
     * @throws CustomException if driver creation fails
     */
    override fun createDriver(): AppiumDriver {
        val url = Urls.from(getHubUrl()).toURL()
        val capabilitiesProvider = getCapabilitiesProvider()

        return try {
            val httpFactory = createHttpClientFactory()
            val capabilities = capabilitiesProvider.getCapabilities()

            val driver = when (AppiumProperties.PLATFORM_NAME) {
                AppiumProperties.ANDROID_PLATFORM ->
                    AndroidDriver(url, httpFactory, capabilities)

                AppiumProperties.IOS_PLATFORM ->
                    IOSDriver(url, capabilities)

                else -> throw CustomException(
                    "PlatformName '${AppiumProperties.PLATFORM_NAME}' is not a valid option."
                )
            }

            driver.manage().timeouts()
                .implicitlyWait(Duration.ofSeconds(AppiumProperties.IMPLICIT_WAIT_TIMEOUT.toLong()))
            driver
        } catch (e: WebDriverException) {
            throw CustomException("Failed to create Appium session: ${e.message}", e)
        }
    }

    private fun getCapabilitiesProvider(): ICapabilitiesProvider {
        return capabilitiesProviders.firstOrNull {
            it.supports(AppiumProperties.PLATFORM_NAME)
        } ?: throw CustomException(
            "No capabilities provider found for platform: ${AppiumProperties.PLATFORM_NAME}"
        )
    }

    private fun createHttpClientFactory(): HttpClient.Factory {
        val defaultFactory = HttpClient.Factory.createDefault()

        return object : HttpClient.Factory {
            override fun createClient(config: ClientConfig): HttpClient {
                val timedConfig = config.readTimeout(Duration.ofMinutes(DEFAULT_HTTP_READ_TIMEOUT_MINUTES))
                return defaultFactory.createClient(timedConfig)
            }

            override fun cleanupIdleClients() = defaultFactory.cleanupIdleClients()
        }
    }

    private fun getHubUrl(): String {
        if (AppiumProperties.BS_USER.isNotEmpty()) {
            if (AppiumProperties.HUB_URL.isEmpty()) {
                throw CustomException("framework.mobile.hub.url NULL or Empty")
            }
            return "https://${AppiumProperties.BS_USER}:${AppiumProperties.BS_PASSWORD}" +
                    "@hub-cloud.browserstack.com/wd/hub"
        }
        return AppiumProperties.HUB_URL
    }
}
