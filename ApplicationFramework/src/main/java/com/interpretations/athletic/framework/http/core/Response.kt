package com.interpretations.athletic.framework.http.core

import com.interpretations.athletic.framework.http.response.ErrorItem

sealed class Response<T : EmptyStateInfo> {

    /**
     * Represents a successful response.
     *
     * @property identifier description text or label for the HTTP request.
     * @property httpStatusCode request status code for the API request.
     * @property response object of the API request.
     */
    data class Success<T : EmptyStateInfo>(
            val identifier: String? = null,
            val httpStatusCode: HttpStatusCode,
            val response: T
    ) : Response<T>()

    /**
     * Represents a failed request.
     *
     * @property identifier description text or label for the HTTP request.
     * @property exception a request error wrapped inside the [ErrorItem].
     */
    data class Failure<T : EmptyStateInfo>(
            val identifier: String? = null,
            val exception: ErrorItem
    ) : Response<T>()
}