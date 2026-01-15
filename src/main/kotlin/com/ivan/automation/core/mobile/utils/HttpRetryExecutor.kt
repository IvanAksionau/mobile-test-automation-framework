package com.ivan.automation.core.mobile.utils

import com.ivan.automation.core.mobile.api.wrapper.ResponseWrapper
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

/**
 * Singleton HTTP-specific retry executor.
 */
object HttpRetryExecutor : RetryExecutor<ResponseWrapper>() {
    private const val DEFAULT_WAIT_DURATION_SECONDS = 100L
    private const val DEFAULT_POLL_INTERVAL_MILLIS = 3_000L
    private var poolDuration: Duration = Duration.ZERO
    private var poolIntervalMillis: Long = 0

    private val log = LoggerFactory.getLogger(HttpRetryExecutor::class.java)

    /**
     * Configures polling duration and interval.
     */
    fun setPool(
        poolDuration: Duration = Duration.ofSeconds(DEFAULT_WAIT_DURATION_SECONDS),
        poolIntervalMillis: Long = DEFAULT_POLL_INTERVAL_MILLIS
    ): HttpRetryExecutor {
        this.poolDuration = poolDuration
        this.poolIntervalMillis = poolIntervalMillis
        return this
    }

    /**
     * Executes the HTTP call until the condition is met or the timeout elapses.
     */
    override fun execute(): ResponseWrapper {
        val start = LocalDateTime.now()

        // Initial checks
        check(expression != null) { "Expression must be provided before executing" }
        check(condition != null) { "Condition must be provided before executing" }

        var result = expression!!.invoke()
        var conditionSucceeded = condition!!.invoke(result)

        // Retry loop
        while (!conditionSucceeded && Duration.between(start, LocalDateTime.now()) < poolDuration) {
            try {
                Thread.sleep(poolIntervalMillis)
            } catch (e: InterruptedException) {
                log.error("Interrupted during retry sleep", e)
                Thread.currentThread().interrupt()
            }

            result = expression!!.invoke()
            log.info("Retrying expression: $expression at ${LocalDateTime.now()}")
            conditionSucceeded = condition!!.invoke(result)
        }

        // If we’ve exited without success, fail fast
        check(conditionSucceeded) {
            throw CustomException(
                "Condition was not met within ${poolDuration.toMinutes()} for expression: $expression"
            )
        }

        return result
    }
}
