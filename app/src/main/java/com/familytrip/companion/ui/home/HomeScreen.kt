package com.familytrip.companion.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.familytrip.companion.viewmodel.TripUiState
import com.familytrip.companion.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TripViewModel,
    onNavigateToTrip: () -> Unit,
    onNavigateToSettings: () -> Unit,
    deepLinkToken: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var tokenInput by remember { mutableStateOf("") }

    LaunchedEffect(deepLinkToken) {
        if (!deepLinkToken.isNullOrBlank() && tokenInput.isBlank()) {
            tokenInput = deepLinkToken
            viewModel.loadTrip(deepLinkToken)
            onNavigateToTrip()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("旅行助手", style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "输入子女分享的行程码",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = tokenInput,
                onValueChange = { input ->
                    tokenInput = if (input.contains("/parent/")) {
                        input.substringAfter("/parent/").substringBefore("?").substringBefore("#").trim()
                    } else {
                        input.trim()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("粘贴行程码或分享链接") },
                shape = RoundedCornerShape(12.dp),
                minLines = 2,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Button(
                onClick = {
                    if (tokenInput.isNotBlank()) {
                        viewModel.loadTrip(tokenInput)
                        onNavigateToTrip()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = tokenInput.isNotBlank()
            ) {
                Text("查看行程", style = MaterialTheme.typography.titleMedium)
            }

            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // History
            if (uiState.history.isNotEmpty()) {
                HorizontalDivider()
                Text(
                    "最近查看",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.history) { item ->
                        Card(
                            onClick = {
                                tokenInput = item.token
                                viewModel.loadTrip(item.token)
                                onNavigateToTrip()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        item.title.ifBlank { "未命名行程" },
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        item.dateRange,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // P2-15: Clear history button
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("清除历史记录")
                }
            }
        }
    }
}
