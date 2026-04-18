package com.familytrip.companion.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Trip(
    @Json(name = "trip_id") val tripId: String,
    @Json(name = "title") val title: String,
    @Json(name = "date_range") val dateRange: DateRange,
    @Json(name = "travelers") val travelers: Travelers,
    @Json(name = "preferences") val preferences: Preferences? = null,
    @Json(name = "days") val days: List<TripDay>,
    @Json(name = "emergency_contact") val emergencyContact: EmergencyContact? = null,
    @Json(name = "parent_share_token") val parentShareToken: String,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class DateRange(
    @Json(name = "start") val start: String,
    @Json(name = "end") val end: String
)

@JsonClass(generateAdapter = true)
data class Travelers(
    @Json(name = "type") val type: String,
    @Json(name = "count") val count: Int,
    @Json(name = "avg_age") val avgAge: Int
)

@JsonClass(generateAdapter = true)
data class Preferences(
    @Json(name = "budget_level") val budgetLevel: String? = null,
    @Json(name = "pace") val pace: String? = null
)

@JsonClass(generateAdapter = true)
data class TripDay(
    @Json(name = "id") val id: String,
    @Json(name = "day_number") val dayNumber: Int,
    @Json(name = "date") val date: String,
    @Json(name = "city") val city: String,
    @Json(name = "summary") val summary: List<String> = emptyList(),
    @Json(name = "segments") val segments: List<TripSegment> = emptyList(),
    @Json(name = "hotel_card") val hotelCard: HotelCard? = null,
    @Json(name = "emergency_plan") val emergencyPlan: EmergencyPlan? = null
)

@JsonClass(generateAdapter = true)
data class TripSegment(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String,
    @Json(name = "subtitle") val subtitle: String? = null,
    @Json(name = "transport") val transport: TransportInfo? = null,
    @Json(name = "route") val route: String? = null,
    @Json(name = "hotel") val hotel: String? = null,
    @Json(name = "meal_recommendation") val mealRecommendation: String? = null,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "highlights") val highlights: List<String>? = null,
    @Json(name = "walking_distance_meters") val walkingDistanceMeters: Int? = null,
    @Json(name = "elderly_notes") val elderlyNotes: String? = null,
    @Json(name = "caution") val caution: List<String>? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "ticket_price") val ticketPrice: Double? = null,
    @Json(name = "meal_price") val mealPrice: Double? = null
)

@JsonClass(generateAdapter = true)
data class TransportInfo(
    @Json(name = "mode") val mode: String,
    @Json(name = "departure_station") val departureStation: String? = null,
    @Json(name = "arrival_station") val arrivalStation: String? = null,
    @Json(name = "departure_time") val departureTime: String? = null,
    @Json(name = "duration_minutes") val durationMinutes: Int? = null,
    @Json(name = "price_per_person") val pricePerPerson: Double? = null,
    @Json(name = "elderly_notes") val elderlyNotes: String? = null,
    @Json(name = "board_point") val boardPoint: String? = null
)

@JsonClass(generateAdapter = true)
data class HotelCard(
    @Json(name = "hotel_id") val hotelId: String,
    @Json(name = "name") val name: String,
    @Json(name = "stars") val stars: Int,
    @Json(name = "score") val score: Double,
    @Json(name = "address") val address: String,
    @Json(name = "station_distance_km") val stationDistanceKm: Double,
    @Json(name = "taxi_price") val taxiPrice: Double,
    @Json(name = "bus_stop_meters") val busStopMeters: Int,
    @Json(name = "bus_lines") val busLines: List<String> = emptyList(),
    @Json(name = "restaurants_within_500m") val restaurantsWithin500m: Int,
    @Json(name = "convenience_stores_within_500m") val convenienceStoresWithin500m: Int,
    @Json(name = "price_min") val priceMin: Double,
    @Json(name = "price_max") val priceMax: Double,
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class EmergencyPlan(
    @Json(name = "weather") val weather: List<EmergencyOption> = emptyList(),
    @Json(name = "health") val health: List<EmergencyOption> = emptyList(),
    @Json(name = "transport_delay") val transportDelay: List<EmergencyOption> = emptyList()
)

@JsonClass(generateAdapter = true)
data class EmergencyOption(
    @Json(name = "title") val title: String,
    @Json(name = "detail") val detail: String,
    @Json(name = "tips") val tips: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class EmergencyContact(
    @Json(name = "name") val name: String,
    @Json(name = "phone") val phone: String
)

@JsonClass(generateAdapter = true)
data class PackingList(
    @Json(name = "trip_id") val tripId: String,
    @Json(name = "categories") val categories: List<PackingCategory> = emptyList(),
    @Json(name = "generated_at") val generatedAt: String? = null,
    @Json(name = "weather_note") val weatherNote: String? = null
)

@JsonClass(generateAdapter = true)
data class PackingCategory(
    @Json(name = "name") val name: String,
    @Json(name = "items") val items: List<PackingItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class PackingItem(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "essential") val essential: Boolean = false,
    @Json(name = "condition") val condition: String? = null,
    @Json(name = "checked") val checked: Boolean? = null
)

data class HistoryItem(
    val token: String,
    val title: String,
    val timestamp: Long
)
