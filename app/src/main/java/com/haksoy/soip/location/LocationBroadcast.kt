package com.haksoy.soip.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.CallSuper

abstract class LocationBroadcast : BroadcastReceiver() {
    @CallSuper
    override fun onReceive(context: Context, intent: Intent) {
    }
}