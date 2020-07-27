package com.interpretations.athletic.activity

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import com.interpretations.athletic.R
import com.interpretations.athletic.databinding.ActivityMainBinding
import com.interpretations.athletic.framework.base.BaseActivity
import com.interpretations.athletic.framework.utils.FrameworkUtils
import com.interpretations.athletic.network.NetworkReceiver
import com.interpretations.athletic.utils.DialogUtils

@Suppress("DEPRECATION")
class MainActivity : BaseActivity(), NetworkReceiver.NetworkStatusObserver {
    // view binding and layout widgets
    // this property is only valid between onCreateView and onDestroyView
    private lateinit var binding: ActivityMainBinding

    // network receiver
    private val networkReceiver: NetworkReceiver = NetworkReceiver()

    // dialog
    private val dialog: DialogUtils = DialogUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun notifyConnectionChange(isConnected: Boolean) {
        if (isConnected) {
            // app is connected to network
            dialog.dismissNoNetworkDialog()
        } else { // app is not connected to network
            dialog.showDefaultNoNetworkAlert(this, null,
                resources.getString(R.string.check_network))
        }
    }

    override fun onResume() {
        super.onResume()
        // print info
        FrameworkUtils.printInfo(this)
        // only register receiver if it has not already been registered
        if (!networkReceiver.contains(this)) {
            // register network receiver
            networkReceiver.addObserver(this)
            registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            // print observer list
            networkReceiver.printObserverList()
        }
    }

    override fun onDestroy() {
        // remove network observer
        val observerSize = networkReceiver.observerSize
        if (observerSize > 0 && networkReceiver.contains(this)) {
            try { // unregister network receiver
                unregisterReceiver(networkReceiver)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
            networkReceiver.removeObserver(this)
        }
        super.onDestroy()
    }
}