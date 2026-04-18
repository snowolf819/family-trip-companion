package com.familytrip.companion.ui.day

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.familytrip.companion.R
import com.familytrip.companion.data.model.EmergencyPlan
import com.familytrip.companion.data.model.HotelCard
import com.familytrip.companion.data.model.TripSegment
import com.familytrip.companion.ui.theme.CautionColor
import com.familytrip.companion.ui.theme.ElderlyTipColor
import com.familytrip.companion.ui.theme.HighlightColor
import com.familytrip.companion.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(viewModel: TripViewModel, onBack: () -> Unit) {
    val day by viewModel.selectedDay.collectAsState()
    if (day == null) { onBack(); return }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第 ${day!!.dayNumber} 天 . ${day!!.city}") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Text(day!!.date, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            if (day!!.summary.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) { day!!.summary.forEach { line -> Text(line, style = MaterialTheme.typography.bodyLarge) } }
                    }
                }
            }
            items(day!!.segments) { segment -> SegmentCard(segment) }
            day!!.hotelCard?.let { item { HotelSectionCard(it) } }
            day!!.emergencyPlan?.let { item { EmergencyPlanSection(it) } }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
private fun SegmentCard(segment: TripSegment) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = segmentTypeIcon(segment.type), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(segment.title, style = MaterialTheme.typography.titleMedium)
                    segment.subtitle?.let { Text(it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            segment.transport?.let { transport ->
                Spacer(modifier = Modifier.height(8.dp))
                val info = buildString {
                    append(transport.mode)
                    transport.departureStation?.let { append(" . $it") }
                    transport.arrivalStation?.let { append(" -> $it") }
                    transport.durationMinutes?.let { append(" . ${it}分钟") }
                    transport.pricePerPerson?.let { append(" . Y$it") }
                }
                Text(info, style = MaterialTheme.typography.bodyLarge)
                transport.elderlyNotes?.let { Text("长辈提示：$it", color = ElderlyTipColor, style = MaterialTheme.typography.bodyLarge) }
            }
            segment.walkingDistanceMeters?.let { if (it > 0) { Spacer(modifier = Modifier.height(4.dp)); Text("步行距离：${it}米", style = MaterialTheme.typography.bodyLarge) } }
            segment.ticketPrice?.let { if (it > 0) Text("门票：Y$it", style = MaterialTheme.typography.bodyLarge) }
            segment.mealPrice?.let { if (it > 0) Text("人均：Y$it", style = MaterialTheme.typography.bodyLarge) }
            segment.mealRecommendation?.let { Spacer(modifier = Modifier.height(4.dp)); Text("推荐：$it", style = MaterialTheme.typography.bodyLarge) }
            segment.notes?.let { Spacer(modifier = Modifier.height(4.dp)); Text(it, style = MaterialTheme.typography.bodyLarge) }
            segment.elderlyNotes?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = ElderlyTipColor.copy(alpha = 0.15f)), shape = RoundedCornerShape(12.dp)) {
                    Text(it, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyLarge, color = ElderlyTipColor)
                }
            }
            segment.caution?.takeIf { it.isNotEmpty() }?.let { cautions ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(containerColor = CautionColor.copy(alpha = 0.15f)), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.caution), color = CautionColor, style = MaterialTheme.typography.titleSmall)
                        cautions.forEach { c -> Text(" . $c", color = CautionColor, style = MaterialTheme.typography.bodyLarge) }
                    }
                }
            }
            segment.highlights?.takeIf { it.isNotEmpty() }?.let { highlights ->
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    highlights.forEach { h ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(h) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = HighlightColor.copy(alpha = 0.15f), labelColor = HighlightColor)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HotelSectionCard(hotel: HotelCard) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Hotel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(hotel.name, style = MaterialTheme.typography.titleMedium)
                    Text("⭐".repeat(hotel.stars) + " ${hotel.score}分", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("地址：${hotel.address}", style = MaterialTheme.typography.bodyLarge)
            Text("距地铁站：${hotel.stationDistanceKm}km", style = MaterialTheme.typography.bodyLarge)
            if (hotel.busLines.isNotEmpty()) Text("公交：${hotel.busLines.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge)
            Text("参考价：Y${hotel.priceMin}~${hotel.priceMax}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun EmergencyPlanSection(plan: EmergencyPlan) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.emergency_plan), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null) }
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    if (plan.weather.isNotEmpty()) { Text("天气应急：", style = MaterialTheme.typography.titleSmall); plan.weather.forEach { opt -> Text(" . ${opt.title}：${opt.detail}", style = MaterialTheme.typography.bodyLarge); opt.tips?.forEach { t -> Text("  -> $t", style = MaterialTheme.typography.bodyMedium) } } }
                    if (plan.health.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); Text("健康应急：", style = MaterialTheme.typography.titleSmall); plan.health.forEach { opt -> Text(" . ${opt.title}：${opt.detail}", style = MaterialTheme.typography.bodyLarge) } }
                    if (plan.transportDelay.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); Text("交通应急：", style = MaterialTheme.typography.titleSmall); plan.transportDelay.forEach { opt -> Text(" . ${opt.title}：${opt.detail}", style = MaterialTheme.typography.bodyLarge) } }
                }
            }
        }
    }
}

private fun segmentTypeIcon(type: String): ImageVector = when (type) {
    "arrive", "intercity" -> Icons.Default.Flight
    "sightseeing" -> Icons.Default.PhotoCamera
    "meal" -> Icons.Default.Restaurant
    "hotel" -> Icons.Default.Hotel
    "transport" -> Icons.Default.DirectionsBus
    "free" -> Icons.Default.SelfImprovement
    else -> Icons.Default.Place
}
