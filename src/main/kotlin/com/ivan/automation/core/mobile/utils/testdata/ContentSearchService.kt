package com.ivan.automation.core.mobile.utils.testdata

import com.ivan.automation.core.mobile.api.models.enums.ContentType
import com.ivan.automation.core.mobile.api.models.enums.PublishStatus
import com.ivan.automation.core.mobile.api.models.response.ContentResponse
import com.ivan.automation.core.mobile.api.services.CmsService

/**
 * Service for searching and finding content.
 */
object ContentSearchService {

    /**
     * Searches for published content with a trailer video that is a movie with a production date.
     *
     * @return [ContentResponse] if found, null otherwise.
     */
    fun findContentWithTrailerVideo(): ContentResponse? {
        val videosList = CmsService.getAllVideos(ContentType.TRAILER.name)

        return videosList
            .filter { it.status == PublishStatus.PUBLISHED.name }
            .firstNotNullOfOrNull { video ->
                findMovieParentWithProductionDate(video.id)
            }
    }

    /**
     * Finds a movie parent with a production date for the given video ID.
     */
    private fun findMovieParentWithProductionDate(videoId: String): ContentResponse? {
        val videoParents = CmsService.getVideoParents(videoId)

        return videoParents.firstOrNull { parent ->
            parent.type == ContentType.MOVIE.name && parent.productionDate != null
        }
    }
}
