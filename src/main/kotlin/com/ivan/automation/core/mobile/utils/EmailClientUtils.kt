package com.ivan.automation.core.mobile.utils

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.ivan.automation.core.mobile.api.wrapper.RequestWrapper
import com.ivan.automation.core.mobile.api.wrapper.ResponseWrapper
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import org.testng.SkipException
import java.net.URI
import java.util.*

/**
 * A singleton client for creating disposable PutsBox email addresses
 * and polling for incoming messages.
 */
object EmailClientUtils {
    private const val ENDPOINT: String = "https://preview.putsbox.com"
    private const val MAILBOX_UUID_LENGTH = 8

    private val log = LoggerFactory.getLogger(EmailClientUtils::class.java)

    /**
     * Generates a random email address on the putsbox.com domain.
     * Uses a short UUID segment as the mailbox name.
     */
    fun generateEmailAddress(): String {
        val mailbox = UUID.randomUUID().toString().take(MAILBOX_UUID_LENGTH)
        return "$mailbox@putsbox.com"
    }

    /**
     * Executes a GET request against the email endpoint, retrying until a successful (HTTP 200) response
     * is received or the timeout is reached.
     *
     * @param path             the request path to append to the base email endpoint URI
     * @return                 a [ResponseWrapper] wrapping the final HTTP response
     */
    private fun executeEmailRequest(path: String): ResponseWrapper {
        return HttpRetryExecutor
            .setPool()
            .setExpression {
                RequestWrapper()
                    .baseUri(ENDPOINT)
                    .get(path)
            }
            .setCondition { respWrapper ->
                respWrapper.getResponse().statusCode == HttpStatus.SC_OK
            }
            .execute()
    }

    /**
     * Validates the HTTP response status and deserializes the response body into an Email.
     *
     * @param wrapper the ResponseWrapper containing HTTP response details
     * @param address the email address
     * @return the deserialized Email object if the response status is 200 OK
     * @throws CustomException if the response status is not 200 OK
     */
    private fun handleEmailResponse(wrapper: ResponseWrapper, address: String): Email {
        if (wrapper.getResponse().statusCode == HttpStatus.SC_OK) {
            log.info("Email is received")
            return wrapper.getBody().`as`(Email::class.java) as Email
        }
        val statusCode = wrapper.getResponse().statusCode
        log.error("Failed to get email: status code was $statusCode")
        throw SkipException("Failed to get email for $address: code=$statusCode")
    }

    @Throws(CustomException::class)
    fun waitForEmail(address: String): Email {
        val bucket = address.substringBefore("@")
        val path = "/p/$bucket/last.json"

        try {
            val wrapper = executeEmailRequest(path)
            return handleEmailResponse(wrapper, address)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw CustomException("Operation interrupted while fetching email for $address", e)
        } catch (e: JsonProcessingException) {
            throw CustomException("Failed to parse email response for $address", e)
        }
    }

    /**
     * Utility function to extract the first href value containing "auth" from an email's HTML.
     *
     * @param address  The email address to poll for the message.
     * @return         The first link whose href contains "auth", or null if none found.
     */
    fun extractLinkFromHtml(address: String): String? {
        // Poll for the email
        val email = waitForEmail(address)
        val html = email.html ?: return null

        // Regex to match href="…"
        val hrefRegex = Regex("""href\s*=\s*"([^"]*)"""", RegexOption.IGNORE_CASE)

        return hrefRegex.findAll(html)
            .map { it.groupValues[1] }
            .firstOrNull { link ->
                link.contains("auth")
            }
    }

    /**
     * Parses a full oobContinue URL of the form
     *   https://auth-qa.ivan.com/davinci/connections/.../callback/oobContinue?challenge=XYZ
     * and returns:
     *   1) the baseUri   ("https://auth-qa.ivan.com")
     *   2) the path      ("davinci/connections/.../callback/oobContinue")
     *   3) the challenge ("XYZ")
     *
     * @throws IllegalArgumentException if the challenge param is missing.
     */
    fun parseLinkFromEmail(fullUrl: String): Triple<String, String, String> {
        val uri = URI(fullUrl)

        // build the baseUri (scheme + host [+ port])
        val scheme = uri.scheme  // "https"
        val host = uri.host    // "auth-qa.ivan.com"
        val port = uri.port    // -1 if none
        val baseUri = if (port == -1) {
            "$scheme://$host"
        } else {
            "$scheme://$host:$port"
        }

        // extract the path without the leading slash
        val rawPath = uri.rawPath   // "/davinci/connections/.../oobContinue"
        val path = rawPath.removePrefix("/")

        // pull the challenge value out of the query string
        val query = uri.query             // "challenge=de1fbf8b-...-14120bb86c38"
            ?: throw IllegalArgumentException("No query string in URL")
        val challenge = query
            .split("&")
            .firstOrNull { it.startsWith("challenge=") }
            ?.substringAfter("challenge=")
            ?: throw IllegalArgumentException("No 'challenge' parameter in URL")

        return Triple(baseUri, path, challenge)
    }

    data class Email(
        val id: String? = null,
        val headers: String? = null,
        @JsonProperty("from_email")
        val fromEmail: String? = null,
        @JsonProperty("from_name")
        val fromName: String? = null,
        val to: List<String>? = null,
        val email: String? = null,
        val subject: String? = null,
        val text: String? = null,
        val html: String? = null,
        val attachments: List<Any>? = null,
        @JsonProperty("created_at")
        val createdAt: String? = null
    )
}
