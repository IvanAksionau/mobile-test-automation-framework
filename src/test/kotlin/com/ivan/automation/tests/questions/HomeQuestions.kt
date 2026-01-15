package com.ivan.automation.tests.questions

import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponse
import com.ivan.automation.core.mobile.api.services.EntertainmentService
import com.ivan.automation.core.mobile.assertions.SoftAssertManager.softAssert
import com.ivan.automation.core.mobile.utils.CustomException
import com.ivan.automation.core.mobile.utils.MobileScreenUtils
import com.ivan.automation.core.mobile.utils.TestContextConstants.CLIENT_RESOLUTION_DATA
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.WaitActions
import com.ivan.automation.tests.screens.HomeScreen
import io.qameta.allure.Step
import org.testng.Assert
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
                    validateEditorialSection(showcase)
                    validatedSections += showcase.title.trim()
                }

                ShowcaseDataType.BANNER.name -> {
                    validateBannerSection(showcase)
                    showcase.details?.title?.trim()?.let { validatedSections += it }
                }

                else -> {
                    throw SkipException("There is no implementation for ${showcase.showcaseType}")
                }
            }
        }
        verifyDisplayedSections(validatedSections)
    }

    @Step("validate editorial section for showcase '{showcase.title}'")
    private fun validateEditorialSection(showcase: ShowCaseResponse) {
        val element = MobileScreenUtils.scrollDownToTheElementWithText(
            showCaseSectionsListComponent.sectionTitlesList,
            showcase.title
        )
        MobileScreenUtils.dragAndDropToVerticalMiddle(element)

        val parent = showCaseSectionsListComponent.getSectionParentElement(showcase.title)
        val actualTitles = showCaseSectionsListComponent
            .getSectionContentTitlesList(parent)
            .map { it.text.trim() }
        val expectedTitles = getShowcaseParentsTitles(showcase)

        Assert.assertTrue(expectedTitles.isNotEmpty(), "List of expected titles is not empty")
        Assert.assertTrue(actualTitles.isNotEmpty(), "List of actual titles is not empty")

        checkIfChildTitlesAreExpected(expectedTitles, actualTitles)
    }

    @Step("check if items from '{actualTitles}' are present in {expectedTitles}")
    private fun checkIfChildTitlesAreExpected(expectedTitles: List<String>, actualTitles: List<String>) {
        for (title in actualTitles) {
            softAssert.assertTrue(
                expectedTitles.any { expectedTitle -> expectedTitle.contains(title) },
                "Expected '${title}' is present in any of $expectedTitles"
            )
        }
    }

    @Step("validate banner section for showcase")
    private fun validateBannerSection(showcase: ShowCaseResponse) {
        val ctaLabel = showcase.details?.callToAction?.label ?: return
        MobileScreenUtils.scrollDownToTheElementWithText(
            showCaseSectionsListComponent.showcaseCallToActionsBtn,
            ctaLabel
        )

        val parent = showCaseSectionsListComponent.getSectionParentElement(showcase.details.title)
        val title = showCaseSectionsListComponent.getBannerSectionContentTitle(parent).text.trim()
        val description = showCaseSectionsListComponent.getBannerSectionContentDescription(parent).text.trim()
        val buttonText = showCaseSectionsListComponent.getBannerSectionContentCTAButton(parent).text.trim()

        softAssert.apply {
            assertEquals(title, showcase.details.title, "Banner title should match expected")
            assertEquals(description, showcase.details.description, "Banner description should match expected")
            assertEquals(buttonText, ctaLabel, "Banner CTA text should match expected")
        }
    }

    @Step("validate that displayed section titles correspond to the expected list")
    private fun verifyDisplayedSections(expected: List<String>) {
        val displayed = showCaseSectionsListComponent.sectionTitlesList
            ?.map { it.text.trim() }
            ?: emptyList()

        softAssert.assertTrue(
            displayed.all { it in expected },
            "Displayed sections $displayed should all be within expected $expected"
        )
    }

    @Step("fetch parent titles for showcase '{showcase.id}', applying template match if needed")
    private fun getShowcaseParentsTitles(showcase: ShowCaseResponse): List<String> {
        val children = EntertainmentService.getShowcaseChildList(showcase.id)

        return children.map { child ->
            if (!isAndroid) {
                val match = child.titleTemplateValues?.match
                if (child.broadcastType == "LIVE" && match != null && match.isNotEmpty()) {

                    val parenTagsIdList = showcase.semanticTags?.map { it.id } ?: emptyList()
                    val childTagsIdList = child.semanticTags?.map { it.id } ?: emptyList()

                    if (!childTagsIdList.containsAll(parenTagsIdList)) {
                        match.trim()
                    } else {
                        child.title.trim()
                    }

                } else {
                    child.title.trim()
                }
            } else {
                child.template?.find { it.kind.equals("title") }?.value ?: child.title.trim()
            }
        }
    }
}
