package com.trip.family.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.viewmodel.TripViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val fontScaleOptions = listOf(
    0.8f to "小",
    1.0f to "标准",
    1.2f to "大",
    1.4f to "超大"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TripViewModel = viewModel(),
    fontScaleState: MutableFloatState? = null,
    prefs: com.trip.family.TripPreferences? = null,
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    var serverUrl by remember { mutableStateOf(viewModel.serverUrl) }
    val currentScale = fontScaleState?.floatValue ?: 1.0f
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("设置", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ========== 字体大小 ==========
            Text("🔤 字体大小", fontSize = 22.sp, fontWeight = FontWeight.Bold)

            // 预览文字
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "预览效果",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "这是一段预览文字，调整下方滑块查看效果变化。",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 滑块
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("小", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = currentScale,
                    onValueChange = { newValue ->
                        val snapped = fontScaleOptions.minByOrNull { Math.abs(it.first - newValue) }?.first ?: newValue
                        fontScaleState?.floatValue = snapped
                        prefs?.fontScale = snapped
                    },
                    valueRange = 0.8f..1.4f,
                    steps = 2,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                Text("超大", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            // 当前档位显示
            val label = fontScaleOptions.minByOrNull { Math.abs(it.first - currentScale) }?.second ?: "标准"
            Text(
                "当前：$label (${(currentScale * 100).toInt()}%)",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // ========== 服务器地址 ==========
            Text("🌐 服务器地址", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "App 通过此地址获取行程数据。如子女告知了新地址，在此修改。",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                label = { Text("服务器地址") },
                placeholder = { Text("https://plan.and.im") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 18.sp)
            )

            Button(
                onClick = {
                    viewModel.updateServerUrl(serverUrl)
                    scope.launch {
                        snackbarHostState.showSnackbar("✅ 已保存")
                        kotlinx.coroutines.delay(800)
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("保存", fontSize = 20.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
