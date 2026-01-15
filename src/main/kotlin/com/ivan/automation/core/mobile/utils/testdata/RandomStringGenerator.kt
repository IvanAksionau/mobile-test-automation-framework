package com.ivan.automation.core.mobile.utils.testdata

import java.util.*

/**
 * Utility for generating random strings for test data.
 */
object RandomStringGenerator {

    private const val SHORT_STRING_LENGTH: Int = 10

    /**
     * Generate a random alphanumeric string of the specified length.
     *
     * @param length The desired length of the random string.
     * @return A random alphanumeric string of the given length.
     */
    fun generate(length: Int = SHORT_STRING_LENGTH): String {
        return UUID.randomUUID().toString().replace("-", "").take(length)
    }
}
