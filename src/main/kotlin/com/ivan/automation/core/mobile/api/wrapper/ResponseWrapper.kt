package com.ivan.automation.core.mobile.api.wrapper

import io.restassured.response.Response
import org.hamcrest.Matcher
import org.w3c.dom.Node

class ResponseWrapper(private val response: Response) {

    fun getBody() = response.body

    fun getResponse() = response

    fun <T> body(path: String, matcher: Matcher<T>) = apply {
        response.then().body(path, matcher)
    }

    fun <T> body(matcher: Matcher<T>) = apply {
        response.then().body("", matcher)
    }

    fun <T> body(node: Matcher<Node>, matcher: Matcher<T>) = apply {
        response.then().body(node, matcher)
    }

    fun status(matcher: Matcher<Int>) = apply {
        response.then().statusCode(matcher)
    }
}
