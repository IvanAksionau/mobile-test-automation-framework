package com.ivan.automation.core.mobile.listener

import com.ivan.automation.core.mobile.allure.AllureLogger
import com.ivan.automation.core.mobile.driver.AppiumProperties.MAX_RETRY_COUNT
import com.google.common.collect.ImmutableMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.IAnnotationTransformer
import org.testng.IExecutionListener
import org.testng.IInvokedMethodListener
import org.testng.IReporter
import org.testng.IRetryAnalyzer
import org.testng.ISuite
import org.testng.ISuiteListener
import org.testng.ITestListener
import org.testng.ITestResult
import org.testng.annotations.ITestAnnotation
import org.testng.xml.XmlSuite
import java.lang.reflect.Constructor
import java.lang.reflect.Method

class TestListener :
    IAnnotationTransformer, ISuiteListener, ITestListener, IExecutionListener, IRetryAnalyzer, IInvokedMethodListener,
    IReporter {
    private var retryCount = 1
    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    override fun transform(
        annotation: ITestAnnotation, testClass: Class<*>?,
        testConstructor: Constructor<*>?, testMethod: Method
    ) {
        annotation.setRetryAnalyzer(TestListener::class.java)
    }

    override fun retry(testResult: ITestResult): Boolean {
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++
            log.info("Retrying test")
            return true
        }
        return false
    }

    override fun onExecutionStart() {
        log.info("Cleaning Allure Report.")
        super<IExecutionListener>.onExecutionStart()
        report.deleteOldAllureReports()
    }

    override fun onStart(suite: ISuite) {
        super<ISuiteListener>.onStart(suite)
    }

    override fun generateReport(
        xmlSuites: List<XmlSuite>, suites: List<ISuite>,
        outputDirectory: String
    ) {
        super<IReporter>.generateReport(xmlSuites, suites, outputDirectory)
        log.info("Setting Allure Environment")
        report.savePropertiesInReport(ImmutableMap.copyOf(commonProperties))
    }

    override fun onTestStart(testResult: ITestResult) {
        log.info("** Starting test: {}", testResult.name)
        super<ITestListener>.onTestStart(testResult)
        retryCount = 0
    }

    override fun onTestSuccess(testResult: ITestResult) {
        log.info("**** Test finished with status: PASSED")
        super<ITestListener>.onTestSuccess(testResult)
    }

    override fun onTestFailure(testResult: ITestResult) {
        if (!retry(testResult)) {
            log.error("**** Test finished with status: FAILED")
            super<ITestListener>.onTestFailure(testResult)
        }
    }

    override fun onTestSkipped(testResult: ITestResult) {
        log.warn("**** Test finished with status: SKIPPED")
        super<ITestListener>.onTestSkipped(testResult)
    }

    companion object {
        protected var report: AllureLogger = AllureLogger()
        private var commonProperties: Map<String?, String?> = HashMap()
    }
}
