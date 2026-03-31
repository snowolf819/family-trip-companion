package com.trip.family.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.viewmodel.TripViewModel
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * 入口屏幕：深度链接打开自动加载，否则显示欢迎页+分享码输入
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: TripViewModel = viewModel(),
    shareTokenFlow: MutableSharedFlow<String>? = null,
    onTripLoaded: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasCache by viewModel.hasCache.collectAsState()

    var shareCode by remember { mutableStateOf("") }

    // 监听网络加载成功事件才导航（缓存加载不触发）
    LaunchedEffect(Unit) {
        viewModel.navigateToOverview.collect {
            onTripLoaded()
        }
    }

    // 监听深度链接 token（从 MainActivity 的 onNewIntent 传入）
    LaunchedEffect(Unit) {
        shareTokenFlow?.collect { token ->
            viewModel.loadTripByToken(token)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("👨‍👩‍👧‍👦", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "家庭旅行助手",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "子女为您准备的旅行行程",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = shareCode,
            onValueChange = { shareCode = it },
            label = { Text("分享码", fontSize = 16.sp) },
            placeholder = { Text("输入子女发给您的分享码") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = {
                if (shareCode.isNotBlank()) {
                    viewModel.loadTripByToken(shareCode.trim())
                }
            }),
            textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
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
            Text("查看行程", fontSize = 20.sp)
        }

        errorMessage?.let { msg ->
            Spacer(Modifier.height(16.dp))
            Text(msg, color = MaterialTheme.colorScheme.error, fontSize = 16.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { viewModel.clearError() }) {
                Text("关闭", fontSize = 16.sp)
            }
        }

        if (isLoading) {
            Spacer(Modifier.height(24.dp))
            CircularProgressIndicator()
            Text("加载中…", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (hasCache && !isLoading && errorMessage == null) {
            Spacer(Modifier.height(24.dp))
            TextButton(onClick = { viewModel.loadCachedTrip() }) {
                Text("📖 查看上次保存的行程", fontSize = 18.sp)
            }
        }

        Spacer(Modifier.weight(1f))

        TextButton(onClick = onOpenSettings) {
            Text("⚙️ 设置服务器地址", fontSize = 14.sp)
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
