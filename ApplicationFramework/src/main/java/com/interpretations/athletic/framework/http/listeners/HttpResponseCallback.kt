package com.interpretations.athletic.framework.http.listeners

import com.interpretations.athletic.framework.http.response.ErrorItem
import com.interpretations.athletic.framework.http.response.ResponseItem

/**
 * A success/failure driven callback for HTTP response(s).
 */
interface HttpResponseCallback {

    /**
     * Callback for a successful HTTP response
     *
     * @param response the response body of the HTTP request
     */
    fun onSuccess(response: ResponseItem)

    /**
     * Callback for failed HTTP response
     *
     * @param httpErrorItem the status code for the HTTP request
     */
    fun onFailure(httpErrorItem: ErrorItem)

    /**
     * Callback for a cancelled HTTP request
     */
    fun onCancelled()
}