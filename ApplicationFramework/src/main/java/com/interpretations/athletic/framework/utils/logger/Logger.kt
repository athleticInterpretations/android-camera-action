package com.interpretations.athletic.framework.utils.logger

import android.util.Log
import com.interpretations.athletic.framework.constants.Constants

object Logger {

    /**
     * Helper method for logging e-verbose
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs
     * @param msg The message you would like log
     */
    fun e(tag: String, msg: String) {
        if (Constants.DEBUG) {
            Log.e(tag, msg)
        }
    }

    /**
     * Helper method for logging e-verbose
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs
     * @param msg The message you would like log
     * @param exception Exception is the superclass of all classes that represent recoverable exceptions
     */
    fun e(tag: String, msg: String, exception: Exception) {
        if (Constants.DEBUG) {
            Log.e(tag, msg, exception)
        }
    }

    /**
     * Helper method for logging d-verbose
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs
     * @param msg The message you would like log
     */
    fun d(tag: String, msg: String) {
        if (Constants.DEBUG) {
            Log.d(tag, msg)
        }
    }

    /**
     * Helper method for logging i-verbose
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs
     * @param msg The message you would like log
     */
    fun i(tag: String, msg: String) {
        if (Constants.DEBUG) {
            Log.i(tag, msg)
        }
    }

    /**
     * Helper method for logging v-verbose
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs
     * @param msg The message you would like log
     */
    fun v(tag: String, msg: String) {
        if (Constants.DEBUG) {
            Log.v(tag, msg)
        }
    }

    /**
     * Helper method for logging w-verbose
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or
     * activity where the log call occurs
     * @param msg The message you would like log
     */
    fun w(tag: String, msg: String) {
        if (Constants.DEBUG) {
            Log.w(tag, msg)
        }
    }

    /**
     * Helper method to display data on Console
     *
     * @param msg message to be displayed
     */
    fun printOnConsole(msg: String) {
        if (Constants.DEBUG) {
            println(msg)
        }
    }
}