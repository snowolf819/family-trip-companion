package com.trip.family.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.data.WeatherDay
import com.trip.family.location.LocationService
import com.trip.family.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: TripViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val weather by viewModel.weather.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val isLocating by viewModel.isLocating.collectAsState()
    val weatherError by viewModel.weatherError.collectAsState()
    val context = LocalContext.current

    // 定位权限请求
    var hasPermission by remember {
        mutableStateOf(LocationService.hasLocationPermission(context))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasPermission = granted
        if (granted) {
            viewModel.requestLocation()
        }
    }

    // 有权限时自动定位
    LaunchedEffect(hasPermission) {
        if (hasPermission && currentCity.isBlank()) {
            viewModel.requestLocation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🌤️ 旅行天气", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (weatherError) {
            Box(Modifier.fillMaxSize().padding(padding).padding(horizontal = 24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("天气数据加载失败", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = {
                        viewModel.clearWeatherError()
                        viewModel.refreshTrip()
                    }) {
                        Text("重试")
                    }
                }
            }
        } else if (weather.isEmpty() && currentCity.isBlank()) {
            // 完全没数据
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("暂无天气数据", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Text("加载行程后可查看旅行天气预报", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 定位卡片
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isLocating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Text("正在定位…", style = MaterialTheme.typography.titleMedium)
                                } else if (currentCity.isNotBlank()) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "📍 当前位置：$currentCity",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.LocationOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(28.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("未定位", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            if (!hasPermission) {
                                Button(
                                    onClick = { permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    ) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("开启定位", style = MaterialTheme.typography.bodyLarge)
                                }
                            } else if (currentCity.isBlank() && !isLocating) {
                                IconButton(onClick = { viewModel.requestLocation() }) {
                                    Icon(Icons.Default.MyLocation, contentDescription = "重新定位")
                                }
                            }
                        }
                    }
                }

                // 行程天气
                if (weather.isNotEmpty()) {
                    item {
                        Text(
                            "📋 旅行天气预报",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    items(weather, key = { it.date + it.city }) { day ->
                        WeatherDayCard(day, isCurrentCity = day.city == currentCity)
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun WeatherDayCard(day: WeatherDay, isCurrentCity: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentCity)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(day.date, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (isCurrentCity) {
                            Spacer(Modifier.width(8.dp))
                            Text("📍 当前", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(day.city, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("${day.icon} ${day.textDay}", style = MaterialTheme.typography.headlineSmall)
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("🌡️ ${day.tempMin}° ~ ${day.tempMax}°", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Medium)
                Text("💨 ${day.windDir} ${day.windScale}", style = MaterialTheme.typography.titleMedium)
                Text("💧 ${day.humidity}%", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
