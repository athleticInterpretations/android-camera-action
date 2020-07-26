package com.interpretations.athletic.framework.http.core

private val RANGE_SUCCESSFUL_CODE = 200..299

private const val MESSAGE_UNKNOWN_STATUS_CODE = "Unknown Status Code"

/**
 * Represents an HTTP status with code and message.
 * @property code numeric value of the HTTP response status
 * @property message description of the status
 */
class HttpStatusCode private constructor(val code: Int, val message: String) {

    /**
     * Returns `true` if the status code is considered successful according to HTTP standards.
     *
     * Codes from 200 to 299 are considered to be successful.
     */
    val isSuccessful: Boolean
        get() = code in RANGE_SUCCESSFUL_CODE

    override fun toString(): String = "$code $message"

    override fun equals(other: Any?) = other is HttpStatusCode && other.code == this.code

    override fun hashCode() = code.hashCode()

    companion object {

        /**
         * 1XX: Informational. Indicates a provisional response only consisting of status code and optional headers.
         */
        val Continue = HttpStatusCode(100, "Continue")
        val SwitchingProtocols = HttpStatusCode(101, "Switching Protocols")
        val Processing = HttpStatusCode(102, "Processing")

        /**
         * 2XX: Success. Indicates that the client's request was successfully received, understood, and accepted.
         */
        val OK = HttpStatusCode(200, "OK")
        val Created = HttpStatusCode(201, "Created")
        val Accepted = HttpStatusCode(202, "Accepted")
        val NonAuthoritativeInformation = HttpStatusCode(203, "Non-Authoritative Information")
        val NoContent = HttpStatusCode(204, "No Content")
        val ResetContent = HttpStatusCode(205, "Reset Content")
        val PartialContent = HttpStatusCode(206, "Partial Content")
        val MultiStatus = HttpStatusCode(207, "Multi-Status")

        /**
         * 3XX: Redirection. Indicates that further action needs to be taken by the user agent in order to fulfill the request.
         */
        val MultipleChoices = HttpStatusCode(300, "Multiple Choices")
        val MovedPermanently = HttpStatusCode(301, "Moved Permanently")
        val Found = HttpStatusCode(302, "Found")
        val SeeOther = HttpStatusCode(303, "See Other")
        val NotModified = HttpStatusCode(304, "Not Modified")
        val UseProxy = HttpStatusCode(305, "Use Proxy")
        val SwitchProxy = HttpStatusCode(306, "Switch Proxy")
        val TemporaryRedirect = HttpStatusCode(307, "Temporary Redirect")
        val PermanentRedirect = HttpStatusCode(308, "Permanent Redirect")

        /**
         * 4XX: Client Error. Indicates cases where the client has erred.
         */
        val BadRequest = HttpStatusCode(400, "Bad Request")
        val Unauthorized = HttpStatusCode(401, "Unauthorized")
        val PaymentRequired = HttpStatusCode(402, "Payment Required")
        val Forbidden = HttpStatusCode(403, "Forbidden")
        val NotFound = HttpStatusCode(404, "Not Found")
        val MethodNotAllowed = HttpStatusCode(405, "Method Not Allowed")
        val NotAcceptable = HttpStatusCode(406, "Not Acceptable")
        val ProxyAuthenticationRequired = HttpStatusCode(407, "Proxy Authentication Required")
        val RequestTimeout = HttpStatusCode(408, "Request Timeout")
        val Conflict = HttpStatusCode(409, "Conflict")
        val Gone = HttpStatusCode(410, "Gone")
        val LengthRequired = HttpStatusCode(411, "Length Required")
        val PreconditionFailed = HttpStatusCode(412, "Precondition Failed")
        val PayloadTooLarge = HttpStatusCode(413, "Payload Too Large")
        val RequestURITooLong = HttpStatusCode(414, "Request-URI Too Long")
        val UnsupportedMediaType = HttpStatusCode(415, "Unsupported Media Type")
        val RequestedRangeNotSatisfiable =
                HttpStatusCode(416, "Requested Range Not Satisfiable")
        val ExpectationFailed = HttpStatusCode(417, "Expectation Failed")
        val UnprocessableEntity = HttpStatusCode(422, "Unprocessable Entity")
        val Locked = HttpStatusCode(423, "Locked")
        val FailedDependency = HttpStatusCode(424, "Failed Dependency")
        val UpgradeRequired = HttpStatusCode(426, "Upgrade Required")
        val TooManyRequests = HttpStatusCode(429, "Too Many Requests")
        val RequestHeaderFieldTooLarge = HttpStatusCode(431, "Request Header Fields Too Large")

        /**
         * 5XX: Server Error. Indicates that the server is aware that it has erred or is incapable of performing the request.
         */
        val InternalServerError = HttpStatusCode(500, "Internal Server Error")
        val NotImplemented = HttpStatusCode(501, "Not Implemented")
        val BadGateway = HttpStatusCode(502, "Bad Gateway")
        val ServiceUnavailable = HttpStatusCode(503, "Service Unavailable")
        val GatewayTimeout = HttpStatusCode(504, "Gateway Timeout")
        val VersionNotSupported = HttpStatusCode(505, "HTTP Version Not Supported")
        val VariantAlsoNegotiates = HttpStatusCode(506, "Variant Also Negotiates")
        val InsufficientStorage = HttpStatusCode(507, "Insufficient Storage")

        private val allStatusCodes = getAllHttpStatuses()

        /**
         * Creates an instance of [HttpStatusCode] with the given numeric value. Returns "Unknown Status Code" if [code] is not identified.
         *
         * @param code HTTP status code
         *
         * @return [HttpStatusCode] for corresponding [code]
         */
        fun fromStatusCode(code: Int, message: String = MESSAGE_UNKNOWN_STATUS_CODE): HttpStatusCode {
            val knownStatus = allStatusCodes.firstOrNull { it.code == code }
            return knownStatus ?: HttpStatusCode(code, message)
        }
    }
}

