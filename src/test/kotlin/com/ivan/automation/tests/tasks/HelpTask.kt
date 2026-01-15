package com.ivan.automation.tests.tasks

import io.qameta.allure.Step

class HelpTask : HelpScreen() {

    @Step("tap < button")
    fun tapBackButton() {
        backBtn.click()
    }
}
