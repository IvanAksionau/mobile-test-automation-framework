package com.ivan.automation.core.mobile.assertions

import com.ivan.automation.core.mobile.allure.AllureLogger
import io.qameta.allure.Allure
import io.qameta.allure.model.Status
import io.qameta.allure.model.StepResult
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.asserts.IAssert
import org.testng.asserts.SoftAssert
import java.util.*

class CustomSoftAssert : SoftAssert() {

    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    /**
     * Logs and reports a successful assertion to Allure.
     *
     * @param assertCommand The assertion command that was successfully executed.
     */
    override fun onAssertSuccess(assertCommand: IAssert<*>) {
        super.onAssertSuccess(assertCommand)
        val successMessage = "ASSERTION PASSED: " + assertCommand.message
        log.info(successMessage)
        val stepUuid: String = UUID.randomUUID().toString()
        Allure.getLifecycle()
            .startStep(stepUuid, StepResult().setName(successMessage).setStatus(Status.PASSED))
        Allure.getLifecycle().stopStep(stepUuid)
    }

    /**
     * Logs a failed assertion, captures a screenshot, and reports the failure to Allure.
     *
     * @param assertCommand The assertion command that failed.
     * @param ex            The AssertionError thrown by the failed assertion.
     */
    override fun onAssertFailure(assertCommand: IAssert<*>, ex: AssertionError) {
        super.onAssertFailure(assertCommand, ex)
        val failureMessage =
            "ASSERTION FAILED: " + assertCommand.message + " - " + ex.message
        log.error(failureMessage)
        val stepUuid: String = UUID.randomUUID().toString()
        Allure.getLifecycle()
            .startStep(stepUuid, StepResult().setName(failureMessage).setStatus(Status.FAILED))
        AllureLogger.attachScreenShot("assertScreenShoot")
        Allure.getLifecycle().stopStep(stepUuid)

        // Update the current step status to FAILED to reflect the assertion failure
        try {
            Allure.getLifecycle().updateStep { step ->
                step.status = Status.FAILED
            }
        } catch (e: Exception) {
            log.warn("Could not update parent step status: ${e.message}")
        }
    }
}
