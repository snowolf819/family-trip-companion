package com.familytrip.companion.ui.day

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familytrip.companion.data.model.*
import com.familytrip.companion.data.model.SegmentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    viewModel: com.familytrip.companion.viewmodel.TripViewModel,
    dayIndex: Int,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val day = uiState.trip?.days?.getOrNull(dayIndex)
    val weather = day?.let { uiState.weather.find { w -> w.date == it.date } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(day?.let { "第${it.dayNumber}天 · ${it.city}" } ?: "行程详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (day == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无数据", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Date header
            item {
                Text(day.date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (day.summary.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    day.summary.forEach { s ->
                        Text("• $s", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Weather card
            weather?.let { w ->
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(w.icon, style = MaterialTheme.typography.headlineMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(w.textDay, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("${w.tempMin}°C ~ ${w.tempMax}°C", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${w.windDir} ${w.windScale}级", style = MaterialTheme.typography.bodyMedium)
                                Text("湿度 ${w.humidity}%", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Segments
            items(day.segments) { segment ->
                SegmentCard(segment)
            }

            // Hotel card
            day.hotelCard?.let { hotel ->
                item {
                    HotelCardComposable(hotel)
                }
            }

            // Emergency plan
            day.emergencyPlan?.let { plan ->
                item {
                    EmergencyPlanCard(plan)
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

private val segmentIcon: (SegmentType) -> androidx.compose.ui.graphics.vector.ImageVector = { type ->
    when (type) {
        SegmentType.ARRIVE -> Icons.Default.FlightLand
        SegmentType.INTERCITY -> Icons.Default.Train
        SegmentType.SIGHTSEEING -> Icons.Default.CameraAlt
        SegmentType.MEAL -> Icons.Default.Restaurant
        SegmentType.HOTEL -> Icons.Default.Hotel
        SegmentType.FREE -> Icons.Default.FreeBreakfast
        SegmentType.TRANSPORT -> Icons.Default.DirectionsBus
        else -> Icons.Default.Place
    }
}

@Composable
private fun SegmentCard(segment: TripSegment) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    segmentIcon(segment.type),
                    contentDescription = segment.type.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(segment.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    segment.subtitle?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Transport info
            segment.transport?.let { t ->
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(Icons.Default.Schedule, contentDescription = "时间", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    val info = buildString {
                        t.departureTime?.let { append(it) }
                        t.durationMinutes?.let { append(" · ${it}分钟") }
                        t.departureStation?.let { append(" · $it") }
                        t.arrivalStation?.let { append(" → $it") }
                    }
                    Text(info, style = MaterialTheme.typography.bodyMedium)
                }
                t.pricePerPerson?.let { price ->
                    Row {
                        Icon(Icons.Default.AttachMoney, contentDescription = "价格", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("¥${price.toInt()}/人", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Transport route (intercity route info)
            segment.transportRoute?.let { route ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("🚄 城际交通: ${route.from} → ${route.to}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Text("方式: ${route.mode} · ${route.durationMinutes}分钟 · ¥${route.price.toInt()}", style = MaterialTheme.typography.bodyMedium)
                        if (route.route.isNotBlank()) {
                            Text("路线: ${route.route}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Walking distance
            segment.walkingDistanceMeters?.let { dist ->
                if (dist > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(Icons.Default.DirectionsWalk, contentDescription = "步行距离", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("步行${dist}米", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Ticket/meal price
            segment.ticketPrice?.let { price ->
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Icon(Icons.Default.ConfirmationNumber, contentDescription = "门票", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("门票 ¥${price.toInt()}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            segment.mealPrice?.let { price ->
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Icon(Icons.Default.Restaurant, contentDescription = "用餐", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("人均 ¥${price.toInt()}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            // Highlights
            segment.highlights?.takeIf { it.isNotEmpty() }?.let { highlights ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    highlights.take(3).forEach { h ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(h, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            // Elderly notes
            segment.elderlyNotes?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = com.familytrip.companion.ui.theme.ElderlyBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Icon(Icons.Default.Accessible, contentDescription = "适老提示", tint = com.familytrip.companion.ui.theme.ElderlyTip, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(note, style = MaterialTheme.typography.bodyMedium, color = com.familytrip.companion.ui.theme.ElderlyTip)
                    }
                }
            }

            // Caution
            segment.caution?.takeIf { it.isNotEmpty() }?.let { cautions ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = com.familytrip.companion.ui.theme.CautionBg),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row {
                            Icon(Icons.Default.Warning, contentDescription = "警告", tint = com.familytrip.companion.ui.theme.Warning, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("注意事项", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = com.familytrip.companion.ui.theme.Warning)
                        }
                        cautions.forEach { c ->
                            Text("⚠️ $c", style = MaterialTheme.typography.bodyMedium, color = com.familytrip.companion.ui.theme.Warning)
                        }
                    }
                }
            }

            // Notes
            segment.notes?.takeIf { it.isNotBlank() }?.let { note ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HotelCardComposable(hotel: HotelCard) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hotel, contentDescription = "酒店", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(hotel.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.weight(1f))
                Text("${hotel.stars}星 · ${hotel.score}分", style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("📍 ${hotel.address}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("💰 ¥${hotel.priceMin.toInt()} ~ ¥${hotel.priceMax.toInt()}", style = MaterialTheme.typography.bodyLarge)
            if (hotel.stationDistanceKm > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("🚇 距地铁${hotel.stationDistanceKm}km · 打车约¥${hotel.taxiPrice.toInt()}", style = MaterialTheme.typography.bodyMedium)
            }
            if (hotel.busLines.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("🚌 公交: ${hotel.busLines.joinToString(", ")}", style = MaterialTheme.typography.bodyMedium)
            }
            if (hotel.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("📞 ${hotel.phone}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun EmergencyPlanCard(plan: EmergencyPlan) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🆘 应急方案", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                if (plan.weather.isNotEmpty()) {
                    Text("🌧 天气应急", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    plan.weather.forEach { opt ->
                        Text("• ${opt.title}: ${opt.detail}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (plan.health.isNotEmpty()) {
                    Text("🏥 健康应急", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    plan.health.forEach { opt ->
                        Text("• ${opt.title}: ${opt.detail}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                if (plan.transportDelay.isNotEmpty()) {
                    Text("🚄 交通延误", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    plan.transportDelay.forEach { opt ->
                        Text("• ${opt.title}: ${opt.detail}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
