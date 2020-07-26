package com.interpretations.athletic.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.interpretations.athletic.framework.utils.logger.Logger
import com.interpretations.athletic.framework.utils.network.NetworkUtils
import java.util.*

class NetworkReceiver : BroadcastReceiver() {

    override fun onReceive(
            context: Context,
            intent: Intent
    ) {
        Logger.i(TAG, "onReceive() broadcast")
        val disconnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
        var isNetworkConnectedCurrent = false
        if (!disconnected) {
            isNetworkConnectedCurrent = NetworkUtils.isConnected(context)
        }
        if (isNetworkConnectedCurrent != isNetworkConnected) {
            isNetworkConnected = isNetworkConnectedCurrent
            Logger.d(TAG, "NetworkStatus.onReceive - isNetworkConnected: $isNetworkConnected")
            notifyObservers(isNetworkConnected)
        }
    }

    /**
     * Lets all [NetworkStatusObserver]s know if the device is connected to a network
     *
     * @param isNetworkConnectedCurrent True if device is connected to a network
     */
    private fun notifyObservers(isNetworkConnectedCurrent: Boolean) {
        for (networkStatusObserver in observerList) {
            networkStatusObserver.notifyConnectionChange(isNetworkConnectedCurrent)
        }
    }

    /**
     * Add observer to observer list
     *
     * @param observer List of observers that track network activity
     */
    fun addObserver(observer: NetworkStatusObserver) {
        observerList.add(observer)
    }

    /**
     * Remove observer from observer list
     *
     * @param observer List of observers that track network activity
     */
    fun removeObserver(observer: NetworkStatusObserver) {
        observerList.remove(observer)
    }

    /**
     * Retrieve observer list size
     *
     * @return The observer list size
     */
    val observerSize: Int
        get() = observerList.size

    /**
     * Check if receiver is added to observer list
     *
     * @param observer List of observers that track network activity
     * @return True if receiver is added to observer list
     */
    operator fun contains(observer: NetworkStatusObserver): Boolean {
        return observerList.contains(observer)
    }

    /**
     * Method is used to print observer list
     */
    fun printObserverList() {
        Logger.i(TAG, "===== PRINT OBSERVER LIST ===== ")
        for (i in observerList.indices) {
            Logger.i(TAG, String.format(Locale.US, "item(%d): %s", i, observerList[i].toString()))
        }
    }

    /**
     * Interface for monitoring network status change
     */
    interface NetworkStatusObserver {
        fun notifyConnectionChange(isConnected: Boolean)
    }

    companion object {
        private val TAG = NetworkReceiver::class.java.simpleName
        private val observerList: MutableList<NetworkStatusObserver> = ArrayList()
        private var isNetworkConnected = true
    }
}