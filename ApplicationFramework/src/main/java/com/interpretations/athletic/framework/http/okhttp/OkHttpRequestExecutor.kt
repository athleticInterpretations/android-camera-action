package com.interpretations.athletic.framework.http.okhttp

import androidx.annotation.VisibleForTesting
import com.interpretations.athletic.framework.http.HttpException
import com.interpretations.athletic.framework.http.core.HttpStatusCode
import com.interpretations.athletic.framework.http.listeners.HttpRequestExecutor
import com.interpretations.athletic.framework.http.listeners.HttpResponseCallback
import com.interpretations.athletic.framework.http.okhttp.internal.RequestCleanupSpec
import com.interpretations.athletic.framework.http.okhttp.internal.RequestCleanupStrategy
import com.interpretations.athletic.framework.http.okhttp.internal.RequestState
import com.interpretations.athletic.framework.http.request.HttpMethod
import com.interpretations.athletic.framework.http.request.HttpRequest
import com.interpretations.athletic.framework.http.request.RequestPayload
import com.interpretations.athletic.framework.http.response.ErrorItem
import com.interpretations.athletic.framework.http.response.ResponseItem
import com.interpretations.athletic.framework.utils.logger.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionSpec
import okhttp3.Headers.Companion.toHeaders
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.Closeable
import java.io.IOException

internal const val TAG = "OkHttpRequestExecutor"
internal const val ERROR_MESSAGE_NULL_EMPTY_URL = "Url cannot be null or empty"
internal val EMPTY_BYTE_ARRAY = ByteArray(0)
internal val EMPTY_REQUEST = EMPTY_BYTE_ARRAY.toRequestBody(null, 0, 0)

/**
 * HTTP request executor implementation for [OkHttpClient].
 * @property client Factory for calls, which can be used to send HTTP requests and read their responses.
 * @property connectionSpec Unencrypted, unauthenticated connections for http: URLs.
 * @property requestCleanupStrategy Cleanup strategy for removing resources stored in cache.
 * @constructor
 */
