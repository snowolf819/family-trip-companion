package com.trip.family.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.data.*
import com.trip.family.ui.components.*
import com.trip.family.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dayNumber: Int,
    viewModel: TripViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val trip by viewModel.trip.collectAsState()
    val day = trip?.days?.find { it.dayNumber == dayNumber }
    val context = LocalContext.current

    if (day == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("未找到该天行程", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📅 第${day.dayNumber}天 · ${day.city}",
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    trip?.emergencyContact?.let { contact ->
                        if (contact.phone.isNotBlank()) {
                            IconButton(onClick = { context.dialPhone(contact.phone) }) {
                                Icon(Icons.Default.Phone, contentDescription = "呼叫", tint = Color(0xFFE53935))
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 日期概要卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${day.date} · ${day.city}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        SummaryList(summary = day.summary)
                    }
                }
            }

            // 应急方案
            day.emergencyPlan?.let { plan ->
                item { EmergencyPlanCard(plan) }
            }

            // 行程片段
            items(day.segments, key = { it.id }) { segment ->
                SegmentCard(segment)
            }

            // 酒店信息
            day.hotelCard?.let { hotel ->
                item { HotelInfoCard(hotel) }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SummaryList(summary: List<String>) {
    for (item in summary) {
        Text("• $item", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
