package com.ivan.automation.core.mobile.utils

import io.qameta.allure.Allure
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A custom exception class that extends [RuntimeException].
 * This class is designed to handle custom exceptions in the application.
 *
 *
 * Note: Some changes in this class are implemented as workarounds to address
 * specific issues with BrowserStack.
 */
class CustomException : RuntimeException {
    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    /**
     * Constructs a new `CustomException` with the specified detail message and cause.
     *
     * @param message The detail message, saved for later retrieval by the
     * [.getMessage] method.
     */
    constructor(message: String?) : super(message)

    /**
     * Constructs a new `CustomException` with the specified detail message and cause.
     *
     * @param message The detail message, saved for later retrieval by the
     * [.getMessage] method.
     * @param cause   The cause, saved for later retrieval by the [.getCause] method.
     * A `null` value is permitted and indicates that the cause is nonexistent
     * or unknown.
     */
    constructor(message: String?, cause: Throwable) : super(message, cause) {
        log.error(cause.message)
        cause.cause?.message?.let { causeMessage ->
            log.error(causeMessage)
            Allure.addAttachment("Exception Cause", causeMessage)
        }
        cause.cause?.let { c ->
            c.message?.let { causeMessage ->
                Allure.addAttachment("Exception Cause", causeMessage)
            }
        }
        cause.message?.let { msg ->
            Allure.addAttachment("Exception Message", msg)
        }
        Allure.addAttachment("Exception StackTrace", cause.stackTrace.joinToString("\n"))
    }


    /**
     * Constructs a new `CustomException` with the specified detail message and cause.
     *
     * @param cause The cause, saved for later retrieval by the [.getCause] method.
     * A `null` value is permitted and indicates that the cause is nonexistent
     * or unknown.
     */
    constructor(cause: Throwable) : super(cause) {
        log.error(cause.message)
        cause.cause?.message?.let { causeMessage ->
            log.error(causeMessage)
            Allure.addAttachment("Exception Cause", causeMessage)
        }
        cause.message?.let { msg ->
            Allure.addAttachment("Exception Message", msg)
        }
        Allure.addAttachment("Exception StackTrace", cause.stackTrace.joinToString("\n"))
    }

    companion object {
        private const val NAME = "screenShoot"
    }
}
