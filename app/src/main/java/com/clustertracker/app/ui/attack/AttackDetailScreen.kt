package com.clustertracker.app.ui.attack

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.EnvironmentalData
import com.clustertracker.app.domain.model.TherapyNote
import com.clustertracker.app.ui.chart.AttackChart
import com.clustertracker.app.util.TimeFormatters
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttackDetailScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit = {},
    viewModel: AttackDetailViewModel = hiltViewModel()
) {
    val attack by viewModel.attack.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AttackDetailViewModel.DetailEvent.AttackDeleted -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attack Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { attack?.let { onEdit(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        attack?.let { a ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary header
                AttackSummaryHeader(a)

                // Pain/O2 chart
                if (a.painDataPoints.isNotEmpty() || a.oxygenSessions.isNotEmpty()) {
                    AttackChart(attack = a)
                }

                // Therapy timeline
                if (a.therapyNotes.isNotEmpty()) {
                    TherapyTimeline(notes = a.therapyNotes, shadowOnset = a.shadowOnsetTime)
                }

                // O2 sessions detail
                if (a.oxygenSessions.isNotEmpty()) {
                    O2SessionsDetail(attack = a)
                }

                // Environmental data
                a.environmentalData?.let { env ->
                    EnvironmentalCard(env)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        } ?: run {
            Text(
                text = "Loading...",
                modifier = Modifier.padding(padding).padding(16.dp)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Attack?") },
            text = { Text("This will permanently delete this attack and all its data.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAttack()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttackSummaryHeader(attack: Attack) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = TimeFormatters.formatDateTime(attack.shadowOnsetTime),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            val duration = attack.endTime?.let {
                Duration.between(attack.shadowOnsetTime, it)
            }
            Row {
                Text(
                    text = "Duration: ${duration?.let { TimeFormatters.formatDurationShort(it) } ?: "Unknown"}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(24.dp))
                val peakPain = attack.painDataPoints.maxByOrNull { it.intensity }?.intensity
                peakPain?.let {
                    Text(
                        text = "Peak: KIP $it",
                        style = MaterialTheme.typography.bodyLarge,
                        color = com.clustertracker.app.ui.theme.painColor(it)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${attack.oxygenSessions.size} O2 sessions | ${attack.therapyNotes.size} notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TherapyTimeline(notes: List<TherapyNote>, shadowOnset: Instant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Therapy Timeline",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            notes.forEach { note ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    Text(
                        text = TimeFormatters.formatRelativeMinutes(shadowOnset, note.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(48.dp)
                    )
                    Text(
                        text = note.note,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun O2SessionsDetail(attack: Attack) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "O2 Sessions",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            val totalO2 = attack.oxygenSessions
                .filter { it.stopTime != null }
                .sumOf { Duration.between(it.startTime, it.stopTime).toMillis() / 1000 }
            Text(
                text = "Total O2 time: ${TimeFormatters.formatDurationShort(Duration.ofSeconds(totalO2))}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(4.dp))
            attack.oxygenSessions.forEach { session ->
                val duration = session.stopTime?.let {
                    Duration.between(session.startTime, it)
                }
                val offset = TimeFormatters.formatRelativeMinutes(
                    attack.shadowOnsetTime, session.startTime
                )
                Text(
                    text = "$offset ${session.flowRate} L/min — ${duration?.let { TimeFormatters.formatDurationShort(it) } ?: "active"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
private fun EnvironmentalCard(env: EnvironmentalData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Environment",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            env.cityName?.let {
                Text(text = "Location: $it", style = MaterialTheme.typography.bodyMedium)
            }
            env.temperatureCelsius?.let {
                Text(text = "Temperature: ${"%.1f".format(it)}°C", style = MaterialTheme.typography.bodyMedium)
            }
            env.barometricPressureHpa?.let {
                Text(text = "Pressure: ${"%.0f".format(it)} hPa", style = MaterialTheme.typography.bodyMedium)
            }
            env.humidity?.let {
                Text(text = "Humidity: $it%", style = MaterialTheme.typography.bodyMedium)
            }
            env.moonPhaseName?.let { phase ->
                val illumination = env.moonIlluminationFraction?.let {
                    " (${"%.0f".format(it * 100)}% illuminated)"
                } ?: ""
                Text(
                    text = "Moon: $phase$illumination",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
