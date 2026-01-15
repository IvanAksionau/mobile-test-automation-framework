package com.ivan.automation.tests.stepsdefinitions

import com.ivan.automation.tests.questions.HomeQuestions
import com.ivan.automation.tests.tasks.HomeTask
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class HomeStepDefinitions {

    private val homeQuestions = HomeQuestions()
    private val homeTask = HomeTask()

    @Then("the user sees top showcase banner display expected showcases")
    fun checkTopShowcaseBannerDisplayExpectedShowcases() {
        homeQuestions.checkTopShowcaseBannerDisplayExpectedShowcases()
    }

    @When("the user taps on the showcase into the top banner carousel on a home screen")
    fun tapTheShowcaseInTheHomeScreenTopBanner() {
        homeTask.selectTheShowcaseInTheHomeScreenTopBanner()
    }
}
