package com.familytrip.companion.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
    var showClearDialog by remember { mutableStateOf(false) }
    var hasNavigated by remember { mutableStateOf(false) }

    // Deep link: load trip and wait for it before navigating
    LaunchedEffect(deepLinkToken) {
        if (!deepLinkToken.isNullOrBlank() && tokenInput.isBlank()) {
            tokenInput = deepLinkToken
            viewModel.loadTrip(deepLinkToken)
        }
    }

    // Navigate once trip is loaded (fixes race condition)
    LaunchedEffect(uiState.trip, uiState.error) {
        if (uiState.trip != null && !hasNavigated && tokenInput.isNotBlank()) {
            hasNavigated = true
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
                        hasNavigated = false
                        viewModel.loadTrip(tokenInput)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = tokenInput.isNotBlank() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("查看行程", style = MaterialTheme.typography.titleMedium)
                }
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

            // Guide card when no server configured
            val baseUrl by viewModel.baseUrl.collectAsState()
            if (baseUrl.isBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "提示",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "请先配置服务器地址",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "前往 设置 页面填写 API 地址后即可使用",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
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
                                hasNavigated = false
                                viewModel.loadTrip(item.token)
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
                                    contentDescription = "历史记录",
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

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showClearDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("清除历史记录")
                }

                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        title = { Text("确认清除") },
                        text = { Text("确定要清除所有历史记录吗？此操作不可撤销。") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.clearHistory()
                                showClearDialog = false
                            }) { Text("清除", color = MaterialTheme.colorScheme.error) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) { Text("取消") }
                        }
                    )
                }
            }
        }
    }
}
