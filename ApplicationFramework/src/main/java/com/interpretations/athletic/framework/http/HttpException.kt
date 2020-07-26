package com.interpretations.athletic.framework.http

import com.interpretations.athletic.framework.http.okhttp.OkHttpRequestExecutor

/**
 * Class to map [HttpException] in [OkHttpRequestExecutor].
 *
 * @property responseBody error string of [HttpException]
 */
data class HttpException(val responseBody: String?) : Exception(responseBody)