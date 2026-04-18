package com.familytrip.companion.data.repository

import com.familytrip.companion.data.api.TripApiService
import com.familytrip.companion.data.model.PackingList
import com.familytrip.companion.data.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class TripRepository(private val apiService: TripApiService) {

    suspend fun getTripByToken(token: String): Result<Trip> = withContext(Dispatchers.IO) {
        withTimeoutOrNull(15_000L) {
            try {
                Result.success(apiService.getTripByToken(token))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } ?: Result.failure(Exception("请求超时，请检查网络"))
    }

    suspend fun getPackingList(tripId: String): Result<PackingList> = withContext(Dispatchers.IO) {
        withTimeoutOrNull(15_000L) {
            try {
                Result.success(apiService.getPackingList(tripId))
            } catch (e: Exception) {
                Result.failure(e)
            }
        } ?: Result.failure(Exception("请求超时，请检查网络"))
    }
}
