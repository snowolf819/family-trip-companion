package com.familytrip.companion.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familytrip.companion.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var baseUrlInput by remember(uiState.baseUrl) { mutableStateOf(uiState.baseUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Server URL
            Text("服务器地址", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = baseUrlInput,
                onValueChange = { baseUrlInput = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("http://192.168.1.100:3000") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = { viewModel.setBaseUrl(baseUrlInput) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("保存")
            }

            HorizontalDivider()

            // Font size
            Text("字体大小", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val scales = listOf("小" to 0.85f, "标准" to 1.0f, "大" to 1.2f, "特大" to 1.5f)
                scales.forEach { (label, scale) ->
                    FilterChip(
                        selected = kotlin.math.abs(uiState.fontScale - scale) < 0.01f,
                        onClick = { viewModel.setFontScale(scale) },
                        label = { Text(label) }
                    )
                }
            }

            HorizontalDivider()

            // Dark mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("深色模式", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = uiState.darkMode,
                    onCheckedChange = { viewModel.setDarkMode(it) }
                )
            }
        }
    }
}
