package com.ivan.automation.core.mobile.allure

import com.ivan.automation.core.mobile.driver.AppiumProperties
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.environment.Environments
import com.github.automatedowl.tools.AllureEnvironmentWriter
import com.google.common.collect.ImmutableMap
import io.qameta.allure.Allure
import org.apache.commons.io.FileUtils
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriverException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException

class AllureLogger {

    /**
     * Saves properties in the report using the provided map.
     *
     * @param commonProperties the map containing properties to save
     */
    fun savePropertiesInReport(commonProperties: MutableMap<String?, String?>) {
        val mutableCopy = HashMap(commonProperties)
        mutableCopy["Environment"] = ENV
        mutableCopy["Device Name"] = AppiumProperties.DEVICE_NAME
        mutableCopy["Device S.O Version"] = AppiumProperties.PLATFORM_VERSION
        mutableCopy["Device S.O Name"] = AppiumProperties.PLATFORM_NAME
        mutableCopy["App Version"] = AppiumProperties.APP_VERSION
        AllureEnvironmentWriter.allureEnvironmentWriter(ImmutableMap.copyOf(mutableCopy), BASE_PATH)
    }

    fun deleteOldAllureReports() {
        val projectFile = File("target/allure-results")
        try {
            FileUtils.deleteQuietly(projectFile)
        } catch (e: IOException) {
            log.error("Failed to delete old Allure reports: {}", e.message, e)
        }
    }

    /**
     * Takes a screenshot and attaches it to the Allure report.
     */
    companion object {
        private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
        private val BASE_PATH = System.getProperty("user.dir") + "/target/allure-results/"
        val ENV: String = System.getProperty("environment")?.let {
            Environments.byName(it).environmentName
        } ?: Environments.STG.name

        fun attachScreenShot(name: String?) {
            log.info("Taking screenshot")
            try {
                val driver = ThreadLocalDriverManager.getDriver()
                val screenshot: ByteArray = driver.getScreenshotAs(OutputType.BYTES)
                Allure.addAttachment(name, ByteArrayInputStream(screenshot))
            } catch (e: WebDriverException) {
                log.error("WebDriver error while taking screenshot: {}", e.message, e)
            } catch (e: IOException) {
                log.error("Error attaching screenshot to Allure: {}", e.message, e)
            }
        }
    }
}
