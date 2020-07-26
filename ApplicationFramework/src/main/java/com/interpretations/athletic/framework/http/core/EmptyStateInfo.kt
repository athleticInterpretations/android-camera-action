package com.interpretations.athletic.framework.http.core

/**
 * Provide [isEmpty] metadata about the [Response.Success] object.
 */
interface EmptyStateInfo {

    /**
     * Returns `true` if [Response.Success.response] is an empty response object.
     */
    fun isEmpty(): Boolean
}