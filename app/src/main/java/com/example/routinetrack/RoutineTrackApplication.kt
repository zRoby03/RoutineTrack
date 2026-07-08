package com.example.routinetrack

import android.app.Application
import com.example.routinetrack.data.AppContainer

class RoutineTrackApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
