package com.trip.family

import android.app.Application

class TripApplication : Application() {
    lateinit var preferences: TripPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = TripPreferences(this)
    }
}
