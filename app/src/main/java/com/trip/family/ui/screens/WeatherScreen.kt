package com.trip.family.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.data.WeatherDay
import com.trip.family.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: TripViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val weather by viewModel.weather.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌤️ 旅行天气", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (weather.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无数据", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(weather, key = { it.date + it.city }) { day ->
                    WeatherDayCard(day)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun WeatherDayCard(day: WeatherDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(day.date, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(day.city, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${day.icon} ${day.textDay}", fontSize = 24.sp)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("🌡️ ${day.tempMin}° ~ ${day.tempMax}°", fontSize = 22.sp, fontWeight = FontWeight.Medium)
                Text("💨 ${day.windDir} ${day.windScale}级", fontSize = 18.sp)
                Text("💧 ${day.humidity}%", fontSize = 18.sp)
            }
        }
    }
}
