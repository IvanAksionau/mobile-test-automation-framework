package com.ivan.automation.core.mobile.utils

import com.ivan.automation.core.mobile.environment.Environments
import java.io.IOException
import java.util.*

class PropertiesManager {

    private val properties: Properties = Properties()

    /**
     * Constructs a new PropertiesManager and loads properties from the specified file.
     */
    init {
        loadProperties()
    }

    /**
     * Loads properties from the file associated with this instance.
     */
    private fun loadProperties() {
        val classLoader = Thread.currentThread().contextClassLoader
        try {
            val commonProp = classLoader.getResourceAsStream("properties/common.properties")
            val environment = System.getProperty("environment") ?: Environments.STG.environmentName
            val environmentName = Environments.byName(environment).environmentName
            val envPropName = "properties/$environmentName$PROPERTIES_EXTENSION"
            val envProp = classLoader.getResourceAsStream(envPropName)
                ?: throw CustomException("Missing environment properties file: $envPropName")
            properties.load(commonProp)
            properties.load(envProp)
        } catch (exception: IOException) {
            throw CustomException("Error loading properties files", exception)
        }
    }

    /**
     * Retrieves the value of the specified property key.
     *
     * @param key the property key to retrieve
     * @return the property value
     */
    fun getProperty(key: String): String? {
        return properties.getProperty(key.trim { it <= ' ' })
    }

    companion object {
        private const val PROPERTIES_EXTENSION = ".properties"

        @Volatile
        private var instance: PropertiesManager? = null

        /**
         * Retrieves the singleton instance of PropertiesManager.
         * If not initialized, it creates a new one.
         */
        private fun getInstance(): PropertiesManager {
            return instance ?: synchronized(this) {
                instance ?: PropertiesManager().also { instance = it }
            }
        }

        /**
         * Formats a given string to match the naming conventions of environment variables.
         * It converts all characters to uppercase and replaces periods with underscores.
         */
        private fun formatAsEnvVar(variable: String): String {
            return variable.uppercase(Locale.getDefault()).replace(".", "_")
        }

        /**
         * Retrieves the value of the specified parameter from system environment variables,
         * system properties, or configuration properties in that order.
         */
        fun getParameter(key: String): String {
            var value = getSystemEnvVariable(key)
            if (value == null) {
                value = getSystemProperty(key)
                if (value == null) {
                    value = getConfigProperty(key)
                }
            }
            if (value == null) {
                val comment = "=value"
                val message = String.format(
                    "Unable to get '%s' parameter. You can set the property as follows:%n"
                            + "option 1: as an input in .properties file: %s%n"
                            + "option 2: in the command line parameter as -D%s%n"
                            + "--if the parameter is env specific you should create a key per env",
                    key, key + comment, key + comment
                )
                throw CustomException(message)
            }
            return value
        }

        /**
         * Retrieves a property value from configuration properties.
         */
        private fun getConfigProperty(property: String): String? {
            return getInstance().getProperty(property)
        }

        /**
         * Retrieves a system environment variable value.
         */
        private fun getSystemEnvVariable(variable: String): String? {
            return System.getenv(formatAsEnvVar(variable))
        }

        /**
         * Retrieves a system property value.
         */
        private fun getSystemProperty(property: String?): String? {
            return property?.let { System.getProperty(it) }
        }
    }
}
