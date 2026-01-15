package com.ivan.automation.core.mobile.utils

import com.ivan.automation.core.mobile.driver.AppiumProperties.IMPLICIT_WAIT_TIMEOUT
import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.utils.enums.DragAndDropDirection
import com.ivan.automation.core.mobile.utils.enums.ScrollDirection
import io.appium.java_client.AppiumDriver
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.nativekey.AndroidKey
import io.appium.java_client.android.nativekey.KeyEvent
import io.appium.java_client.ios.IOSDriver
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Pause
import org.openqa.selenium.interactions.PointerInput
import org.openqa.selenium.interactions.Sequence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

@Suppress("TooManyFunctions")
object MobileScreenUtils {

    private val logger: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    private const val SCROLL_START_RATIO = 0.6 // Start scroll at 60% down
    private const val SCROLL_END_RATIO = 0.3   // Move scroll to 30% up
    private const val SCROLL_DURATION_MS = 800L
    private const val SWIPE_DURATION_MS = 150L
    private const val HORIZONTAL_DRAG_DURATION_MS = 500L
    private const val SCROLL_UP_END_RATIO = 0.8
    private const val DOUBLE_TAP_DELAY_MS = 200L
    private const val RADIUS_VALUE = 5
    private const val SLEEP_DURATION = 1_000L
    private const val SWIPE_START_RATIO = 0.8
    private const val SWIPE_END_RATIO = 0.2
    private const val SWIPE_Y_RATIO = 0.5
    private const val HORIZONTAL_SWIPE_DURATION_MS = 500L
    private const val SWIPE_DELAY_BETWEEN_MS = 1_000L

    private fun getWindowSize() = ThreadLocalDriverManager.getDriver().manage().window().size

    private fun setImplicitWait(duration: Duration) {
        ThreadLocalDriverManager.getDriver().manage().timeouts().implicitlyWait(duration)
    }

    private fun resetImplicitWait() {
        setImplicitWait(Duration.ofSeconds(IMPLICIT_WAIT_TIMEOUT.toLong()))
    }

