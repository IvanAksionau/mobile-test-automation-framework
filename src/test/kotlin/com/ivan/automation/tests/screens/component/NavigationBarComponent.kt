package com.ivan.automation.tests.screens.component

import com.ivan.automation.core.mobile.driver.manager.ThreadLocalDriverManager
import com.ivan.automation.core.mobile.screen.MobileBaseScreen.Companion.isAndroid
import com.ivan.automation.core.mobile.utils.CustomException
import com.ivan.automation.core.mobile.utils.WaitActions
import io.appium.java_client.pagefactory.AndroidFindBy
import io.appium.java_client.pagefactory.AppiumFieldDecorator
import io.appium.java_client.pagefactory.Widget
import io.appium.java_client.pagefactory.iOSXCUITFindBy
import org.openqa.selenium.NoSuchElementException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.PageFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@iOSXCUITFindBy(iOSNsPredicate = "type == 'XCUIElementTypeApplication'")
@AndroidFindBy(id = "android:id/content")
open class NavigationBarComponent(element: WebElement) : Widget(element) {

    private val log: Logger by lazy { LoggerFactory.getLogger(this::class.java) }

    init {
        val driver = ThreadLocalDriverManager.getDriver()
            ?: throw IllegalStateException("Driver not initialized")
        PageFactory.initElements(
            AppiumFieldDecorator(driver), this
        )
    }

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Home']/..")
    @iOSXCUITFindBy(accessibility = "home")
    lateinit var navigationBarHomeBtn: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Home']/../..")
    @iOSXCUITFindBy(accessibility = "Tab Bar")
    lateinit var navigationBar: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Explore']/..")
    @iOSXCUITFindBy(xpath = "//*[@name='explore' or @name='esplora']")
    lateinit var navigationBarExploreBtn: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Live']/..")
    @iOSXCUITFindBy(accessibility = "live")
    lateinit var navigationBarLiveBtn: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Archive']/..")
    @iOSXCUITFindBy(accessibility = "archive")
    lateinit var navigationBarArchiveBtn: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@text='Other']/..")
    @iOSXCUITFindBy(accessibility = "myAccount")
    lateinit var navigationBarSettingsBtn: WebElement

    fun areNavigationBarItemsDisplayed(): Boolean {
        return WaitActions.isTheElementVisible(navigationBarHomeBtn) && navigationBarExploreBtn.isDisplayed
                && navigationBarLiveBtn.isDisplayed
                && navigationBarArchiveBtn.isDisplayed && navigationBarSettingsBtn.isDisplayed
    }

    fun isNavigationBarNotDisplayed(): Boolean {
        return WaitActions.isTheElementInvisible(navigationBar)
    }

    fun isSpecificNavigationBarTabIsSelected(tabName: String): Boolean {
        return try {
            val tab = when (tabName) {
                "Home" -> navigationBarHomeBtn
                "Explore" -> navigationBarExploreBtn
                "Live" -> navigationBarLiveBtn
                "Archive" -> navigationBarArchiveBtn
                "Settings" -> navigationBarSettingsBtn
                else -> throw CustomException("'$tabName' is not a valid tab name.")
            }

            val (attribute, expectedValue) = if (isAndroid) {
                "selected" to "true"
            } else {
                "value" to "1"
            }

            tab.getDomAttribute(attribute)?.equals(expectedValue) ?: false

        } catch (e: NoSuchElementException) {
            log.warn(e.message)
            false
        }
    }

    fun tapSpecificNavigationBarTab(tabName: String) {
        val tab = when (tabName) {
            "Home" -> navigationBarHomeBtn
            "Explore" -> navigationBarExploreBtn
            "Live" -> navigationBarLiveBtn
            "Archive" -> navigationBarArchiveBtn
            "Settings" -> navigationBarSettingsBtn
            else -> throw CustomException("'$tabName' is not a valid tab name.")
        }
        tab.click()
    }
}
