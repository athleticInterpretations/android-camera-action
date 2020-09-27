package com.interpretations.athletic.framework.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.telephony.TelephonyManager
import android.text.util.Linkify
import android.util.DisplayMetrics
import android.util.Patterns
import android.widget.TextView
import com.interpretations.athletic.framework.BuildConfig
import com.interpretations.athletic.framework.constants.Constants
import com.interpretations.athletic.framework.utils.logger.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object FrameworkUtils {
    private const val MINIMUM_PASSWORD_LENGTH = 6
    private const val DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss"

    // click control threshold
    private const val CLICK_THRESHOLD = 300
    private var lastClickTime: Long = 0

    // tags
    // used for app debugging
    private const val TAG_MEMORY = "debug-memory"
    private const val TAG_INFO = "debug-info"

    // device attributes
    // used for app debugging
    private const val OS_VERSION = "os.version"

    /**
     * Method is used for printing the memory usage. This is used
     * only for verbosity mode
     *
     * @param name fragment or class simple name
     */
    fun printMemory(name: String) {
        if (Constants.DEBUG && Constants.DEBUG_VERBOSE) {
            val totalMemory = Runtime.getRuntime().totalMemory()
            val freeMemory = Runtime.getRuntime().freeMemory()
            val usedMemory = totalMemory - freeMemory
            // note that you cannot divide a long by a long value, this
            // refers to (long/long - long) operation giving a long result of 0
            val percentFree = (freeMemory.toFloat() / totalMemory * 100).toLong()
            val percentUsed = (usedMemory.toFloat() / totalMemory * 100).toLong()
            if (percentFree <= 2) {
                Logger.e(TAG_MEMORY, "===== MEMORY WARNING :: Available memory is low! Please add " +
                        "'art-' to your regex tag to see that gc is freeing up more available memory =====")
            }
            // printing memory details
            Logger.d(TAG_MEMORY, "===== Memory recorded from " + name + " :: " +
                    "MAX_MEMORY:" + Runtime.getRuntime().maxMemory() +
                    "  // FREE_MEMORY:" + freeMemory + " (" + percentFree + "% free)" +
                    "  // TOTAL_MEMORY:" + totalMemory +
                    "  // USED_MEMORY:" + usedMemory + " (" + percentUsed + "% used) =====")
        }
    }

    /**
     * Method is used to print device and application information. This is
     * used only for verbosity mode
     *
     * @param context  Interface to global information about an application environment
     */
    fun printInfo(context: Context) {
        if (Constants.DEBUG && Constants.DEBUG_VERBOSE) {
            // determine phone carrier
            val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val carrierName = manager.networkOperatorName
            // get display metrics
            val displayMetrics = DisplayMetrics()
            (context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            try {
                Logger.i(TAG_INFO, "===== DEVICE INFORMATION =====" +
                        "\nManufacturer: " + Build.MANUFACTURER +
                        "\nRegistrationModel: " + Build.MODEL +
                        "\nDevice/Product Id: " + Build.PRODUCT +
                        "\nCarrier: " + carrierName +
                        "\nOS Version: " + System.getProperty(OS_VERSION) +
                        "\nAPI Level: " + Build.VERSION.SDK_INT +
                        "\nScreen size (width/height): " +
                        displayMetrics.widthPixels + "/" +
                        displayMetrics.heightPixels +
                        "\n===== APP INFORMATION =====" +
                        "\nApp Version: " + BuildConfig.VERSION_NAME +
                        "\nBuild Type: " + BuildConfig.BUILD_TYPE +
                        "\nVersion Code: " + BuildConfig.VERSION_CODE +
                        "\nPackage Name: " + context.packageName +
                        "\nGoogle Map API Version: " + context.packageManager
                        .getPackageInfo("com.google.android.apps.maps", 0).versionName)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Method is used to Linkify words in a TextView
     *
     * @param textView TextView who's text you want to change
     * @param linkThis A regex of what text to turn into a link
     * @param toThis   The url you want to send the user to
     */
    fun linkify(textView: TextView, linkThis: String, toThis: String) {
        val pattern = Pattern.compile(linkThis)
        Linkify.addLinks(textView, pattern, toThis, { _, _, _ -> true })
        { _, _ -> "" }
    }

    /**
     * Method is used to confirm that string parameter is in valid email format
     *
     * @param email Email of the user
     * @return True if email is valid format, otherwise false
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
                email.substring(email.lastIndexOf(".") + 1).length > 1
    }

    /**
     * Method is used to confirm that a password was entered and is the correct length
     * [MINIMUM_PASSWORD_LENGTH]
     *
     * @param password Password to confirm
     * @return True if password is the correct length
     */
    fun isValidPassword(password: String): Boolean {
        return password.isNotEmpty() && password.length >= MINIMUM_PASSWORD_LENGTH
    }

    /**
     * Method is used to get formatted date and time
     *
     * @return Current date and time
     */
    val currentDateTime: String
        get() {
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT, Locale.US)
            return formatter.format(calendar.time)
        }

    /**
     * Method is used to get formatted date and time in UTC
     *
     * @return Current date and time
     */
    val currentDateTimeUtc: String
        get() {
            val calendar = Calendar.getInstance()
            val formatter = SimpleDateFormat(DEFAULT_TIMESTAMP_FORMAT, Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            return formatter.format(calendar.time)
        }

    /**
     * Method is used to parse formatted date
     *
     * @param date       The date to parse
     * @param dateFormat Method is used to parse formatted date
     * @return Formatted date and time
     * @throws ParseException Thrown when the string being parsed is not in the correct form
     */
    @Throws(ParseException::class)
    fun parseDateTime(date: String, dateFormat: String): Date {
        val formatter = SimpleDateFormat(dateFormat, Locale.US)
        formatter.timeZone = TimeZone.getDefault()
        return formatter.parse(date) as Date
    }

    /**
     * Method is used to control clicks on views. Clicking views repeatedly and quickly will
     * sometime cause crashes when objects and views are not fully animated or instantiated.
     * This helper method helps minimize and control UI interaction and flow
     *
     * @return True if view interaction has not been interacted with for set time
     */
    val isViewClickable: Boolean
        get() {
            /*
             * @Note: Android queues button clicks so it doesn't matter how fast or slow
             * your onClick() executes, simultaneous clicks will still occur. Therefore solutions
             * such as disabling button clicks via flags or conditions statements will not work.
             * The best solution is to timestamp the click processes and return back clicks
             * that occur within a designated window (currently 300 ms)
             */
            val mCurrClickTimestamp = SystemClock.uptimeMillis()
            val mElapsedTimestamp = mCurrClickTimestamp - lastClickTime
            lastClickTime = mCurrClickTimestamp
            return mElapsedTimestamp > CLICK_THRESHOLD
        }

}