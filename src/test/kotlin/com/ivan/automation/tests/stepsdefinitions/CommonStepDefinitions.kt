package com.ivan.automation.tests.stepsdefinitions

import com.ivan.automation.tests.questions.HomeQuestions
import com.ivan.automation.tests.tasks.HomeTask
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class CommonStepDefinitions {

    private val navigationBarQuestions = NavigationBarQuestions()
    private val navigationBarTask = NavigationBarTask()
    private val homeQuestions = HomeQuestions()
    private val homeTask = HomeTask()

    @Then("the user sees navigation tab bar contains expected buttons")
    fun checkNavigationBarIsDisplayed() {
        navigationBarQuestions.checkNavigationBarItemsAreDisplayed()
    }

    @Then("the user sees navigation tab is not displayed")
    fun checkNavigationBarIsNotDisplayed() {
        navigationBarQuestions.checkNavigationBarIsNotDisplayed()
    }

    @And("the user sees navigation bar tab is selected as {string}")
    fun checkThatSpecificNavigationBarTabIsSelected(tabName: String) {
        navigationBarQuestions.checkThatSpecificNavigationBarTabIsSelected(tabName)
    }

    @And("user taps {string} tab in navigation bar")
    fun tapSpecificNavigationBarTab(tabName: String) {
        navigationBarTask.tapSpecificNavigationBarTab(tabName)
    }

    @Then("the user sees each showcase section display expected content list in {string} navigation tab")
    fun checkAllContentSectionsDisplayExpectedContent(tabName: String) {
        homeQuestions.checkAllContentSectionsDisplayExpectedContent(tabName)
    }

    @When("user taps 'See All' button of the screen showcase section with title {string}")
    fun tapShowcaseSectionSeeAllButtonBySectionTitle(title: String) {
        homeTask.tapShowcaseSectionSeeAllButtonBySectionTitle(title)
    }
}
