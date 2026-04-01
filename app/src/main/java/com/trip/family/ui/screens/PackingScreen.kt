package com.trip.family.ui.screens

import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trip.family.data.PackingCategory
import com.trip.family.data.PackingItem
import com.trip.family.viewmodel.TripViewModel

private fun getChecked(context: Context, itemId: String): Boolean {
    return context.getSharedPreferences("packing", Context.MODE_PRIVATE)
        .getBoolean("packing_checked_$itemId", false)
}

private fun setChecked(context: Context, itemId: String, checked: Boolean) {
    context.getSharedPreferences("packing", Context.MODE_PRIVATE)
        .edit().putBoolean("packing_checked_$itemId", checked).apply()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingScreen(
    viewModel: TripViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val packingList by viewModel.packingList.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🧳 行李清单", fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        val list = packingList
        if (list == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无数据", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (list.weatherNote.isNotBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Text(
                                "💡 ${list.weatherNote}",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                items(list.categories, key = { it.name }) { category ->
                    PackingCategoryCard(category, context)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun PackingCategoryCard(
    category: PackingCategory,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(category.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            category.items.forEach { item ->
                val isChecked = remember { mutableStateOf(getChecked(context, item.id)) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked.value,
                        onCheckedChange = { checked ->
                            isChecked.value = checked
                            setChecked(context, item.id, checked)
                        },
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = buildString {
                            if (item.essential) append("⭐ ")
                            append(item.name)
                            item.condition?.let { append("（$it）") }
                        },
                        fontSize = 18.sp,
                        color = if (isChecked.value)
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isChecked.value) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}
