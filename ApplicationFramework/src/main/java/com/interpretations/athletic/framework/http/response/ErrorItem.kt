package com.interpretations.athletic.framework.http.response

import com.interpretations.athletic.framework.http.core.HttpStatusCode

/**
 * Distinguishes between a runtime error and a failed HTTP response.
 *
 * @property exception the error incurred while making the HTTP request.
 */
sealed class ErrorItem(val exception: Exception) {

    /**
     * Represents an HTTP error response.
     *
     * @property httpStatusCode response status code
     * @property responseTimeInMillis response time in milli secs to complete the response
     * @property exception a bad HTTP request error
     */
    class HttpErrorItem(
        val httpStatusCode: HttpStatusCode,
        val responseTimeInMillis: Long? = null,
        exception: Exception
    ) : ErrorItem(exception)

    /**
     * Represents a generic runtime error.
     *
     * @property exception the runtime error that caused the HTTP request to fail.
     */
    class GenericErrorItem(
        exception: Exception
    ) : ErrorItem(exception)
}