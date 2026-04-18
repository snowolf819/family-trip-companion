package com.familytrip.companion.ui.overview

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.familytrip.companion.R
import com.familytrip.companion.data.model.EmergencyContact
import com.familytrip.companion.data.model.Trip
import com.familytrip.companion.viewmodel.TripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripOverviewScreen(
    viewModel: TripViewModel,
    onNavigateToDay: () -> Unit,
    onNavigateToPacking: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val trip by viewModel.currentTrip.collectAsState()

    if (trip == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(trip!!.title) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回") } },
                actions = {
                    IconButton(onClick = { viewModel.loadPackingList(trip!!.tripId); onNavigateToPacking() }) {
                        Icon(Icons.Default.Luggage, contentDescription = stringResource(R.string.packing_list))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TripInfoCard(trip!!) }
            trip!!.emergencyContact?.let { contact ->
                item {
                    EmergencyCallCard(contact = contact, onCall = {
                        try { context.startActivity(viewModel.getCallIntent(contact.phone)) }
                        catch (_: ActivityNotFoundException) { Toast.makeText(context, "无法拨打电话", Toast.LENGTH_SHORT).show() }
                    })
                }
            }
            items(trip!!.days) { day ->
                Card(
                    onClick = { viewModel.selectDay(day); onNavigateToDay() },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("第 ${day.dayNumber} 天 . ${day.city}", style = MaterialTheme.typography.titleMedium)
                        Text(day.date, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (day.summary.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(day.summary.joinToString("；"), style = MaterialTheme.typography.bodyLarge, maxLines = 3)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TripInfoCard(trip: Trip) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(trip.title, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${trip.dateRange.start} ~ ${trip.dateRange.end}", style = MaterialTheme.typography.bodyLarge)
            val cities = trip.days.map { it.city }.distinct()
            if (cities.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("途经：${cities.joinToString(" -> ")}", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun EmergencyCallCard(contact: EmergencyContact, onCall: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text(stringResource(R.string.emergency_call), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                Text("${contact.name}：${contact.phone}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onErrorContainer)
            }
            FilledTonalButton(onClick = onCall, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("拨打")
            }
        }
    }
}
