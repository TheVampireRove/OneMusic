package com.example

import android.app.Application

import android.util.Log
import com.example.audio.AudioPlaybackManager
import com.example.audio.BluetoothAudioHelper
import com.example.data.MusicRepository

class OneMusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            // Eagerly initialize singletons with ApplicationContext
            // so background services or cold restarts after process termination never encounter null Context
            MusicRepository.getInstance().initContext(this)
            AudioPlaybackManager.getInstance().initContext(this)
            BluetoothAudioHelper.init(this)
            // Initialize diagnostic crash report capture pipeline
            com.example.diagnostics.CrashReportManager.init(this)
        } catch (e: Exception) {
            Log.e("OneMusicApp", "Error during early application initialization", e)
        }
    }
}
