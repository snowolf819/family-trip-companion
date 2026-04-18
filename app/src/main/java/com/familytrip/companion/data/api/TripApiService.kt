package com.familytrip.companion.data.api

import com.familytrip.companion.data.model.PackingList
import com.familytrip.companion.data.model.Trip
import retrofit2.http.GET
import retrofit2.http.Path

interface TripApiService {
    @GET("api/trips/share/{token}")
    suspend fun getTripByToken(@Path("token") token: String): Trip

    @GET("api/trips/{tripId}/packing")
    suspend fun getPackingList(@Path("tripId") tripId: String): PackingList
}