    /**
     * Scrolls the screen until the given WebElement is displayed.
     *
     * @param element The WebElement to inspect.
     * @param maxScrolls The maximum number of scroll attempts.
     * @param direction the direction to perform the scroll.
     *
     * @return The WebElement if found.
     * @throws NoSuchElementException if the element was not found.
     */
    fun scrollToTheElement(
        element: WebElement?,
        direction: ScrollDirection,
        maxScrolls: Int = 10
    ): WebElement {
        var exception: NoSuchElementException? = null
        setImplicitWait(Duration.ofSeconds(1))
        try {
            repeat(maxScrolls) {
                try {
                    if (element != null && element.isDisplayed) {
                        logger.info("Element found!")
                        return element
                    }
                } catch (e: NoSuchElementException) {
                    logger.warn("Element not found in current view: ${e.message}", e)
                    exception = e
                }
                performScroll(direction)
            }
            throw NoSuchElementException("Element was not found after $maxScrolls scrolls.", exception)
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Scrolls down the screen until the given WebElement contains the specified text.
     *
     * @param element The WebElement to inspect.
     * @param text The text to search for within the element.
     * @param maxScrolls The maximum number of scroll attempts.
     * @return The WebElement if found containing the text.
     * @throws CustomException if the element does not contain the text after the specified scrolls.
     */
    fun scrollDownToTheElementWithText(element: WebElement?, text: String, maxScrolls: Int = 10): WebElement {
        setImplicitWait(Duration.ofSeconds(1))
        try {
            repeat(maxScrolls) {
                try {
                    if (element != null && element.isDisplayed && element.text.contains(text, ignoreCase = true)) {
                        logger.info("Element with text '$text' found!")
                        return element
                    }
                } catch (e: NoSuchElementException) {
                    logger.warn("Element not found in current view: ${e.message}", e)
                }
                performScroll()
            }
            throw CustomException("Element does not contain text '$text' after $maxScrolls scrolls.")
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Scrolls down the screen until one of the elements in the list contains the specified text.
     *
     * @param elements A list of WebElements to inspect.
     * @param text The text to search for within the elements.
     * @param maxScrolls The maximum number of scroll attempts.
     * @return The matching WebElement if found.
     * @throws CustomException if none of the elements contain the text after the specified scrolls.
     */
    fun scrollDownToTheElementWithText(elements: List<WebElement>?, text: String, maxScrolls: Int = 10): WebElement {
        setImplicitWait(Duration.ofSeconds(1))
        try {
            repeat(maxScrolls) {
                val found = elements.orEmpty().firstOrNull { el ->
                    try {
                        if (!el.isDisplayed) return@firstOrNull false
                        el.text.contains(text, ignoreCase = true)
                    } catch (e: NoSuchElementException) {
                        logger.warn("Element went stale or wasn't present, continuing scroll", e)
                        false
                    } catch (e: StaleElementReferenceException) {
                        logger.warn("Stale element encountered, continuing scroll", e)
                        false
                    }
                }
                if (found != null) {
                    logger.info("Found element containing text '$text'")
                    return found
                }
                performScroll()
            }
            throw CustomException("Element containing '$text' not found after $maxScrolls scrolls.")
        } finally {
            resetImplicitWait()
        }
    }

    /**
     * Drags and drops the specified element to the vertical midpoint of the mobile screen.
     *
     * @param element the WebElement to drag
     * @param thresholdPx how close (in pixels) the element's center must be to the screen midpoint
     *                    to skip dragging altogether (default is 10px)
     */
    fun dragAndDropToVerticalMiddle(element: WebElement, thresholdPx: Int = 10) {
        val driver = ThreadLocalDriverManager.getDriver()
        val screenSize = driver.manage().window().size
        val screenMidY = screenSize.height / 2

        // calculate element center
        val elementCenterX = element.location.x + element.size.width / 2
        val elementCenterY = element.location.y + element.size.height / 2

        // if already within threshold of middle, do nothing
        if (kotlin.math.abs(elementCenterY - screenMidY) <= thresholdPx) {
            // already "in the middle" — skip the drag/tap entirely
            return
        }

        // otherwise perform the drag
        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val dragSequence = Sequence(finger, 1)

        // 1) move to element center
        dragSequence.addAction(
            finger.createPointerMove(
                Duration.ZERO,
                PointerInput.Origin.viewport(),
                elementCenterX, elementCenterY
            )
        )
        // 2) press down
        dragSequence.addAction(
            finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg())
        )
        // 3) small "jiggle" + pause to ensure a drag
        // By moving just beyond that slop (e.g. by 5px), you guarantee the gesture is classified
        // as a drag rather than a tap—without introducing any visible jump or jank.
        dragSequence.addAction(
            finger.createPointerMove(
                Duration.ofMillis(SWIPE_DURATION_MS),
                PointerInput.Origin.viewport(),
                elementCenterX, elementCenterY + RADIUS_VALUE
            )
        )
        // 4) main slow drag to middle
        dragSequence.addAction(
            finger.createPointerMove(
                Duration.ofMillis(SCROLL_DURATION_MS),
                PointerInput.Origin.viewport(),
                elementCenterX, screenMidY
            )
        )
        // 5) scroll up
        dragSequence.addAction(
            finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg())
        )
        driver.perform(listOf(dragSequence))
    }

    /**
     * Drags a given [element] horizontally across the screen using touch actions.
     *
     * The drag direction is determined by the [direction] parameter:
     * - [DragAndDropDirection.LEFT]  → starts at the horizontal center of the
     *   element and drags to the left edge of the screen.
     * - [DragAndDropDirection.RIGHT] → starts at the horizontal center of the
     *   element and drags to the right edge of the screen.
     *
     * The vertical position of the drag is always centered relative to the element.
     * The Appium driver is retrieved from [driverThreadLocal].
     *
     * @param element the [WebElement] to drag
     * @param direction the horizontal direction to drag the element
     * ([DragAndDropDirection.LEFT] or [DragAndDropDirection.RIGHT])
     */
    fun dragElementHorizontally(element: WebElement, direction: DragAndDropDirection) {
        val driver = ThreadLocalDriverManager.getDriver()
        val location = element.location
        val size = element.size

        // Always start from the horizontal center of the element
        val startX = location.x + size.width / 2
        val startY = location.y + size.height / 2

        // Determine endX based on direction
        val endX = when (direction) {
            DragAndDropDirection.LEFT -> 0
            DragAndDropDirection.RIGHT -> driver.manage().window().size.width - 1
        }
        val endY = startY

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val drag = Sequence(finger, 1)

        drag
            .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(
                finger.createPointerMove(
                    Duration.ofMillis(HORIZONTAL_DRAG_DURATION_MS),
                    PointerInput.Origin.viewport(),
                    endX,
                    endY
                )
            )
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        driver.perform(listOf(drag))
    }

    /**
     * Performs a W3C-compliant scroll using PointerInput (works for both Android & iOS).
     * ScrollDirection.DOWN scrolls down, ScrollDirection.UP scrolls up.
     *
     * @param direction the direction to perform the scroll
     */
    fun performScroll(direction: ScrollDirection = ScrollDirection.DOWN) {
        val driver = ThreadLocalDriverManager.getDriver()
        val size = driver.manage().window().size
        val startX = size.width / 2

        val (startY, endY) = when (direction) {
            ScrollDirection.DOWN ->
                (size.height * SCROLL_START_RATIO).toInt() to (size.height * SCROLL_END_RATIO).toInt()

            ScrollDirection.UP ->
                (size.height * SCROLL_START_RATIO).toInt() to (size.height * SCROLL_UP_END_RATIO).toInt()
        }

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val swipe = Sequence(finger, 0)

        swipe
            .addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY))
            .addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
            .addAction(
                finger.createPointerMove(
                    Duration.ofMillis(SCROLL_DURATION_MS),
                    PointerInput.Origin.viewport(),
                    startX,
                    endY
                )
            )
            .addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
        driver.perform(listOf(swipe))
    }

