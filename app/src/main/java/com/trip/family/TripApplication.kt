package com.trip.family

import android.app.Application
import com.trip.family.api.TripApi

class TripApplication : Application() {
    lateinit var preferences: TripPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = TripPreferences(this)
        TripApi.init(this)
    }
}
