package com.familytrip.companion.data.repository

import com.familytrip.companion.data.api.TripApiService
import com.familytrip.companion.data.model.PackingList
import com.familytrip.companion.data.model.Trip
import com.familytrip.companion.data.model.WeatherInfo

class TripRepository(private val api: TripApiService) {

    suspend fun getTripByToken(token: String): Result<Trip> = runCatching {
        api.getTripByShareToken(token)
    }

    suspend fun getPackingList(tripId: String): Result<PackingList> = runCatching {
        api.getPackingList(tripId)
    }

    suspend fun getWeather(tripId: String): Result<List<WeatherInfo>> = runCatching {
        api.getWeather(tripId)
    }
}
