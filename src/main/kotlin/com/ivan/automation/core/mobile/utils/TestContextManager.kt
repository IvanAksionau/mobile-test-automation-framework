package com.ivan.automation.core.mobile.utils

object TestContextManager {
    private val testContext = ThreadLocal<TestContext>()

    /**
     * Retrieves the current thread's `TestContext` instance, creating it if it does not exist.
     * This ensures a unique `TestContext` for each thread.
     *
     * @return The `TestContext` associated with the current thread.
     */
    fun getTestContext(): TestContext {
        if (testContext.get() == null) {
            testContext.set(TestContext())
        }
        return testContext.get()
    }

    /**
     * Clears the `TestContext` for the current thread.
     */
    fun cleanTestContext() {
        if (testContext.get() != null) {
            testContext.remove()
        }
    }
}
