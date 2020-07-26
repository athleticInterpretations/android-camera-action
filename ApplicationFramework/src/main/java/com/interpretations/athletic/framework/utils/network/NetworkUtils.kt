package com.interpretations.athletic.framework.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.interpretations.athletic.framework.utils.logger.Logger

object NetworkUtils {
    private val TAG = NetworkUtils::class.java.simpleName

    /**
     * Method [isConnected] is used to check is network is available e.g.
     * both connected and available.
     *
     * @param context Interface to global information about an application environment
     * @return True if network is available, otherwise false
     */
    fun isConnected(context: Context): Boolean {
        // Class that answers queries about the state of network connectivity.
        // It also notifies applications when network connectivity changes.
        val connectivityManager = context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        connectivityManager.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)?.run {
                    // Returns a Network object corresponding to the currently active default
                    // data network. In the event that the current active default data network
                    // disconnects, the returned Network object will no longer be usable.
                    // This will return null when there is no default network.
                    return hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                }
            } else {
                // Returns details about the currently active default data network.
                // When connected, this network is the default route for outgoing connections.
                // You should always check NetworkInfo.isConnected() before initiating network
                // traffic. This may return null when there is no default network. Note that if
                // the default network is a VPN, this method will return the NetworkInfo for
                // one of its underlying networks instead, or null if the VPN agent did
                // not specify any.
                return connectivityManager.activeNetworkInfo != null &&
                        connectivityManager.activeNetworkInfo?.isConnected == true
            }
        }
        Logger.i(TAG, "No Connection")
        return false
    }
}