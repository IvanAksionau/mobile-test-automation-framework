package com.ivan.automation.core.mobile.assertions

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.asserts.SoftAssert

object SoftAssertManager {
    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    private val softAssertThreadLocal = ThreadLocal<CustomSoftAssert>()

    val softAssert: CustomSoftAssert
        /**
         * Retrieves the thread-local instance of CustomSoftAssert, creating it if it does not already
         * exist.
         * This ensures that each thread has its own instance of soft assertions to avoid conflicts in
         * parallel execution.
         *
         * @return The thread-local CustomSoftAssert instance.
         */
        get() {
            var softAssert = softAssertThreadLocal.get()
            if (softAssert == null) {
                softAssert = CustomSoftAssert()
                softAssertThreadLocal.set(softAssert)
            }
            return softAssert
        }

    /**
     * Clears the thread-local storage of soft asserts.
     */
    private fun clearSoftAsserts() {
        softAssertThreadLocal.remove()
    }

    /**
     * Executes all collected soft assertions for the current thread.
     * If any assertions fail, an AssertionError will be thrown.
     * After execution, all soft asserts are cleared from the thread's storage.
     */
    fun assertAll() {
        val softAssert: SoftAssert? = softAssertThreadLocal.get()
        if (softAssert != null) {
            log.info("Verifying soft asserts in the scenario")
            try {
                softAssert.assertAll()
            } catch (e: AssertionError) {
                softAssertThreadLocal.set(CustomSoftAssert())
                throw e
            }
            clearSoftAsserts()
        }
    }
}
