package com.ivan.automation.core.mobile.utils

import com.ivan.automation.core.mobile.driver.AppiumProperties.IMPLICIT_WAIT_TIMEOUT
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.screen.MobileBaseScreen.Companion.isAndroid
import com.ivan.automation.core.mobile.utils.WaitActions.DEFAULT_WAIT_TIMEOUT
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

@Suppress("TooManyFunctions")
object WaitActions {
    private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }
    private const val DEFAULT_WAIT_TIMEOUT: Int = 3
    private const val MILLIS_IN_SECOND: Long = 1_000

    /**
     * Sets the implicit wait duration for the driver.
     */
    private fun setImplicitWait(duration: Duration) {
        ThreadLocalDriverManager.getDriver().manage().timeouts().implicitlyWait(duration)
    }

    /**
     * Resets the implicit wait duration to the default value.
     */
    private fun resetImplicitWait() {
        setImplicitWait(Duration.ofSeconds(IMPLICIT_WAIT_TIMEOUT.toLong()))
    }

    /**
     * Creates a configurable FluentWait instance, reusing settings.
     */
    private fun createFluentWait(
        driver: WebDriver,
        timeoutDuration: Duration,
        pollingDuration: Duration
    ): FluentWait<WebDriver> {
        return FluentWait(driver)
            .withTimeout(timeoutDuration)
            .pollingEvery(pollingDuration)
            .ignoring(NoSuchElementException::class.java)
            .ignoring(TimeoutException::class.java)
            .ignoring(StaleElementReferenceException::class.java)
    }

    /**
     * Waits for an element to become invisible.
     */
    fun waitForElementInvisibility(element: WebElement?, timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT): Boolean {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver() ?: return false
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until { ExpectedConditions.invisibilityOf(element).apply(it) } ?: false
        } catch (e: TimeoutException) {
            logger.warn("Element did not become invisible within $timeInSeconds seconds: ${e.message}")
            false
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Checks if an element is invisible within the given time frame.
     */
    fun isTheElementInvisible(element: WebElement?, timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT): Boolean {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver() ?: return false
            val result = createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until { ExpectedConditions.invisibilityOf(element).apply(it) }
            result == true
        } catch (e: TimeoutException) {
            logger.warn("Element is still visible after $timeInSeconds seconds: ${e.message}")
            false
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Waits for an element to be visible.
     */
    fun waitForElementVisibility(element: WebElement?, timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT): WebElement? {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver() ?: return null
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until { ExpectedConditions.visibilityOf(element).apply(it) }
        } catch (e: TimeoutException) {
            logger.warn("Element did not become visible within $timeInSeconds seconds: ${e.message}")
            null
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Waits for an element to be clickable.
     */
    fun waitForElementToBeClickable(element: WebElement?, timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT): WebElement? {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver() ?: return null
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until { ExpectedConditions.elementToBeClickable(element).apply(it) }
        } catch (e: TimeoutException) {
            logger.warn("Element did not become clickable within $timeInSeconds seconds: ${e.message}")
            null
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Checks if an element is visible within the given time frame.
     */
    fun isTheElementVisible(element: WebElement?, timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT): Boolean {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver() ?: return false
            val result = createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until { ExpectedConditions.visibilityOf(element).apply(it) }
            result != null
        } catch (e: TimeoutException) {
            logger.warn("Element is not visible after $timeInSeconds seconds: ${e.message}")
            false
        } catch (e: NoSuchElementException) {
            logger.warn("Element not found: ${e.message}")
            false
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Checks if an element located by [locator] is visible within the given time frame.
     *
     * @param locator the [By] locator of the element to check
     * @param timeInSeconds the timeout in seconds (default = [DEFAULT_WAIT_TIMEOUT])
     * @return true if the element is visible, false otherwise
     */
    fun isTheElementVisible(locator: By, timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT): Boolean {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver()
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until(ExpectedConditions.visibilityOfElementLocated(locator))
            true
        } catch (e: TimeoutException) {
            logger.warn("Element not visible after $timeInSeconds seconds: ${e.message}")
            false
        } catch (e: NoSuchElementException) {
            logger.warn("Element not found: ${e.message}")
            false
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Waits until [element] has any non-blank text, then returns that text.
     * @return the element’s text if present within the timeout, or null otherwise.
     */
    fun getElementText(element: WebElement?, timeInSeconds: Int = 10): String? {
        setImplicitWait(Duration.ZERO)

        return try {
            val driver = ThreadLocalDriverManager.getDriver()
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until {
                element != null && element.text.isNotBlank()
            }
            element?.text
        } catch (e: TimeoutException) {
            logger.warn("Timed out after $timeInSeconds s waiting for element text: ${e.message}")
            null
        } catch (e: NoSuchElementException) {
            logger.warn("Element not found when waiting for text: ${e.message}")
            null
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Waits until the given element contains the expected text value within the specified time.
     *
     * @param element The WebElement to check.
     * @param text The expected text to be present in the element.
     * @param timeInSeconds The maximum time in seconds to wait.
     * @return The element if it contains the expected text within the timeout; otherwise, null.
     */
    fun waitForElementToContainText(
        element: WebElement?,
        text: String,
        timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT
    ): WebElement? {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver()
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until<WebElement?> { _ ->
                if (element != null && element.text.contains(text)) element else null
            }
        } catch (e: TimeoutException) {
            logger.warn("Element did not contain expected text '$text' after $timeInSeconds seconds: ${e.message}")
            null
        } catch (e: NoSuchElementException) {
            logger.warn("Element not found: ${e.message}")
            null
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Waits up to [timeInSeconds] for [element] to contain [text], then clicks it.
     *
     * Temporarily sets implicit‐wait to zero so we can poll quickly, and restores it
     * afterward.
     *
     * @throws TimeoutException if the text never appears within the timeout
     * @throws NoSuchElementException if the element cannot be found at all
     */
    fun waitForElementToContainTextThenClick(
        element: WebElement,
        text: String,
        timeInSeconds: Int = DEFAULT_WAIT_TIMEOUT
    ) {
        setImplicitWait(Duration.ZERO)
        try {
            val driver = ThreadLocalDriverManager.getDriver()
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until(ExpectedConditions.textToBePresentInElement(element, text))
            element.click()
        } catch (e: TimeoutException) {
            logger.warn("Timed out waiting for text '$text' on element: ${e.message}")
            throw e
        } catch (e: NoSuchElementException) {
            logger.warn("Element not found at all: ${e.message}")
            throw e
        } finally {
            resetImplicitWait()
        }
    }

    fun tapOnCyclingElementWithText(text: String, timeoutSeconds: Long) {
        val driver = ThreadLocalDriverManager.getDriver()
        val endTime = System.currentTimeMillis() + timeoutSeconds * MILLIS_IN_SECOND

        while (System.currentTimeMillis() < endTime) {
            try {
                val elements = driver.findElements(
                    By.xpath("//*[contains(@${if (isAndroid) "text" else "label"}, '$text')]")
                )
                val target = elements.firstOrNull { el ->
                    try {
                        el.isDisplayed
                    } catch (_: NoSuchElementException) {
                        false
                    } catch (_: StaleElementReferenceException) {
                        false
                    }
                }
                if (target != null) {
                    target.click()
                    return
                }
            } catch (e: NoSuchElementException) {
                logger.warn("Element collection not present yet: ${e.message}")
            } catch (e: StaleElementReferenceException) {
                logger.warn("Stale reference while searching element: ${e.message}")
            }
        }

        throw TimeoutException("Could not tap on element with text '$text' within $timeoutSeconds seconds.")
    }

    /**
     * Waits for an alert to be visible.
     */
    fun waitUntilAlertIsVisible(timeInSeconds: Int): Alert? {
        setImplicitWait(Duration.ZERO)
        return try {
            val driver = ThreadLocalDriverManager.getDriver()
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until(ExpectedConditions.alertIsPresent())
        } catch (e: TimeoutException) {
            logger.warn("No alert found within $timeInSeconds seconds: ${e.message}")
            null
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Waits until the provided list of elements is not empty, then returns it.
     * Temporarily sets the implicit-wait to zero to poll quickly, then restores it to the default timeout.
     *
     * @param elements      the list of WebElements to check if not empty
     * @param timeInSeconds the maximum time in seconds to wait for the list to become non-empty
     * @return the original list once it’s non-empty, or an empty list if the wait timed out
     */
    fun waitForListOfElementsIsNotEmpty(elements: List<WebElement>?, timeInSeconds: Int = 10): List<WebElement> {
        val driver = ThreadLocalDriverManager.getDriver()
        setImplicitWait(Duration.ZERO)
        try {
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until { _ ->
                elements?.isNotEmpty()
            }
        } catch (e: TimeoutException) {
            logger.warn("Elements did not all become visible within $timeInSeconds seconds: ${e.message}")
        } finally {
            resetImplicitWait()
        }
        return elements.orEmpty()
    }

    /**
     * Check if the provided list of elements is not empty.
     * Temporarily sets the implicit-wait to zero to poll quickly, then restores it to the default timeout.
     *
     * @param elements      the list of WebElements to check if not empty
     * @param timeInSeconds the maximum time in seconds to wait for the list to become non-empty
     * @return true if the list became non-empty within the timeout, false otherwise
     */
    fun isListOfElementsIsNotEmpty(elements: List<WebElement>?, timeInSeconds: Int = 10): Boolean {
        val driver = ThreadLocalDriverManager.getDriver()
        setImplicitWait(Duration.ZERO)
        return try {
            createFluentWait(
                driver,
                Duration.ofSeconds(timeInSeconds.toLong()),
                Duration.ofSeconds(1)
            ).until {
                elements?.isNotEmpty() == true
            }
            true
        } catch (e: TimeoutException) {
            logger.warn("Elements did not become non-empty within $timeInSeconds seconds: ${e.message}")
            false
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Checks if the video control element becomes visible within the timeout period.
     * Avoids tapping screen if the element is already displayed.
     * Taps in the middle of left side screen to get video control buttons displayed.
     *
     * @param element The WebElement representing the video control.
     * @param timeoutSeconds The maximum time to wait for the element to become visible.
     * @return true if the element is visible within the timeout, false otherwise.
     */
    fun isVideoControlElementVisible(element: WebElement, timeoutSeconds: Long = 30): Boolean {
        setImplicitWait(Duration.ZERO)
        val driver = ThreadLocalDriverManager.getDriver()
        return try {
            createFluentWait(
                driver,
                Duration.ofSeconds(timeoutSeconds),
                Duration.ofSeconds(1)
            ).until {
                MobileScreenUtils.doubleTapInTheMiddleOfOfScreen()
                element.isDisplayed
            }
        } catch (e: TimeoutException) {
            logger.warn("Element did not become visible within $timeoutSeconds seconds: ${e.message}")
            false
        } finally {
            resetImplicitWait()
        }
    }
}
