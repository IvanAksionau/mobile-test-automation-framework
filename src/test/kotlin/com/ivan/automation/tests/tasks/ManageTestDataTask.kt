package com.ivan.automation.tests.tasks

import com.ivan.automation.core.mobile.api.models.response.ClientResolutionResponse
import com.ivan.automation.core.mobile.api.models.response.ContentResponse
import com.ivan.automation.core.mobile.api.models.response.ContentResponseV2
import com.ivan.automation.core.mobile.api.models.response.ShowCaseResponse
import com.ivan.automation.core.mobile.api.services.EntertainmentService
import com.ivan.automation.core.mobile.api.services.GateKeeperService
import com.ivan.automation.core.mobile.api.services.MorpheusService
import com.ivan.automation.core.mobile.utils.CustomException
import com.ivan.automation.core.mobile.utils.MobileScreenUtils
import com.ivan.automation.core.mobile.utils.TestContextConstants.CLIENT_RESOLUTION_DATA
import com.ivan.automation.core.mobile.utils.TestContextConstants.CONTENT_RESPONSE
import com.ivan.automation.core.mobile.utils.TestContextConstants.DASHBOARD_ADMIN_TOKEN
import com.ivan.automation.core.mobile.utils.TestContextConstants.SHOWCASES_TITLE
import com.ivan.automation.core.mobile.utils.TestContextConstants.X_CHILI_DEVICE_ID
import com.ivan.automation.core.mobile.utils.TestContextManager
import com.ivan.automation.core.mobile.utils.TestDataManager
import com.ivan.automation.core.mobile.utils.testdata.BannerShowcasePreparator
import com.ivan.automation.core.mobile.utils.testdata.ContentSearchService
import io.qameta.allure.Step
import org.testng.SkipException

/**
 * Task class for managing test data operations.
 */
class ManageTestDataTask : OnboardingScreen() {

    private val bannerPreparator = BannerShowcasePreparator()

    // ========== Content Search Operations ==========

    /**
     * Searches for published content with a trailer video.
     *
     * @return [ContentResponse] if found, null otherwise.
     */
    @Step("search the content with related video of 'TRAILER' type")
    fun findContentWithTrailerVideo(): ContentResponse? {
        initializeDashboardToken()
        return ContentSearchService.findContentWithTrailerVideo()
    }

    /**
     * Initializes the dashboard admin token in the test context.
     */
    private fun initializeDashboardToken() {
        val token = GateKeeperService.getDashBoardAdminToken(
            GetAdminTokenRequest(
                xDeviceId = TestContextManager.getTestContext().get(X_CHILI_DEVICE_ID)
            )
        )
        TestContextManager.getTestContext().set(DASHBOARD_ADMIN_TOKEN, "Bearer $token")
    }

    // ========== Banner Showcase Operations ==========

    /**
     * Adds a showcase with content to the top banner carousel.
     *
     * @param contentType The type of content to create.
     */
    @Step("add showcase into the top banner carousel with a CTA for {contentType} content type")
    fun addShowcaseToTheTopBannerContainer(contentType: String) {
        // Create territory, showcase, and content
        val territoryId = TestDataManager.createTerritory(publish = true).id
        val showcaseId = TestDataManager.createTheShowcase(territoryId).id
        val contentData = TestDataManager.createContent(contentType)
        val content = TestDataManager.createPublishContent(contentData.id, territoryId)

        // Prepare and publish showcase
        bannerPreparator.prepareShowcaseWithContent(territoryId, showcaseId, content.id)

        // Wait for showcase to be added and content to be playable
        waitForShowcaseAndContent(ContentType.SIMPLE)

        // Restart app to reflect changes
        MobileScreenUtils.restartTheApp()
    }

    /**
     * Adds an event showcase to the top banner carousel.
     *
     * @param eventState The state of the event (PAST, CURRENT, FUTURE).
     */
    @Step("add showcase into the top banner carousel with a CTA for a {eventState} event")
    fun addEventShowcaseToTheTopBannerContainer(eventState: String) {
        // Create territory, showcase, and event content
        val territoryId = TestDataManager.createTerritory(publish = true).id
        val showcaseId = TestDataManager.createTheShowcase(territoryId).id
        val contentData = TestDataManager.createContent(ContentType.EVENT.name)
        val content = TestDataManager.createPublishContent(contentData.id, territoryId, eventState)

        // Prepare and publish showcase
        bannerPreparator.prepareShowcaseWithContent(territoryId, showcaseId, content.id)

        // Wait for showcase to be added and content to be playable
        waitForShowcaseAndContent(ContentType.EVENT)

        // Restart app to reflect changes
        MobileScreenUtils.restartTheApp()
    }

