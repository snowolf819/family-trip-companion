package com.trip.family.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.trip.family.data.*

fun Context.dialPhone(phone: String) {
    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
}

/**
 * 应急联系人卡片 — 使用 errorContainer 语义颜色
 */
@Composable
fun EmergencyContactCard(contact: EmergencyContact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "📞 应急联系人",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${contact.name}：${contact.phone}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            val context = LocalContext.current
            Button(
                onClick = { context.dialPhone(contact.phone) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("📞 一键呼叫 ${contact.name}", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/**
 * 应急方案卡片 — 使用 tertiaryContainer 语义颜色
 */
@Composable
fun EmergencyPlanCard(plan: EmergencyPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🆘 应急方案", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            if (plan.weather.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("🌦️ 天气突变", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                plan.weather.forEach { option ->
                    Text("  • ${option.detail}", style = MaterialTheme.typography.bodyLarge)
                    option.tips?.forEach { tip ->
                        Text("    💡 $tip", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (plan.health.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("🏥 身体不适", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                plan.health.forEach { option ->
                    Text("  • ${option.detail}", style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (plan.transportDelay.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text("🚄 交通延误", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                plan.transportDelay.forEach { option ->
                    Text("  • ${option.detail}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/**
 * 行程片段卡片
 */
@Composable
fun SegmentCard(segment: TripSegment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(segmentIcon(segment.type), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(8.dp))
                Text(segment.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            segment.subtitle?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // 交通信息
            segment.transport?.let { transport ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🚄 交通", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        if (transport.departureTime != null) {
                            Text("  🕐 ${transport.departureTime}", style = MaterialTheme.typography.bodyLarge)
                        }
                        if (!transport.departureStation.isNullOrBlank() || !transport.arrivalStation.isNullOrBlank()) {
                            Text("  📍 ${transport.departureStation ?: ""} → ${transport.arrivalStation ?: ""}", style = MaterialTheme.typography.bodyLarge)
                        }
                        Text("  🚌 方式：${transport.mode}", style = MaterialTheme.typography.bodyLarge)
                        transport.durationMinutes?.let {
                            Text("  ⏱️ 约${it}分钟", style = MaterialTheme.typography.bodyLarge)
                        }
                        transport.pricePerPerson?.let {
                            Text("  💰 ${it}元/人", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // 公共交通参考
            segment.transportRoute?.let { route ->
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🚇 公共交通参考", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text("  ${route.from} → ${route.to}", style = MaterialTheme.typography.bodyLarge)
                        Text("  ${route.mode}，约${route.durationMinutes}分钟", style = MaterialTheme.typography.bodyLarge)
                        Text("  📋 ${route.route}", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            segment.route?.let {
                Spacer(Modifier.height(4.dp))
                Text("🗺️ 路线：$it", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            segment.mealRecommendation?.let {
                Spacer(Modifier.height(4.dp))
                Text("🍜 $it", style = MaterialTheme.typography.bodyLarge)
            }

            segment.highlights?.let { highlights ->
                if (highlights.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    highlights.forEach { h ->
                        Text("⭐ $h", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // 老年人提示 — 使用 secondaryContainer
            segment.elderlyNotes?.let {
                Spacer(Modifier.height(8.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("👴 老年人建议：$it", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(8.dp))
                }
            }

            // 注意事项 — 使用 errorContainer
            segment.caution?.let { cautions ->
                if (cautions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            cautions.forEach { c ->
                                Text("⚠️ $c", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
            }

            segment.notes?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * 酒店信息卡片 — 使用 primaryContainer
 */
@Composable
fun HotelInfoCard(hotel: HotelCard) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏨", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.width(8.dp))
                Text(hotel.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("★${hotel.stars} · 评分 ${hotel.score}/5.0", style = MaterialTheme.typography.bodyLarge)
            Text("📍 ${hotel.address}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(Modifier.height(8.dp))
            Text("🚇 距车站 ${hotel.stationDistanceKm}km，打车约${hotel.taxiPrice}元", style = MaterialTheme.typography.bodyLarge)
            Text("🚶 距公交站 ${hotel.busStopMeters}米（${hotel.busLines.joinToString("、")}）", style = MaterialTheme.typography.bodyLarge)
            Text("🍜 500米内餐厅${hotel.restaurantsNearby}家，便利店${hotel.storesNearby}家", style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(4.dp))
            Text("💰 ${hotel.priceMin} - ${hotel.priceMax}元/晚", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)

            if (hotel.phone.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { context.dialPhone(hotel.phone) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("📞 拨打酒店电话", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

fun segmentIcon(type: String): String = when (type) {
    "arrive" -> "🛬"
    "intercity" -> "🚄"
    "sightseeing" -> "🏛️"
    "meal" -> "🍜"
    "hotel" -> "🏨"
    "free" -> "🛍️"
    "transport" -> "🚌"
    else -> "📍"
}
