package com.steelbytes.media3test

import android.app.Application
import android.content.Context

class MyApp : Application() {

    companion object {
        lateinit var app: Application
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        app = this
    }
}