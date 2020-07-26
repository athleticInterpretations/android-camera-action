package com.interpretations.athletic.framework.utils.device

import android.content.Context
import android.content.res.Resources
import android.os.IBinder
import android.view.inputmethod.InputMethodManager

object DeviceUtils {

    /**
     * Method is used to show virtual keyboard
     *
     * <p>Regarding  lifecycle, it is ideal to call this in onResume()</p>
     *
     * @param context Interface to global information about an application environment
     */
    fun showKeyboard(context: Context) {
        val imm = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    /**
     * Method is used to hide virtual keyboard
     *
     * <p>Regarding lifecycle, it is ideal to call this in onPause() as opposed to
     * onDetach() or onDestroy()</p>
     *
     * @param context Interface to global information about an application environment
     * @param binder  Base interface for a remotable object, the core part of a lightweight remote
     * procedure call mechanism designed for high performance when performing
     * in-process and cross-process calls
     *
     */
    fun hideKeyboard(context: Context, binder: IBinder) {
        val imm = (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
        imm.hideSoftInputFromWindow(binder, 0)
    }

    /**
     * Method is used to get the device width in pixels
     *
     * @return Return the current display metrics (Width) that are in effect for this resource object
     */
    val deviceWidthPx: Int
        get() {
            val metrics = Resources.getSystem().displayMetrics
            return metrics.widthPixels
        }

    /**
     * Method is used to get the device height in pixels
     *
     * @return Return the current display metrics (Height) that are in effect for this resource object
     */
    val deviceHeightPx: Int
        get() {
            val metrics = Resources.getSystem().displayMetrics
            return metrics.heightPixels
        }
}