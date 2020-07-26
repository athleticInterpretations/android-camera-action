package com.interpretations.athletic.activity

import android.os.Bundle
import com.interpretations.athletic.R
import com.interpretations.athletic.databinding.ActivityMainBinding
import com.interpretations.athletic.framework.base.BaseActivity
import com.interpretations.athletic.network.NetworkReceiver
import com.interpretations.athletic.utils.DialogUtils

class MainActivity : BaseActivity(), NetworkReceiver.NetworkStatusObserver {
    // view binding and layout widgets
    // this property is only valid between onCreateView and onDestroyView
    private lateinit var binding: ActivityMainBinding

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
}