package com.trip.family.data

import com.google.gson.annotations.SerializedName

// ===== 适老化精简模型，只保留父母端需要展示的字段 =====

data class Trip(
    @SerializedName("trip_id") val tripId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("date_range") val dateRange: DateRange = DateRange(),
    @SerializedName("days") val days: List<TripDay> = emptyList(),
    @SerializedName("emergencyContact") val emergencyContact: EmergencyContact = EmergencyContact()
)

data class DateRange(
    @SerializedName("start") val start: String = "",
    @SerializedName("end") val end: String = ""
)

data class TripDay(
    @SerializedName("id") val id: String = "",
    @SerializedName("day_number") val dayNumber: Int = 0,
    @SerializedName("date") val date: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("summary") val summary: List<String> = emptyList(),
    @SerializedName("segments") val segments: List<TripSegment> = emptyList(),
    @SerializedName("hotelCard") val hotelCard: HotelCard? = null,
    @SerializedName("emergencyPlan") val emergencyPlan: EmergencyPlan? = null
)

data class TripSegment(
    @SerializedName("id") val id: String = "",
    @SerializedName("type") val type: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("transport") val transport: TransportInfo? = null,
    @SerializedName("transportRoute") val transportRoute: TransportRoute? = null,
    @SerializedName("route") val route: String? = null,
    @SerializedName("meal_recommendation") val mealRecommendation: String? = null,
    @SerializedName("elderly_notes") val elderlyNotes: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("highlights") val highlights: List<String>? = null,
    @SerializedName("caution") val caution: List<String>? = null
)

data class TransportInfo(
    @SerializedName("mode") val mode: String = "",
    @SerializedName("departure_station") val departureStation: String? = null,
    @SerializedName("arrival_station") val arrivalStation: String? = null,
    @SerializedName("departure_time") val departureTime: String? = null,
    @SerializedName("duration_minutes") val durationMinutes: Int? = null,
    @SerializedName("price_per_person") val pricePerPerson: Number? = null,
    @SerializedName("board_point") val boardPoint: String? = null
)

data class TransportRoute(
    @SerializedName("from") val from: String = "",
    @SerializedName("to") val to: String = "",
    @SerializedName("mode") val mode: String = "",
    @SerializedName("duration_minutes") val durationMinutes: Int = 0,
    @SerializedName("price") val price: Number = 0,
    @SerializedName("route") val route: String = ""
)

data class HotelCard(
    @SerializedName("name") val name: String = "",
    @SerializedName("stars") val stars: Int = 0,
    @SerializedName("score") val score: Double = 0.0,
    @SerializedName("address") val address: String = "",
    @SerializedName("phone") val phone: String = "",
    @SerializedName("stationDistanceKm") val stationDistanceKm: Double = 0.0,
    @SerializedName("taxiPrice") val taxiPrice: Number = 0,
    @SerializedName("busStopMeters") val busStopMeters: Int = 0,
    @SerializedName("busLines") val busLines: List<String> = emptyList(),
    @SerializedName("restaurantsWithin500m") val restaurantsNearby: Int = 0,
    @SerializedName("convenienceStoresWithin500m") val storesNearby: Int = 0,
    @SerializedName("priceMin") val priceMin: Number = 0,
    @SerializedName("priceMax") val priceMax: Number = 0
)

data class EmergencyContact(
    @SerializedName("name") val name: String = "",
    @SerializedName("phone") val phone: String = ""
)

data class EmergencyOption(
    @SerializedName("title") val title: String = "",
    @SerializedName("detail") val detail: String = "",
    @SerializedName("tips") val tips: List<String>? = null
)

data class EmergencyPlan(
    @SerializedName("weather") val weather: List<EmergencyOption> = emptyList(),
    @SerializedName("health") val health: List<EmergencyOption> = emptyList(),
    @SerializedName("transportDelay") val transportDelay: List<EmergencyOption> = emptyList()
)
