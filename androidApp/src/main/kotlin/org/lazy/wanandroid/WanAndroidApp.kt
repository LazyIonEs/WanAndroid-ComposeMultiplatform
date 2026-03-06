package org.lazy.wanandroid

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.lazy.wanandroid.di.initKoin

class WanAndroidApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@WanAndroidApp)
        }
    }
}