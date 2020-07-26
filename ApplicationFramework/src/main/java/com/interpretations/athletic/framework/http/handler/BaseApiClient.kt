package com.interpretations.athletic.framework.http.handler

import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.interpretations.athletic.framework.http.core.EmptyStateInfo
import com.interpretations.athletic.framework.http.core.HttpStatusCode
import com.interpretations.athletic.framework.http.core.Response
import com.interpretations.athletic.framework.http.core.ResponseCallback
import com.interpretations.athletic.framework.http.listeners.HttpRequestExecutor
import com.interpretations.athletic.framework.http.listeners.HttpResponseCallback
import com.interpretations.athletic.framework.http.okhttp.OkHttpRequestExecutor
import com.interpretations.athletic.framework.http.response.ErrorItem
import com.interpretations.athletic.framework.http.response.ResponseItem

/**
 * This abstract class encapsulates HTTP logic.
 *
 * @property okHttpRequestExecutor This interface establishes a common contract for hiding HTTP library dependencies.
 * @property handler Class used to run a message loop for a thread
 * @property gson This is the main class for using Gson.
 */
abstract class BaseApiClient(
    protected val okHttpRequestExecutor: HttpRequestExecutor = OkHttpRequestExecutor(),
    protected val handler: Handler = Handler(Looper.getMainLooper()),
    protected val gson: Gson = Gson()
) {
    /**
     * FOR TESTING ONLY
     */
    internal var postRunnableHook: () -> Unit = {}

    /**
     * FOR TESTING ONLY
     */
    protected fun updatePostRunnableHook(postRunnableHook: () -> Unit) {
        this.postRunnableHook = postRunnableHook
    }

    /**
     * Listen for the [HttpResponseCallback] and update [ResponseCallback] accordingly.
     *
     * @param T Generic type parameter
     * @param identifier description text or label for the HTTP request.
     * @param emptyResponse Empty object for [T]
     * @param responseCallback Callback to notify call-site of `onSuccess` and `onFailure` events
     */
    protected inline fun <reified T : EmptyStateInfo> getHttpResponseCallback(
        identifier: String? = null,
        emptyResponse: T,
        responseCallback: ResponseCallback<T>?
    ) =
        object : HttpResponseCallback {
            override fun onSuccess(response: ResponseItem) {
                handleValidHttpResponse(
                    identifier,
                    response,
                    emptyResponse,
                    responseCallback,
                    T::class.java
                )
            }

            override fun onFailure(httpErrorItem: ErrorItem) {
                handleHttpResponseFailure(identifier, httpErrorItem, responseCallback)
            }

            override fun onCancelled() {
                // no-op
            }
        }

    /**
     * Handle callbacks for [ResponseCallback] when a HTTP request concludes successfully.
     *
     * @param T Generic type parameter
     * @param identifier description text or label for the HTTP request.
     * @param responseItem HTTP response item
     * @param emptyResponse Empty object for [T]
     * @param responseCallback Callback to notify call-site of `onSuccess` and `onFailure` events
     */
    protected fun <T : EmptyStateInfo> handleValidHttpResponse(
        identifier: String? = null,
        responseItem: ResponseItem,
        emptyResponse: T,
        responseCallback: ResponseCallback<T>?,
        tClass: Class<T>
    ) {
        when (responseItem) {
            is ResponseItem.StringResponseItem -> {
                try {
                    val responseData = gson.fromJson(responseItem.response, tClass)
                    handleResponseSuccess(
                        identifier,
                        responseItem.statusCode,
                        responseData,
                        responseCallback
                    )
                } catch (e: JsonSyntaxException) {
                    handleNonHttpFailure(identifier, e, responseCallback)
                }
            }
            is ResponseItem.EmptyResponseItem -> {
                handleResponseSuccess(
                    identifier,
                    responseItem.statusCode,
                    emptyResponse,
                    responseCallback
                )
            }
        }
    }

    /**
     * Handle callbacks for [ResponseCallback] when a response succeeds.
     *
     * @param T Generic type parameter
     * @param identifier description text or label for the HTTP request.
     * @param httpStatusCode Represents an HTTP status with code and message.
     * @param responseData Response data
     * @param responseCallback Callback to notify call-site of `onSuccess` and `onFailure` events
     */
    protected fun <T : EmptyStateInfo> handleResponseSuccess(
        identifier: String? = null,
        httpStatusCode: HttpStatusCode,
        responseData: T,
        responseCallback: ResponseCallback<T>?
    ) = notifyWithHandler {
        responseCallback?.onSuccess(Response.Success(identifier, httpStatusCode, responseData))
    }

    /**
     * Handle callbacks for [ResponseCallback] when a response fails.
     *
     * @param T Generic type parameter
     * @param identifier description text or label for the HTTP request.
     * @param errorItem Distinguishes between a runtime error and a failed HTTP response.
     * @param responseCallback Callback to notify call-site of `onSuccess` and `onFailure` events
     */
    protected fun <T : EmptyStateInfo> handleResponseFailure(
        identifier: String? = null,
        errorItem: ErrorItem,
        responseCallback: ResponseCallback<T>?
    ) = notifyWithHandler {
        responseCallback?.onFailure(Response.Failure(identifier, errorItem))
    }

    /**
     * Handle non-http error logging for failures and callbacks for failures.
     *
     * @param T Generic type parameter
     * @param identifier description text or label for the HTTP request.
     * @param exception An object that wraps an error event that occurred and contains information
     * about the error including its type.
     * @param responseCallback Callback to notify call-site of `onSuccess` and `onFailure` events
     */
    protected fun <T : EmptyStateInfo> handleNonHttpFailure(
        identifier: String? = null,
        exception: Exception,
        responseCallback: ResponseCallback<T>?
    ) {
        val exceptionItem = ErrorItem.GenericErrorItem(exception)
        handleResponseFailure(identifier, exceptionItem, responseCallback)
    }

    /**
     * Handle http error logging for Http failures and callbacks for failures.
     *
     * @param T Generic type parameter
     * @param identifier description text or label for the HTTP request.
     * @param errorItem Distinguishes between a runtime error and a failed HTTP response.
     * @param responseCallback Callback to notify call-site of `onSuccess` and `onFailure` events
     */
    protected fun <T : EmptyStateInfo> handleHttpResponseFailure(
        identifier: String? = null,
        errorItem: ErrorItem,
        responseCallback: ResponseCallback<T>?
    ) {
        handleResponseFailure(identifier, errorItem, responseCallback)
    }

    /**
     * Wrap [action] around [Handler]'s post call.
     */
    protected fun notifyWithHandler(action: () -> Unit) = handler.post { action() }
        .also { postRunnableHook() }
}
