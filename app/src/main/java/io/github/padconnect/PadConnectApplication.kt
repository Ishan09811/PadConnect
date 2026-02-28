
package io.github.padconnect

import android.app.Application
import android.content.Context

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
        instance = this
    }
}