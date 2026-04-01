package com.trip.family.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.viewmodel.TripViewModel
import kotlinx.coroutines.flow.SharedFlow

/**
 * 入口屏幕：深度链接打开自动加载，否则显示欢迎页+分享码输入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TripViewModel = viewModel(),
    shareTokenFlow: SharedFlow<String>? = null,
    onTripLoaded: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasCache by viewModel.hasCache.collectAsState()

    var shareCode by remember { mutableStateOf("") }

    // 监听导航事件
    LaunchedEffect(viewModel) {
        viewModel.navigateToOverview.collect {
            onTripLoaded()
        }
    }

    // 监听深度链接 token（从 MainActivity 的 onNewIntent 传入）
    LaunchedEffect(shareTokenFlow) {
        shareTokenFlow?.collect { token ->
            viewModel.loadTripByToken(token)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👨‍👩‍👧‍👦", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "家庭旅行助手",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "子女为您准备的旅行行程",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = shareCode,
            onValueChange = { shareCode = it },
            label = { Text("分享码", style = MaterialTheme.typography.bodyLarge) },
            placeholder = { Text("输入子女发给您的分享码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                if (shareCode.isNotBlank()) {
                    viewModel.loadTripByToken(shareCode.trim())
                }
            }),
            textStyle = LocalTextStyle.current
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (shareCode.isNotBlank()) {
                    viewModel.loadTripByToken(shareCode.trim())
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("查看行程", style = MaterialTheme.typography.titleLarge)
        }

        errorMessage?.let { msg ->
            Spacer(Modifier.height(16.dp))
            Text(msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { viewModel.clearError() }) {
                Text("关闭", style = MaterialTheme.typography.bodyLarge)
            }
        }

        if (isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
            Text("加载中…", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (hasCache && !isLoading && errorMessage == null) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { viewModel.loadCachedTrip() }) {
                Text("📖 查看上次保存的行程", style = MaterialTheme.typography.titleMedium)
            }
        }

        // 预览演示按钮（不需要后端）
        if (!isLoading && errorMessage == null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.loadMockTrip() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("🎨 预览演示（不需要服务器）", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(Modifier.height(32.dp))

        TextButton(onClick = onOpenSettings) {
            Text("⚙️ 设置服务器地址", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * 从 Intent 中提取分享 token
 */
fun extractShareToken(intent: android.content.Intent?): String? {
    if (intent == null) return null
    val data = intent.data ?: return null

    // https://plan.and.im/share/xxxxx
    if (data.scheme == "https" && data.host == "plan.and.im" && data.pathSegments.size >= 2) {
        return data.pathSegments[1]
    }

    // tripfamily://share/xxxxx
    if (data.scheme == "tripfamily" && data.host == "share") {
        return data.pathSegments.firstOrNull()
    }

    return null
}
