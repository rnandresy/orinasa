package com.orinasa.app

import android.app.Application
import com.orinasa.app.utils.NotificationHelper

class OrinasaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper(this).createChannels()
    }
}