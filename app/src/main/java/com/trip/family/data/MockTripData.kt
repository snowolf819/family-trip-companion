package com.trip.family.data

import android.content.Context
import com.google.gson.Gson

object MockTripData {
    fun getSampleTrip(context: Context): Trip {
        val json = context.assets.open("mock_trip.json").bufferedReader().readText()
        return Gson().fromJson(json, Trip::class.java)
    }
}
