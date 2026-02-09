package com.example.pennypilot

// In your Application class (e.g., MyApplication.kt)
import android.app.Application
import com.github.mikephil.charting.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

//package com.example.pennypilot
//
//import android.app.Application
//import com.example.pennypilot.di.appModule
//import com.example.pennypilot.di.viewModelModule
//import org.koin.android.ext.koin.androidContext
//import org.koin.android.ext.koin.androidLogger
//import org.koin.core.context.startKoin
//
//class MyApplication : Application() {
//    override fun onCreate() {
//        super.onCreate()
//
//        startKoin {
//            // Log Koin activity (optional, but helpful for debugging)
//            androidLogger()
//            // Provide the Android context to Koin
//            androidContext(this@MyApplication)
//            // Load your modules
//            modules(appModule, viewModelModule)
//        }
//    }
//}