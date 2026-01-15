package com.ivan.automation.tests.tasks

import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponse
import com.ivan.automation.core.mobile.api.services.EntertainmentService
import com.ivan.automation.core.mobile.utils.MobileScreenUtils
import com.ivan.automation.core.mobile.utils.TestContextConstants.CLIENT_RESOLUTION_DATA
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENT_RESPONSE
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_ID
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASE_DATA
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.WaitActions
import com.ivan.automation.core.mobile.utils.enums.ScrollDirection
import com.ivan.automation.tests.screens.HomeScreen
import io.qameta.allure.Step
import org.testng.SkipException

class HomeTask : HomeScreen() {

    @Step("close airship popup if displayed")
    fun closeAirShipPopupIfDisplayed() {
        if (WaitActions.isTheElementVisible(closeAirShipPopupBtn, 10)) {
            closeAirShipPopupBtn.click()
        }
    }

    @Step("select the expected showcase in top banner carousel by title")
    fun selectTheShowcaseInTheHomeScreenTopBanner() {
        val showcaseId = TestContextManager.getTestContext().get<String>(SHOWCASES_ID)
        val showcaseData = EntertainmentService.getShowcaseById(showcaseId)
        TestContextManager.getTestContext().set(SHOWCASE_DATA, showcaseData)

        showcaseData.let {
            WaitActions.tapOnCyclingElementWithText(it!!.title, 60)
        }

        val redirectId = showcaseData?.callToAction?.redirectId ?: showcaseData?.catalogRedirectId
        requireNotNull(redirectId) {
            "Neither callToAction.redirectId nor catalogRedirectId was provided on the showcase data"
        }
        val contentData = EntertainmentService.getContentById(redirectId)
        TestContextManager.getTestContext().set(CONTENT_RESPONSE, contentData)
    }

    @Step("tap 'See All' button on the home screen showcase with the title '{title}'")
    fun tapShowcaseSectionSeeAllButtonBySectionTitle(title: String) {
        val element = MobileScreenUtils.scrollDownToTheElementWithText(
            showCaseSectionsListComponent.sectionTitlesList,
            title
        )
        MobileScreenUtils.dragAndDropToVerticalMiddle(element)

        val parent = showCaseSectionsListComponent.getSectionParentElement(title)
        MobileScreenUtils.scrollToTheElement(
            showCaseSectionsListComponent.getSectionSeeAllButton(parent),
            ScrollDirection.DOWN
        ).click()
    }

    @Step("tap CTA button of banner showcase with '{bannerTitle}' banner details title")
    fun tapBannerShowcaseCtaButtonByLabel(bannerTitle: String) {
        val element = MobileScreenUtils.scrollDownToTheElementWithText(
            showCaseSectionsListComponent.bannerTitle,
            bannerTitle
        )
        MobileScreenUtils.dragAndDropToVerticalMiddle(element)

        val parentElem = showCaseSectionsListComponent.getSectionParentElement(bannerTitle)
        MobileScreenUtils.scrollToTheElement(
            showCaseSectionsListComponent.getBannerSectionContentCTAButton(parentElem),
            ScrollDirection.DOWN
        ).click()
    }

    @Step("scroll down the list of showcase sections to find the showcase of '{contentType}' type")
    fun scrollDownSectionsListTillSpecificContentType(contentType: String) {
        val resolution = TestContextManager.getTestContext().get<ClientResolutionResponse>(CLIENT_RESOLUTION_DATA)
        val showcaseSectionsList = EntertainmentService.getShowcaseChildListFiltered(resolution.homeShowcaseId)

        // Find the first section that contains a MOVIE child and capture that child
        val (showcaseSection, movieChild) = showcaseSectionsList.firstNotNullOfOrNull { section ->
            val movieChild = EntertainmentService.getShowcaseChildList(section.id)
                .take(3)
                .firstOrNull { it.type == contentType }
            movieChild?.let { section to it }
        }
            ?: throw SkipException(
                "There is no showcase section containing child showcase of '${contentType}' type"
            )

        // Scroll down to get the expected showcase section displayed
        MobileScreenUtils.dragAndDropToVerticalMiddle(
            MobileScreenUtils.scrollDownToTheElementWithText(
                showCaseSectionsListComponent.sectionTitlesList,
                showcaseSection.title, 15
            )
        )

        TestContextManager.getTestContext().set(SHOWCASE_DATA, movieChild)
    }


    @Step("select the specific showcase by title")
    fun tapOnTheShowCaseOfSpecificContentType() {
        val showcaseData = TestContextManager.getTestContext().get<ShowCaseResponse>(SHOWCASE_DATA)
        // Select the expected showcase from the section child list
        showCaseSectionsListComponent.getSectionParentElement(showcaseData.title).click()

        val redirectId = showcaseData.catalogRedirectId
        requireNotNull(redirectId) {
            "There is no catalogRedirectId to get the content data of the ${showcaseData.title} showcase"
        }

        // load the content showcase view data
        val contentData = EntertainmentService.getContentById(redirectId)
        TestContextManager.getTestContext().set(CONTENT_RESPONSE, contentData)
    }
}
