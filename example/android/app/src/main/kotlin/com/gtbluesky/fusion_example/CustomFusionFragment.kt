package com.gtbluesky.fusion_example

import android.util.Log
import android.widget.Toast
import com.gtbluesky.fusion.channel.FusionMessengerProvider
import com.gtbluesky.fusion.container.FusionFragment
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel

class CustomFusionFragment : FusionFragment(), FusionMessengerProvider {

    private var channel: MethodChannel? = null

    override fun configureFlutterChannel(binaryMessenger: BinaryMessenger) {
        Log.d(this.toString(), "configureFlutterChannel")
        channel = MethodChannel(binaryMessenger, "custom_channel")
        channel?.setMethodCallHandler { call, result ->
            Toast.makeText(context, "Custom Channel：${this}_${call.method}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun releaseFlutterChannel() {
        Log.d(this.toString(), "releaseFlutterChannel")
        channel?.setMethodCallHandler(null)
        channel = null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(this::class.java.simpleName, "onDestroy")
    }
}