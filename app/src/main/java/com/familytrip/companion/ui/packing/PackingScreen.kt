package com.familytrip.companion.ui.packing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familytrip.companion.data.model.PackingCategory
import com.familytrip.companion.data.model.PackingItem
import com.familytrip.companion.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackingScreen(
    viewModel: TripViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val packingList = uiState.packingList
    val tripId = packingList?.tripId ?: ""

    // P1-7: Load persisted checked items
    val persistedChecked by viewModel.getPackingCheckedFlow(tripId).collectAsState(initial = emptySet())
    val checkedItems = remember(tripId) { mutableStateMapOf<String, Boolean>() }

    // Sync from persisted state
    LaunchedEffect(persistedChecked) {
        checkedItems.clear()
        persistedChecked.forEach { checkedItems[it] = true }
    }

    // Persist on change
    fun onCheckedChanged(itemId: String, checked: Boolean) {
        checkedItems[itemId] = checked
        viewModel.savePackingChecked(tripId, checkedItems.filterValues { it }.keys)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("行李清单") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        // P1-5: loading state
        if (uiState.isPackingLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在生成清单...", style = MaterialTheme.typography.bodyLarge)
                }
            }
            return@Scaffold
        }

        // P1-5: error state
        if (uiState.packingError != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = "错误", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(uiState.packingError!!, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
            }
            return@Scaffold
        }

        if (packingList == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("暂无行李清单", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (packingList.weatherNote.isNotBlank()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = "天气", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(packingList.weatherNote, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            items(packingList.categories) { category ->
                PackingCategorySection(category, checkedItems) { itemId, checked ->
                    onCheckedChanged(itemId, checked)
                }
            }

            item {
                val total = packingList.categories.sumOf { it.items.size }
                val checked = packingList.categories.sumOf { cat ->
                    cat.items.count { item -> checkedItems[item.id] == true }
                }
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(
                        "已准备 $checked / $total 项",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun PackingCategorySection(
    category: PackingCategory,
    checkedItems: MutableMap<String, Boolean>,
    onCheckedChanged: (String, Boolean) -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(category.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            category.items.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = checkedItems[item.id] ?: item.checked ?: false,
                        onCheckedChange = { checked -> onCheckedChanged(item.id, checked) }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                item.name,
                                style = MaterialTheme.typography.bodyLarge,
                                textDecoration = if (checkedItems[item.id] == true) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            )
                            if (item.essential) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("必备", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        item.condition?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
