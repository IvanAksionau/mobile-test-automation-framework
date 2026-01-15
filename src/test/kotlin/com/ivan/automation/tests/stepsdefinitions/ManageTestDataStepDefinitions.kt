package com.ivan.automation.tests.stepsdefinitions

import com.ivan.automation.core.mobile.api.models.enums.ContentType
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENT_RESPONSE
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASE_DATA
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.tests.tasks.ManageTestDataTask
import io.cucumber.java.en.When
import org.testng.SkipException

class ManageTestDataStepDefinitions {

    private val manageTestDataTask = ManageTestDataTask()

    @When(
        "there is a showcase into the top banner carousel on a home screen with the CTA button of " +
                "{string} content type"
    )
    fun addShowcaseToTheTopBannerContainer(callToActionContentType: String) {
        manageTestDataTask.addShowcaseToTheTopBannerContainer(callToActionContentType)
    }

    @When("there is a {string} event showcase into the top banner carousel on a home screen")
    fun addEventShowcaseToTheTopBannerContainer(callToActionContentType: String) {
        manageTestDataTask.addEventShowcaseToTheTopBannerContainer(callToActionContentType)
    }

    @When("there is a content with video of 'TRAILER' type")
    fun findContentWithTrailerVideo() {
        val content = manageTestDataTask.findContentWithTrailerVideo()
            ?: throw SkipException("There is no playable video found of '${ContentType.TRAILER.name}' type")

        TestContextManager.getTestContext().set(CONTENT_RESPONSE, content)
    }

    @When("there is a showcase with the section title {string} on the {string} screen")
    fun findHomeScreenShowcaseBySectionTitle(title: String, tabName: String) {
        val showcase = manageTestDataTask.findScreenShowcaseBySectionTitle(title, tabName)
        TestContextManager.getTestContext().set(SHOWCASE_DATA, showcase)
    }

    @When("there is a showcase with the section details title {string} and {string} redirectType on the home screen")
    fun findHomeScreenBannerShowcaseByDetailsCtaLabel(ctaLabel: String, redirectType: String) {
        val bannerShowcase = manageTestDataTask.findHomeScreenBannerShowcaseByDetailsCtaLabel(ctaLabel, redirectType)
        TestContextManager.getTestContext().set(SHOWCASE_DATA, bannerShowcase)
    }
}
