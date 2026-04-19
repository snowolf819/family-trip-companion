package com.familytrip.companion.ui.overview

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familytrip.companion.data.model.*
import java.time.LocalDate
import com.familytrip.companion.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripOverviewScreen(
    viewModel: TripViewModel,
    onNavigateToDay: (Int) -> Unit,
    onNavigateToPacking: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val trip = uiState.trip
    val context = LocalContext.current

    LaunchedEffect(trip) {
        if (trip == null && !uiState.isLoading) {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip?.title ?: "行程详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (trip == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (uiState.isLoading) CircularProgressIndicator() else Text("暂无行程数据", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { OverviewHeader(trip) }

            // Weather section
            if (uiState.weather.isNotEmpty()) {
                item { WeatherSection(uiState.weather) }
            }

            if (trip.emergencyContact.phone.isNotBlank()) {
                item { EmergencyContactCard(trip) }
            }

            item {
                Text("行程安排", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (trip.days.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("暂无行程安排", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                itemsIndexed(trip.days) { index, day ->
                    DayCard(day = day, dayIndex = index, onClick = { onNavigateToDay(index) }, weather = uiState.weather.find { it.date == day.date })
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = {
                        viewModel.loadPackingList(trip.tripId)
                        onNavigateToPacking()
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Luggage, contentDescription = "行李")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("查看行李清单", style = MaterialTheme.typography.titleMedium)
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val dayOfWeek = when (date.dayOfWeek.value) {
            1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
            5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
        }
        "${date.monthValue}月${date.dayOfMonth}日 $dayOfWeek"
    } catch (_: Exception) { dateStr }
}

@Composable
private fun WeatherSection(weather: List<WeatherInfo>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("🌤 天气预报", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weather.forEach { w ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(w.icon, style = MaterialTheme.typography.titleMedium)
                        Text("${w.tempMin}°~${w.tempMax}°", style = MaterialTheme.typography.labelSmall)
                        Text(w.textDay, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewHeader(trip: Trip) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(trip.title.ifBlank { "未命名行程" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = "日期", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${formatDate(trip.dateRange.start)} ~ ${formatDate(trip.dateRange.end)}", style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationCity, contentDescription = "城市", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                val cities = trip.days.map { it.city }.distinct()
                Text(cities.joinToString(" → "), style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.People, contentDescription = "出行人数", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${trip.travelers.count}人出行", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun EmergencyContactCard(trip: Trip) {
    val context = LocalContext.current
    val contact = trip.emergencyContact
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("拨打电话") },
            text = { Text("确定拨打 ${contact.name} 的电话 ${contact.phone} 吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
                    context.startActivity(intent)
                }) { Text("拨打") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("📞 紧急联系人", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("${contact.name}  ${contact.phone}", style = MaterialTheme.typography.bodyLarge)
            }
            FilledIconButton(
                onClick = { showDialog = true },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = "拨号", modifier = Modifier.size(28.dp))
            }
        }
    }
}

@Composable
private fun DayCard(day: TripDay, dayIndex: Int, onClick: () -> Unit, weather: WeatherInfo?) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("D${day.dayNumber}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(day.city.ifBlank { "未指定城市" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(day.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (day.summary.isNotEmpty()) {
                    Text(
                        day.summary.take(2).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                weather?.let { w ->
                    Text(
                        "${w.icon} ${w.tempMin}°~${w.tempMax}° ${w.textDay}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = "查看详情")
        }
    }
}