    /**
     * Waits for the showcase to be added to the banner and for content to become playable.
     *
     * @param contentType The type of content to wait for.
     */
    private fun waitForShowcaseAndContent(contentType: ContentType) {
        waitTillTopBannerShowcaseChildAdded()
        waitUntilContentIsPlayable(contentType)
    }

    /**
     * Waits until the showcase with specific title is added to the top banner showcase.
     */
    private fun waitTillTopBannerShowcaseChildAdded() {
        val context = TestContextManager.getTestContext()
        val bannerShowcaseId = context.get<ClientResolutionResponse>(CLIENT_RESOLUTION_DATA).bannerShowcaseId
        val showcaseTitle = context.get<String>(SHOWCASES_TITLE)

        EntertainmentService.waitTillShowcaseChildAdded(bannerShowcaseId, showcaseTitle)
    }

    /**
     * Waits until the content video becomes playable.
     *
     * @param contentType The type of content to wait for.
     */
    private fun waitUntilContentIsPlayable(contentType: ContentType) {
        val content = TestContextManager.getTestContext().get<ContentResponseV2>(CONTENT_RESPONSE)

        val request = GetContentDataRequest(
            contentId = content.id,
            contentType = contentType.name,
            productId = content.productId
        )

        MorpheusService.waitUntilContentIsPlayable(request)
    }

    // ========== Showcase Search Operations ==========

    /**
     * Finds a showcase by section title on a specific tab.
     *
     * @param title The title of the showcase section.
     * @param tabName The name of the tab (Home or Explore).
     * @return [ShowCaseResponse] if found.
     * @throws CustomException if tab name is invalid.
     * @throws SkipException if showcase is not found.
     */
    fun findScreenShowcaseBySectionTitle(title: String, tabName: String): ShowCaseResponse {
        val clientResolution = TestContextManager.getTestContext()
            .get<ClientResolutionResponse>(CLIENT_RESOLUTION_DATA)

        val showcaseId = when (tabName) {
            "Home" -> clientResolution.homeShowcaseId
            "Explore" -> clientResolution.mobileShowcaseExplore
            else -> throw CustomException("'$tabName' is not a valid tab name.")
        }

        val showcaseList = EntertainmentService.getShowcaseChildList(showcaseId)

        return showcaseList.find { it.title == title }
            ?: throw SkipException("No showcase found with CTA label '$title'")
    }

    /**
     * Finds a home screen banner showcase by CTA label and optional redirect type.
     *
     * @param ctaLabel The CTA label to search for.
     * @param redirectType Optional redirect type filter.
     * @return [ShowCaseResponse] if found.
     * @throws SkipException if showcase is not found.
     */
    fun findHomeScreenBannerShowcaseByDetailsCtaLabel(ctaLabel: String, redirectType: String = ""): ShowCaseResponse {
        val clientResolution = TestContextManager.getTestContext()
            .get<ClientResolutionResponse>(CLIENT_RESOLUTION_DATA)
        val showcaseList = EntertainmentService.getShowcaseChildList(clientResolution.homeShowcaseId)

        return if (redirectType.isNotEmpty()) {
            findShowcaseByCtaAndRedirectType(showcaseList, ctaLabel, redirectType)
        } else {
            findShowcaseByCtaLabel(showcaseList, ctaLabel)
        }
    }

    /**
     * Finds a showcase by CTA label and redirect type.
     */
    private fun findShowcaseByCtaAndRedirectType(
        showcaseList: List<ShowCaseResponse>, ctaLabel: String, redirectType: String
    ): ShowCaseResponse {
        return showcaseList.find {
            it.details?.callToAction?.label == ctaLabel &&
                    it.details.callToAction.redirectType == redirectType
        } ?: throw SkipException(
            "No showcase found with CTA label '$ctaLabel' and redirectType '$redirectType'"
        )
    }

    /**
     * Finds a showcase by CTA label only.
     */
    private fun findShowcaseByCtaLabel(showcaseList: List<ShowCaseResponse>, ctaLabel: String): ShowCaseResponse {
        return showcaseList.find {
            it.callToAction?.label == ctaLabel
        } ?: throw SkipException("No showcase found with CTA label '$ctaLabel'")
    }
}
