package com.ivan.automation.core.mobile.utils

/**
 * Abstract retry executor that runs an expression until a condition is met or a timeout occurs.
 */
abstract class RetryExecutor<T> {
    protected var expression: (() -> T)? = null
    protected var condition: ((T) -> Boolean)? = null

    /**
     * Sets the expression to execute.
     */
    fun setExpression(expression: () -> T): RetryExecutor<T> {
        this.expression = expression
        return this
    }

    /**
     * Sets the condition to evaluate the result.
     */
    fun setCondition(condition: (T) -> Boolean): RetryExecutor<T> {
        this.condition = condition
        return this
    }

    /**
     * Executes the expression, retrying until the condition is true or the timeout is reached.
     */
    abstract fun execute(): T
}
