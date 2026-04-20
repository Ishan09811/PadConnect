
package io.github.padconnect

import android.app.Application
import android.content.Context
import io.github.padconnect.utils.settings.GlobalConfig

class PadConnectApplication : Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance : PadConnectApplication
            private set

        val context : Context get() = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        GlobalConfig.init(this)
        instance = this
    }
}