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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clustertracker.app.domain.model.FlowRate
import com.clustertracker.app.domain.model.OxygenSession
import com.clustertracker.app.domain.model.TherapyNote
import com.clustertracker.app.ui.theme.painColor
import com.clustertracker.app.util.TimeFormatters
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

private val kipLabels = mapOf(
    0 to "No pain",
    1 to "Very mild",
    2 to "Mild",
    3 to "Noticeable",
    4 to "Moderate",
    5 to "Moderately severe",
    6 to "Severe",
    7 to "Very severe",
    8 to "Intense",
    9 to "Excruciating",
    10 to "Unbearable"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveAttackScreen(
    onAttackEnded: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ActiveAttackViewModel = hiltViewModel()
) {
    val attack by viewModel.attack.collectAsStateWithLifecycle()
    val activeO2 by viewModel.activeO2Session.collectAsStateWithLifecycle()
    val painLevel by viewModel.currentPainLevel.collectAsStateWithLifecycle()
    val selectedFlowRate by viewModel.selectedFlowRate.collectAsStateWithLifecycle()
    val showNoteDialog by viewModel.showNoteDialog.collectAsStateWithLifecycle()
    var showEndDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ActiveAttackViewModel.AttackEvent.AttackEnded ->
                    onAttackEnded(event.attackId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Attack") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Timer
            attack?.let { a ->
                AttackTimer(shadowOnsetTime = a.shadowOnsetTime)
            }

            // Pain slider
            PainSliderSection(
                painLevel = painLevel,
                onPainLevelChange = viewModel::updatePainLevel,
                onLogNow = viewModel::logPainNow
            )

            // O2 therapy
            OxygenSection(
                activeSession = activeO2,
                selectedFlowRate = selectedFlowRate,
                completedSessions = attack?.oxygenSessions?.filter { it.stopTime != null } ?: emptyList(),
                onFlowRateSelect = viewModel::selectFlowRate,
                onToggleO2 = viewModel::toggleO2,
                shadowOnset = attack?.shadowOnsetTime
            )

            // Therapy notes
            TherapyNotesSection(
                notes = attack?.therapyNotes ?: emptyList(),
                shadowOnset = attack?.shadowOnsetTime,
                onAddNote = viewModel::showNoteDialog
            )

            // End attack
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { showEndDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("End Attack", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Note dialog
    if (showNoteDialog) {
        var noteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = viewModel::dismissNoteDialog,
            title = { Text("Add Therapy Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("e.g., took sumatriptan, used ice pack...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addTherapyNote(noteText) }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissNoteDialog) {
                    Text("Cancel")
                }
            }
        )
    }

    // End attack confirmation
    if (showEndDialog) {
        AlertDialog(
            onDismissRequest = { showEndDialog = false },
            title = { Text("End Attack?") },
            text = { Text("This will stop all timers and save the attack data.") },
            confirmButton = {
                TextButton(onClick = {
                    showEndDialog = false
                    viewModel.endAttack()
                }) {
                    Text("End Attack")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AttackTimer(shadowOnsetTime: Instant) {
    var elapsedMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(shadowOnsetTime) {
        while (true) {
            elapsedMs = Duration.between(shadowOnsetTime, Instant.now()).toMillis()
            delay(1000L)
        }
    }

    val duration = Duration.ofMillis(elapsedMs)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Elapsed Time",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = TimeFormatters.formatDuration(duration),
                style = MaterialTheme.typography.displayLarge,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PainSliderSection(
    painLevel: Int,
    onPainLevelChange: (Int) -> Unit,
    onLogNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pain Level",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Auto-logging",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Large pain level display
            Text(
                text = "KIP $painLevel",
                style = MaterialTheme.typography.headlineLarge,
                color = painColor(painLevel),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = kipLabels[painLevel] ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = painColor(painLevel),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = painLevel.toFloat(),
                onValueChange = { onPainLevelChange(it.toInt()) },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = painColor(painLevel),
                    activeTrackColor = painColor(painLevel)
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0", style = MaterialTheme.typography.labelSmall)
                Text("10", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onLogNow,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Log Now")
            }
        }
    }
}

@Composable
private fun OxygenSection(
    activeSession: OxygenSession?,
    selectedFlowRate: Int,
    completedSessions: List<OxygenSession>,
    onFlowRateSelect: (Int) -> Unit,
    onToggleO2: () -> Unit,
    shadowOnset: Instant?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Oxygen Therapy",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Flow rate slider
            Text(
                text = "${FlowRate.displayLabel(selectedFlowRate)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Slider(
                value = selectedFlowRate.toFloat(),
                onValueChange = { onFlowRateSelect(it.toInt()) },
                valueRange = FlowRate.MIN.toFloat()..FlowRate.MAX.toFloat(),
                steps = FlowRate.MAX - FlowRate.MIN - 1,
                enabled = activeSession == null,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${FlowRate.MIN}", style = MaterialTheme.typography.labelSmall)
                Text("${FlowRate.MAX}", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active session timer
            if (activeSession != null) {
                var elapsedMs by remember { mutableLongStateOf(0L) }
                LaunchedEffect(activeSession.startTime) {
                    while (true) {
                        elapsedMs = Duration.between(activeSession.startTime, Instant.now()).toMillis()
                        delay(1000L)
                    }
                }
                Text(
                    text = "O2 running: ${TimeFormatters.formatDuration(Duration.ofMillis(elapsedMs))} at ${FlowRate.displayLabel(activeSession.flowRate)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Start/Stop button
            Button(
                onClick = onToggleO2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSession != null) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.secondary
                    }
                )
            ) {
                Text(
                    text = if (activeSession != null) "Stop O2" else "Start O2",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Completed sessions list
            if (completedSessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Completed sessions:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                completedSessions.forEach { session ->
                    val duration = Duration.between(session.startTime, session.stopTime)
                    val offset = shadowOnset?.let {
                        TimeFormatters.formatRelativeMinutes(it, session.startTime)
                    } ?: ""
                    Text(
                        text = "$offset ${FlowRate.displayLabel(session.flowRate)} for ${TimeFormatters.formatDurationShort(duration)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TherapyNotesSection(
    notes: List<TherapyNote>,
    shadowOnset: Instant?,
    onAddNote: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Other Therapies",
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(onClick = onAddNote) {
                    Icon(Icons.Default.Add, contentDescription = "Add note")
                }
            }

            if (notes.isEmpty()) {
                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                notes.forEach { note ->
                    Row(modifier = Modifier.padding(vertical = 2.dp)) {
                        val offset = shadowOnset?.let {
                            TimeFormatters.formatRelativeMinutes(it, note.timestamp)
                        } ?: ""
                        Text(
                            text = offset,
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
}
