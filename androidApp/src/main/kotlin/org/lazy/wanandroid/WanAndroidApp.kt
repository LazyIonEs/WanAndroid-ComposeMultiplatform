package org.lazy.wanandroid

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.lazy.wanandroid.di.initKoin

class WanAndroidApp : Application() {

    companion object {
        lateinit var instance: WanAndroidApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initKoin {
            androidContext(this@WanAndroidApp)
            androidLogger()
        }
    }
}