internal fun getAllHttpStatuses() = listOf(
        HttpStatusCode.Continue,
        HttpStatusCode.SwitchingProtocols,
        HttpStatusCode.Processing,
        HttpStatusCode.OK,
        HttpStatusCode.Created,
        HttpStatusCode.Accepted,
        HttpStatusCode.NonAuthoritativeInformation,
        HttpStatusCode.NoContent,
        HttpStatusCode.ResetContent,
        HttpStatusCode.PartialContent,
        HttpStatusCode.MultiStatus,
        HttpStatusCode.MultipleChoices,
        HttpStatusCode.MovedPermanently,
        HttpStatusCode.Found,
        HttpStatusCode.SeeOther,
        HttpStatusCode.NotModified,
        HttpStatusCode.UseProxy,
        HttpStatusCode.SwitchProxy,
        HttpStatusCode.TemporaryRedirect,
        HttpStatusCode.PermanentRedirect,
        HttpStatusCode.BadRequest,
        HttpStatusCode.Unauthorized,
        HttpStatusCode.PaymentRequired,
        HttpStatusCode.Forbidden,
        HttpStatusCode.NotFound,
        HttpStatusCode.MethodNotAllowed,
        HttpStatusCode.NotAcceptable,
        HttpStatusCode.ProxyAuthenticationRequired,
        HttpStatusCode.RequestTimeout,
        HttpStatusCode.Conflict,
        HttpStatusCode.Gone,
        HttpStatusCode.LengthRequired,
        HttpStatusCode.PreconditionFailed,
        HttpStatusCode.PayloadTooLarge,
        HttpStatusCode.RequestURITooLong,
        HttpStatusCode.UnsupportedMediaType,
        HttpStatusCode.RequestedRangeNotSatisfiable,
        HttpStatusCode.ExpectationFailed,
        HttpStatusCode.UnprocessableEntity,
        HttpStatusCode.Locked,
        HttpStatusCode.FailedDependency,
        HttpStatusCode.UpgradeRequired,
        HttpStatusCode.TooManyRequests,
        HttpStatusCode.RequestHeaderFieldTooLarge,
        HttpStatusCode.InternalServerError,
        HttpStatusCode.NotImplemented,
        HttpStatusCode.BadGateway,
        HttpStatusCode.ServiceUnavailable,
        HttpStatusCode.GatewayTimeout,
        HttpStatusCode.VersionNotSupported,
        HttpStatusCode.VariantAlsoNegotiates,
        HttpStatusCode.InsufficientStorage
)
