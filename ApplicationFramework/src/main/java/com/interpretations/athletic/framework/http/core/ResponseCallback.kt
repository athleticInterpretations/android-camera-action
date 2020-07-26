package com.interpretations.athletic.framework.http.core

interface ResponseCallback<T : EmptyStateInfo> {

    /**
     * Represents that a request concluded successfully.
     *
     * @param response A successful variant of the [Response]
     */
    fun onSuccess(response: Response.Success<T>)

    /**
     * Represents that a request failed.
     *
     * @param failure A failed variant of the [Response]
     */
    fun onFailure(failure: Response.Failure<T>)
}

/**
 * Denotes [ResponseCallback.onSuccess] as an alias
 */
typealias OnSuccess<T> = (Response.Success<T>) -> Unit

/**
 * Denotes [ResponseCallback.onFailure] as an alias
 */
typealias OnFailure<T> = (Response.Failure<T>) -> Unit