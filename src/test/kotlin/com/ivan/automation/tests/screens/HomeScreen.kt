package com.ivan.automation.tests.screens

import com.ivan.automation.core.mobile.screen.MobileBaseScreen
import com.ivan.automation.tests.screens.component.NavigationBarComponent
import io.appium.java_client.pagefactory.AndroidFindBy
import io.appium.java_client.pagefactory.iOSXCUITFindBy
import org.openqa.selenium.WebElement

open class HomeScreen : MobileBaseScreen() {

    protected lateinit var navigationBarComponent: NavigationBarComponent

    @AndroidFindBy(
        xpath = "//com.urbanairship.android.layout.widget.WeightlessLinearLayout//android.widget.ImageButton"
    )
    @iOSXCUITFindBy(xpath = "//*[@name='xmark' or @name='Close']")
    protected lateinit var closeAirShipPopupBtn: WebElement

    @AndroidFindBy(xpath = "//android.view.View[@resource-id='top_carousel_label']//android.widget.TextView")
    @iOSXCUITFindBy(
        xpath = "//XCUIElementTypeOther[@name='header_slider_view']//XCUIElementTypeStaticText[@name='label']"
    )
    protected lateinit var topBannerLabel: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@resource-id='top_carousel_title']")
    @iOSXCUITFindBy(
        xpath = "//XCUIElementTypeOther[@name='header_slider_view']//XCUIElementTypeStaticText[@name='title']"
    )
    protected lateinit var topBannerTitleLbl: WebElement

    @AndroidFindBy(xpath = "//android.widget.TextView[@resource-id='top_carousel_description']")
    @iOSXCUITFindBy(
        xpath = "//XCUIElementTypeOther[@name='header_slider_view']//XCUIElementTypeStaticText[@name='description']"
    )
    protected lateinit var topBannerDescriptionLbl: WebElement
}