    /**
     * Performs a tap gesture in the center of the screen.
     */
    fun tapInTheMiddleOfOfScreen() {
        val windowSize = getWindowSize()
        val x = windowSize.width / 2
        val y = windowSize.height / 2

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val tapSequence = Sequence(finger, 0)

        tapSequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
        tapSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        tapSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        val driver = ThreadLocalDriverManager.getDriver()
        driver.perform(listOf(tapSequence))
    }

    /**
     * Performs a tap gesture in the center of the screen.
     */
    fun doubleTapInTheMiddleOfOfScreen() {
        val windowSize = getWindowSize()
        val x = windowSize.width / 2
        val y = windowSize.height / 2

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val tapSequence1 = Sequence(finger, 0)
        val tapSequence2 = Sequence(finger, 0)

        // First tap
        tapSequence2.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
        tapSequence1.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
        tapSequence1.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        tapSequence1.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        // Short pause
        tapSequence1.addAction(Pause(finger, Duration.ofMillis(DOUBLE_TAP_DELAY_MS)))
        val driver = ThreadLocalDriverManager.getDriver()
        driver.perform(listOf(tapSequence1))

        // Second tap
        tapSequence2.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        tapSequence2.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
        driver.perform(listOf(tapSequence2))
    }

    /**
     * Performs a tap gesture in the middle of the left side of the screen
     * using W3C Actions API (PointerInput and Sequence).
     */
    @SuppressWarnings("MagicNumber")
    fun tapInTheMiddleOfLeftSideOfScreen() {
        val driver = ThreadLocalDriverManager.getDriver()
        val size = driver.manage().window().size
        val x = size.width / 4            // Closer to the left edge (25% of screen width)
        val y = size.height / 2          // Vertically centered

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val tapSequence = Sequence(finger, 0)

        tapSequence.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), x, y))
        tapSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        tapSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        driver.perform(listOf(tapSequence))
    }

    fun swipeLeft(times: Int = 1) {
        val driver = ThreadLocalDriverManager.getDriver()
        val size = driver.manage().window().size
        val startX = (size.width * SWIPE_START_RATIO).toInt()
        val endX = (size.width * SWIPE_END_RATIO).toInt()
        val y = (size.height * SWIPE_Y_RATIO).toInt()

        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")

        repeat(times) {
            val swipe = Sequence(finger, 0).apply {
                addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, y))
                addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
                addAction(
                    finger.createPointerMove(
                        Duration.ofMillis(HORIZONTAL_SWIPE_DURATION_MS),
                        PointerInput.Origin.viewport(),
                        endX,
                        y
                    )
                )
                addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))
            }
            driver.perform(listOf(swipe))
            Thread.sleep(SWIPE_DELAY_BETWEEN_MS) // small delay between swipes
        }
    }

    /**
     * Performs a tap gesture in the center of the specified element.
     *
     * @param element The WebElement to tap.
     */
    fun tapAtCoordinates(element: WebElement) {
        val location = element.location
        val size = element.size
        val centerX = location.x + size.width / 2
        val centerY = location.y + size.height / 2

        val driver = ThreadLocalDriverManager.getDriver()
        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val tapSequence = Sequence(finger, 0)

        tapSequence.addAction(
            finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), centerX, centerY)
        )
        tapSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        tapSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        driver.perform(listOf(tapSequence))
    }

    /**
     * Performs a tap gesture at the end of a progress bar element.
     * This function calculates the rightmost position of the progress bar
     * and taps at that location to seek to the end.
     *
     * @param progressBarElement The progress bar WebElement to tap at the end of.
     * @param offsetFromEnd Optional offset in pixels from the right edge (default: 5px).
     *                      This prevents tapping exactly at the edge which might not register.
     */
    fun tapAtEndOfProgressBar(progressBarElement: WebElement, offsetFromEnd: Int = 5) {
        val location = progressBarElement.location
        val size = progressBarElement.size

        // Calculate the X coordinate near the end of the progress bar
        // Subtract offsetFromEnd to avoid tapping exactly at the edge
        val endX = location.x + size.width - offsetFromEnd

        // Calculate the Y coordinate at the vertical center of the progress bar
        val centerY = location.y + size.height / 2

        val driver = ThreadLocalDriverManager.getDriver()
        val finger = PointerInput(PointerInput.Kind.TOUCH, "finger")
        val tapSequence = Sequence(finger, 0)

        tapSequence.addAction(
            finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), endX, centerY)
        )
        tapSequence.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()))
        tapSequence.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()))

        driver.perform(listOf(tapSequence))

        logger.info("Tapped at end of progress bar at coordinates: ($endX, $centerY)")
    }

    /**
     * Restart the mobile app.
     */
    fun restartTheApp() {
        val driver = ThreadLocalDriverManager.getDriver() as AppiumDriver
        val appPackage = driver.capabilities
            .getCapability("appPackage")
            ?: driver.capabilities.getCapability("bundleId")
                ?.toString()

        val scriptKey = if (driver.capabilities.getCapability("appPackage") != null) {
            "appId"
        } else {
            "bundleId"
        }

        val js = driver as JavascriptExecutor
        try {
            js.executeScript("mobile: terminateApp", mapOf(scriptKey to appPackage))
        } catch (e: WebDriverException) {
            if (e.message?.contains("still running after 500ms") == true) {
                Thread.sleep(SLEEP_DURATION)
                js.executeScript("mobile: terminateApp", mapOf(scriptKey to appPackage))
            } else {
                throw e
            }
        }
        // Reactivate
        js.executeScript("mobile: activateApp", mapOf(scriptKey to appPackage))
    }

    /**
     * Simulates pressing the 'Escape' key.
     */
    fun pressEscapeKey() {
        val driver = ThreadLocalDriverManager.getDriver() as AppiumDriver
        when (driver) {
            is AndroidDriver -> {
                driver.pressKey(KeyEvent(AndroidKey.BACK))
                driver.pressKey(KeyEvent(AndroidKey.BACK))
            }

            is IOSDriver -> {
                //do nothing, because ESC is rarely meaningful on iOS.
            }

            else -> {
                throw UnsupportedOperationException("Unsupported platform for Escape key")
            }
        }
    }
}
