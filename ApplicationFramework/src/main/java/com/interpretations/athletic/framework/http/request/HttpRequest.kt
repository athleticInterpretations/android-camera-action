package com.interpretations.athletic.framework.http.request

/**
 * Represents all the required parameters for an HTTP request.
 * @property url the request URL
 * @property httpMethod type of HTTP request method to be used. For e.g. [HttpMethod.GET], [HttpMethod.POST], etc.
 * @property headers additional headers to be included in the HTTP request.
 * @property requestPayload the request parameter to be included as a part of the HTTP request.
 */
class HttpRequest(
    val url: String?,
    val httpMethod: HttpMethod,
    val headers: Map<String, String>? = null,
    val requestPayload: RequestPayload? = null
)