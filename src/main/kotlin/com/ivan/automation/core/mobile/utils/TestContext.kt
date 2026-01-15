package com.ivan.automation.core.mobile.utils

/**
 * A utility class for managing test data in a key-value context.
 */
@Suppress("UNCHECKED_CAST")
class TestContext {
    private val testData: MutableMap<String, Any> = HashMap()

    /**
     * Sets a value in the test context with the specified key.
     * If the value is `null`, an exception will be thrown.
     *
     * @param key   the key to associate with the value.
     * @param value the value to store; cannot be `null`.
     * @throws IllegalArgumentException if the provided value is `null`.
     */
    fun set(key: String, value: Any?) {
        if (value == null) {
            throw CustomException("Value cannot be null")
        }
        testData[key] = value
    }

    /**
     * Retrieves the value associated with the specified key from the test context.
     * If the key does not exist, an exception will be thrown.
     *
     * @param key the key for which to retrieve the value.
     * @return the value associated with the specified key.
     * @throws IllegalArgumentException if the key is not present in the test context.
     */
    fun <T> get(key: String): T {
        if (!testData.containsKey(key)) {
            throw CustomException(
                "The specified key '$key' does not exist in the test context"
            )
        }
        return testData[key] as T
    }

    /**
     * Removes a value according to the given key
     *
     * @throws IllegalArgumentException if the key is not present in the test context.
     */
    fun remove(key: String) {
        if (!testData.containsKey(key)) {
            throw CustomException(
                "The specified key '$key' does not exist in the test context"
            )
        }
        testData.remove(key)
    }

    /**
     * Retrieves the value associated with the specified key from the test context.
     * If the key does not exist, it will return the default value.
     *
     * @param key the key for which to retrieve the value.
     * @return the value associated with the specified key or default value.
     */
    fun <T> getOrDefault(key: String, defaultValue: T): T {
        return testData.getOrDefault(key, defaultValue) as T
    }

    /**
     * Checks if the test context contains a data entry associated with the specified key.
     *
     * @param key the key whose presence in the test context is to be checked
     * @return `true` if the test context contains a mapping for the specified key,
     */
    fun containsKey(key: String): Boolean {
        return testData.containsKey(key)
    }
}
