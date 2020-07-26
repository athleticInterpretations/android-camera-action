package com.interpretations.athletic.framework.http.listeners

import com.interpretations.athletic.framework.http.request.HttpRequest
import com.interpretations.athletic.framework.http.okhttp.OkHttpRequestExecutor

/**
 * This interface establishes a common contract for hiding HTTP library dependencies.
 *
 * @see OkHttpRequestExecutor
 */
interface HttpRequestExecutor {

    /**
     * Execute a HTTP request.
     *
     * @param httpRequest collection of HTTP request models - method, headers and body
     * @param callback HTTP callback for the call-site to receive the HTTP response
     */
    fun execute(httpRequest: HttpRequest, callback: HttpResponseCallback?)

    /**
     * Cancel ongoing HTTP request, if applicable.
     */
    fun cancel()
}