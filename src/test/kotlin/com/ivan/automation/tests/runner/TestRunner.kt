package com.ivan.automation.tests.runner

import com.ivan.automation.core.mobile.listener.TestListener
import io.cucumber.testng.AbstractTestNGCucumberTests
import io.cucumber.testng.CucumberOptions
import org.testng.annotations.Listeners

@CucumberOptions(
    features = ["src/test/resources/features"],
    glue = ["com.ivan.automation"],
    snippets = CucumberOptions.SnippetType.CAMELCASE,
//    tags = "@run",
    plugin = ["pretty", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"]
)
@Listeners(
    TestListener::class
)
class TestRunner : AbstractTestNGCucumberTests() {
    // enable for remote execution setup
    //    @DataProvider(parallel = true)
    //    override fun scenarios(): Array<Array<Any>> {
    //        return super.scenarios()
    //    }
}