open class OkHttpRequestExecutor(
        private var client: OkHttpClient = OkHttpClient(),
        private var connectionSpec: List<ConnectionSpec> = emptyList(),
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        private val requestCleanupStrategy: RequestCleanupSpec = RequestCleanupStrategy()
) : HttpRequestExecutor {

    override fun execute(httpRequest: HttpRequest, callback: HttpResponseCallback?) {

        requestCleanupStrategy.onStateChanged(RequestState.Ongoing)
        requestCleanupStrategy.callback = callback

        // establish pre-checks
        if (httpRequest.url.isNullOrEmpty()) {
            callback?.onFailure(ErrorItem.GenericErrorItem(NullPointerException(ERROR_MESSAGE_NULL_EMPTY_URL)))
            requestCleanupStrategy.onStateChanged(RequestState.Failed)
            return
        }

        // build (instantiate) client configuration with consideration of custom properties
        if (connectionSpec.isNotEmpty()) {
            // add connection specs
            client = client.newBuilder()
                .connectionSpecs(connectionSpec)
                .build()
        }

        // initialize request
        val okHttpRequest = buildRequest(httpRequest.url, httpRequest.httpMethod, httpRequest.headers, httpRequest.requestPayload)
        val apiCall = client.newCall(okHttpRequest)

        // prepares cleanup for future requests
        requestCleanupStrategy.ongoingCall = apiCall

        // execute request
        apiCall.enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                handleHttpRequestFailure(e, callback)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    handleSuccessfulResponse(response, callback)
                } else {
                    handleErrorResponse(response, callback)
                }
                closeQuietly(response, TAG)
            }
        })
    }

    /**
     * Cancel ongoing/last know request. Although request cancellation is not guaranteed, this call will ensure if cancelled, the
     * response callback reflects the correct state along with internal resource cleanup.
     */
    override fun cancel() {
        if (requestCleanupStrategy.ongoingCall == null || requestCleanupStrategy.ongoingCall?.isCanceled() == true) {
            return
        }
        requestCleanupStrategy.ongoingCall?.cancel()
        requestCleanupStrategy.callback?.onCancelled()
        requestCleanupStrategy.onStateChanged(RequestState.Cancelled)
    }

    /**
     * Build an appropriate [Request] object for the HTTP request.
     *
     * @param url the request url
     * @param httpMethod the HTTP method
     * @param httpHeaders the HTTP request header
     * @param requestPayload the request payload
     *
     * @return [Request] object for the HTTP request.
     */
    @VisibleForTesting
    fun buildRequest(url: String, httpMethod: HttpMethod, httpHeaders: Map<String, String>?, requestPayload: RequestPayload?): Request {
        return Request.Builder().apply {
            // initialize URL
            when (val httpUrl = buildHttpUrl(url, requestPayload)) {
                null -> url(url)
                else -> url(httpUrl)
            }
            // initialize headers
            httpHeaders?.let {
                headers(it.toHeaders())
            }
            val requestBody = buildRequestBody(requestPayload)
            when (httpMethod) {
                HttpMethod.GET -> get()
                HttpMethod.POST -> post(requestBody.orEmpty())
                HttpMethod.PUT -> put(requestBody.orEmpty())
                HttpMethod.PATCH -> patch(requestBody.orEmpty())
                HttpMethod.DELETE -> requestBody?.let { delete(it) } ?: delete()
            }
        }.build()
    }

    /**
     * Build an appropriate [HttpUrl] from the [requestPayload]. Adds additional query parameters to the url if necessary.
     *
     * @param url the request url
     * @param requestPayload the request payload
     *
     * @return a nullable [HttpUrl] with the appropriate query parameters, if necessary.
     */
    @VisibleForTesting
    fun buildHttpUrl(url: String, requestPayload: RequestPayload?): HttpUrl? {
        if (requestPayload !is RequestPayload.UrlQueryParameters) {
            return url.toHttpUrlOrNull()
        }
        if (requestPayload.queryParameters.isNullOrEmpty()) {
            return url.toHttpUrlOrNull()
        }
        return url.toHttpUrlOrNull()?.newBuilder()?.apply {
            requestPayload.queryParameters.keys.forEach { key ->
                addEncodedQueryParameter(key, requestPayload.queryParameters[key])
            }
        }?.build()
    }

    /**
     * Build the HTTP request body.
     *
     * @param requestPayload the request payload
     *
     * @return a nullable [RequestBody] for the HTTP request.
     */
    @VisibleForTesting
    fun buildRequestBody(requestPayload: RequestPayload?): RequestBody? {
        return when (requestPayload) {
            is RequestPayload.StringRequestPayload ->
                requestPayload.value.orEmpty()
                        .toRequestBody(requestPayload.contentType.orEmpty().toMediaTypeOrNull())
            is RequestPayload.EmptyRequestPayload -> EMPTY_REQUEST
            else -> null
        }
    }

    /**
     * Handle [okhttp3.Callback.onResponse] events for an HTTP request.
     *
     * This method specifically handles cases when a response successfully concludes (HTTP response code is between 200 and 300).
     */
    private fun handleSuccessfulResponse(response: Response, callback: HttpResponseCallback?) {
        if (requestCleanupStrategy.currentRequestState == RequestState.Cancelled) {
            return
        }
        val stringBody = response.body?.string()
        val statusCode = HttpStatusCode.fromStatusCode(response.code)
        val responseHeaders = response.headers

        val headers = mutableMapOf<String, String>()
        responseHeaders.forEach { headers[it.first] = it.second }

        callback?.onSuccess(
                response = if (stringBody.isNullOrEmpty()) {
                    ResponseItem.EmptyResponseItem(statusCode, headers)
                } else {
                    ResponseItem.StringResponseItem(statusCode, stringBody, headers)
                }
        )
        requestCleanupStrategy.onStateChanged(RequestState.Successful)
    }

    /**
     * Handle [okhttp3.Callback.onFailure] events for an HTTP request.
     */
    private fun handleHttpRequestFailure(e: IOException, callback: HttpResponseCallback?) {
        if (requestCleanupStrategy.currentRequestState == RequestState.Cancelled) {
            return
        }
        callback?.onFailure(ErrorItem.GenericErrorItem(e))
        requestCleanupStrategy.onStateChanged(RequestState.Failed)
    }

    /**
     * Handle [okhttp3.Callback.onResponse] events for an HTTP request.
     *
     * This method specifically handles cases when a response is unsuccessful (HTTP response code is higher than 300).
     */
    private fun handleErrorResponse(response: Response, callback: HttpResponseCallback?) {
        if (requestCleanupStrategy.currentRequestState == RequestState.Cancelled) {
            return
        }
        val responseTimeInMillSecs = response.receivedResponseAtMillis - response.sentRequestAtMillis
        callback?.onFailure(
                ErrorItem.HttpErrorItem(
                        httpStatusCode = HttpStatusCode.fromStatusCode(response.code),
                        responseTimeInMillis = responseTimeInMillSecs,
                        exception = HttpException(response.body?.string())
                )
        )
        requestCleanupStrategy.onStateChanged(RequestState.Failed)
    }

    /**
     * An extension function on [RequestBody] to return [EMPTY_REQUEST] if null.
     */
    private fun RequestBody?.orEmpty() = this ?: EMPTY_REQUEST

    /**
     * Method which closes a [Closeable] and absorbs [IOException] if it is thrown
     */
    internal fun closeQuietly(closeable: Closeable?, tag: String) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (e: IOException) {
                Logger.i(tag, "Unable to close the closeable.")
            }
        }
    }
}