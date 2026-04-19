package com.familytrip.companion.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Trip(
    @SerialName("trip_id") val tripId: String = "",
    val title: String = "",
    @SerialName("date_range") val dateRange: DateRange = DateRange(),
    val travelers: Travelers = Travelers(),
    val days: List<TripDay> = emptyList(),
    @SerialName("emergency_contact") val emergencyContact: EmergencyContact = EmergencyContact(),
    @SerialName("parent_share_token") val parentShareToken: String = "",
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

@Serializable
data class DateRange(val start: String = "", val end: String = "")

@Serializable
data class Travelers(val type: String = "", val count: Int = 0, @SerialName("avg_age") val avgAge: Int = 0)

@Serializable
data class TripDay(
    val id: String = "",
    @SerialName("day_number") val dayNumber: Int = 0,
    val date: String = "",
    val city: String = "",
    val summary: List<String> = emptyList(),
    val segments: List<TripSegment> = emptyList(),
    @SerialName("hotel_card") val hotelCard: HotelCard? = null,
    @SerialName("emergency_plan") val emergencyPlan: EmergencyPlan? = null
)

@Serializable
data class TripSegment(
    val id: String = "",
    val type: String = "",
    val title: String = "",
    val subtitle: String? = null,
    val transport: TransportInfo? = null,
    val route: String? = null,
    val hotel: String? = null,
    @SerialName("meal_recommendation") val mealRecommendation: String? = null,
    val notes: String? = null,
    val highlights: List<String>? = null,
    @SerialName("walking_distance_meters") val walkingDistanceMeters: Int? = null,
    @SerialName("elderly_notes") val elderlyNotes: String? = null,
    val caution: List<String>? = null,
    val city: String? = null,
    @SerialName("ticket_price") val ticketPrice: Double? = null,
    @SerialName("meal_price") val mealPrice: Double? = null
)

@Serializable
data class TransportInfo(
    val mode: String = "",
    @SerialName("departure_station") val departureStation: String? = null,
    @SerialName("arrival_station") val arrivalStation: String? = null,
    @SerialName("departure_time") val departureTime: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int? = null,
    @SerialName("price_per_person") val pricePerPerson: Double? = null,
    @SerialName("elderly_notes") val elderlyNotes: String? = null,
    @SerialName("board_point") val boardPoint: String? = null
)

@Serializable
data class HotelCard(
    @SerialName("hotel_id") val hotelId: String = "",
    val name: String = "",
    val stars: Int = 0,
    val score: Double = 0.0,
    val address: String = "",
    @SerialName("station_distance_km") val stationDistanceKm: Double = 0.0,
    @SerialName("taxi_price") val taxiPrice: Double = 0.0,
    @SerialName("bus_stop_meters") val busStopMeters: Int = 0,
    @SerialName("bus_lines") val busLines: List<String> = emptyList(),
    @SerialName("restaurants_within_500m") val restaurantsWithin500m: Int = 0,
    @SerialName("convenience_stores_within_500m") val convenienceStoresWithin500m: Int = 0,
    @SerialName("price_min") val priceMin: Double = 0.0,
    @SerialName("price_max") val priceMax: Double = 0.0,
    val phone: String = ""
)

@Serializable
data class EmergencyPlan(
    val weather: List<EmergencyOption> = emptyList(),
    val health: List<EmergencyOption> = emptyList(),
    @SerialName("transport_delay") val transportDelay: List<EmergencyOption> = emptyList()
)

@Serializable
data class EmergencyOption(val title: String = "", val detail: String = "", val tips: List<String>? = null)

@Serializable
data class EmergencyContact(val name: String = "", val phone: String = "")

@Serializable
data class PackingList(
    @SerialName("trip_id") val tripId: String = "",
    val categories: List<PackingCategory> = emptyList(),
    @SerialName("generated_at") val generatedAt: String = "",
    @SerialName("weather_note") val weatherNote: String = ""
)

@Serializable
data class PackingCategory(val name: String = "", val items: List<PackingItem> = emptyList())

@Serializable
data class PackingItem(
    val id: String = "",
    val name: String = "",
    val essential: Boolean = false,
    val condition: String? = null,
    val checked: Boolean? = null
)

@Serializable
data class TripHistory(
    @SerialName("token") val token: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("date_range") val dateRange: String = "",
    @SerialName("viewed_at") val viewedAt: Long = 0L
)
