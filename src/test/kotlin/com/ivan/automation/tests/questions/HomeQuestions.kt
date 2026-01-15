package com.ivan.automation.tests.questions

import com.ivan.automation.core.mobile.api.models.enums.ShowcaseDataType
import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.services.EntertainmentService
import com.ivan.automation.core.mobile.assertions.SoftAssertManager.softAssert
import com.ivan.automation.core.mobile.utils.CustomException
import com.ivan.automation.core.mobile.utils.TestContextConstants.CLIENT_RESOLUTION_DATA
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.WaitActions
import com.ivan.automation.tests.screens.HomeScreen
import io.qameta.allure.Step
import org.testng.SkipException

class HomeQuestions : HomeScreen() {

    private val clientResolution: ClientResolutionResponse
        get() = TestContextManager.getTestContext()
            .get(CLIENT_RESOLUTION_DATA)

    @Step("validate top showcase banner shows the expected label, title and description")
    fun checkTopShowcaseBannerDisplayExpectedShowcases() {
        val children = EntertainmentService
            .getShowcaseChildList(clientResolution.bannerShowcaseId, true)

        val expectedLabels =
            children.filter { it.callToAction?.label != null }.map { it.callToAction?.label!!.uppercase() }.toSet()
        val expectedTitles = children.map { it.title.trim() }.toSet()
        val expectedDescriptions = children.mapNotNull { it.description?.trim() }.toSet()

        val actualLabel = WaitActions.getElementText(topBannerLabel)
        val actualTitle = WaitActions.getElementText(topBannerTitleLbl)
        val actualDescription = WaitActions.getElementText(topBannerDescriptionLbl)

        softAssert.apply {
            assertTrue(
                expectedLabels.contains(actualLabel),
                "Top banner label '$actualLabel' should be one of $expectedLabels"
            )
            assertTrue(
                expectedTitles.contains(actualTitle),
                "Top banner title '$actualTitle' should be one of $expectedTitles"
            )
            assertTrue(
                expectedDescriptions.contains(actualDescription),
                "Top banner description '$actualDescription' should be one of $expectedDescriptions"
            )
        }
    }

    @Step("validate '{tabName}' screen showcase sections display expected content")
    fun checkAllContentSectionsDisplayExpectedContent(tabName: String) {
        val showcaseId = when (tabName) {
            "Home" -> clientResolution.homeShowcaseId
            "Explore" -> clientResolution.mobileShowcaseExplore
            else -> throw CustomException("'$tabName' is not a valid tab name.")
        }

        val showcaseList = EntertainmentService.getShowcaseChildListFiltered(showcaseId)
        val validatedSections = mutableListOf<String>()

        showcaseList.forEach { showcase ->
            when (showcase.showcaseType) {
                ShowcaseDataType.EDITORIAL.name -> {
//                    validateEditorialSection(showcase)
                    validatedSections += showcase.title.trim()
                }

                ShowcaseDataType.BANNER.name -> {
//                    validateBannerSection(showcase)
                    showcase.details?.title?.trim()?.let { validatedSections += it }
                }

                else -> {
                    throw SkipException("There is no implementation for ${showcase.showcaseType}")
                }
            }
        }

    }
}
