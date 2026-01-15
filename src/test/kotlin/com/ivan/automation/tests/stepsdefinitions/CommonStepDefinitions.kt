package com.ivan.automation.tests.stepsdefinitions

import com.ivan.automation.tests.questions.HomeQuestions
import io.cucumber.java.en.Then

class CommonStepDefinitions {

    private val homeQuestions = HomeQuestions()

    @Then("the user sees each showcase section display expected content list in {string} navigation tab")
    fun checkAllContentSectionsDisplayExpectedContent(tabName: String) {
        homeQuestions.checkAllContentSectionsDisplayExpectedContent(tabName)
    }
}
