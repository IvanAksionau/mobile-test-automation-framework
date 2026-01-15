@file:OptIn(kotlin.ExperimentalStdlibApi::class)

package com.ivan.automation.core.mobile.environment

/**
 * Enum representing different environment configurations.
 */
enum class Environments(val environmentName: String) {
    INT_2("int-2"),
    STG("stg");

    companion object {
        /**
         * Get enum item by name.
         *
         * @param name The name of the environment.
         * @return The matching environment, or STG if none matches.
         */
        fun byName(name: String): Environments {
            return entries.firstOrNull { it.environmentName.equals(name, ignoreCase = true) } ?: STG
        }
    }
}
