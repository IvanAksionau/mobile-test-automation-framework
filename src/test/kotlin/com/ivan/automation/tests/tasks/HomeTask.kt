package com.ivan.automation.tests.tasks

import com.ivan.automation.core.mobile.api.services.EntertainmentService
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENT_RESPONSE
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_ID
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASE_DATA
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.WaitActions
import com.ivan.automation.tests.screens.HomeScreen
import io.qameta.allure.Step

class HomeTask : HomeScreen() {

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
}
