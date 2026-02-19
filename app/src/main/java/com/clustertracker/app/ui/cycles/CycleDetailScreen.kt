package com.clustertracker.app.ui.cycles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.CycleLog
import com.clustertracker.app.util.TimeFormatters
import java.time.Duration
import java.time.Instant

/**
 * Represents an item in the interleaved timeline (attacks + logs sorted by time).
 */
private sealed class TimelineItem(val timestamp: Instant) {
    class AttackItem(val attack: Attack, val attackNumber: Int) :
        TimelineItem(attack.shadowOnsetTime)

    class LogItem(val log: CycleLog) : TimelineItem(log.timestamp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleDetailScreen(
    onBack: () -> Unit,
    onEditCycle: (Long) -> Unit,
    onAttackClick: (Long) -> Unit,
    onStartAttack: (attackId: Long, cycleId: Long) -> Unit,
    onManualAttack: (cycleId: Long) -> Unit = {},
    viewModel: CycleDetailViewModel = hiltViewModel()
) {
    val cycle by viewModel.cycle.collectAsStateWithLifecycle()
    val attacks by viewModel.attacks.collectAsStateWithLifecycle()
    val cycleLogs by viewModel.cycleLogs.collectAsStateWithLifecycle()
    val attackCount by viewModel.attackCount.collectAsStateWithLifecycle()
    val showLogDialog by viewModel.showLogDialog.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var editingLog by remember { mutableStateOf<CycleLog?>(null) }

    // Build interleaved timeline: attacks + logs sorted by time descending
    val timelineItems = remember(attacks, cycleLogs) {
        val attackItems = attacks.mapIndexed { index, attack ->
            TimelineItem.AttackItem(attack, attacks.size - index)
        }
        val logItems = cycleLogs.map { TimelineItem.LogItem(it) }
        (attackItems + logItems).sortedByDescending { it.timestamp }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CycleDetailViewModel.DetailEvent.AttackStarted ->
                    onStartAttack(event.attackId, viewModel.cycleId)
                CycleDetailViewModel.DetailEvent.CycleDeleted -> onBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cycle?.name ?: "Cycle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onManualAttack(viewModel.cycleId) }) {
                        Icon(Icons.Default.PostAdd, contentDescription = "Add Past Attack")
                    }
                    IconButton(onClick = { onEditCycle(viewModel.cycleId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Cycle", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showOverflowMenu = false
                                showDeleteDialog = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            val cycleEnded = cycle?.endDate != null
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (!cycleEnded) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.startAttack() },
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ) {
                        Text("Attack")
                    }
                }
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showLogDialog() },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Text("Log")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cycle info header
            item {
                cycle?.let { c ->
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row {
                            Text(
                                text = TimeFormatters.formatDate(c.startDate),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            c.endDate?.let {
                                Text(
                                    text = " — ${TimeFormatters.formatDate(it)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } ?: Text(
                                text = " — Active",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$attackCount attacks",
                            style = MaterialTheme.typography.titleMedium
                        )
                        c.notes?.let { notes ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Timeline",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (timelineItems.isEmpty()) {
                item {
                    Text(
                        text = "No attacks or logs yet. Use the buttons below to get started.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            items(
                timelineItems,
                key = { item ->
                    when (item) {
                        is TimelineItem.AttackItem -> "attack_${item.attack.id}"
                        is TimelineItem.LogItem -> "log_${item.log.id}"
                    }
                }
            ) { item ->
                when (item) {
                    is TimelineItem.AttackItem -> AttackSummaryCard(
                        attack = item.attack,
                        attackNumber = item.attackNumber,
                        onClick = {
                            if (item.attack.endTime == null) {
                                onStartAttack(item.attack.id, item.attack.cycleId)
                            } else {
                                onAttackClick(item.attack.id)
                            }
                        }
                    )
                    is TimelineItem.LogItem -> LogCard(
                        log = item.log,
                        onClick = { editingLog = item.log }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Cycle?") },
            text = { Text("This will permanently delete this cycle and all its attacks.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteCycle()
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

    if (showLogDialog) {
        var logText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogDialog() },
            title = { Text("Add Log") },
            text = {
                OutlinedTextField(
                    value = logText,
                    onValueChange = { logText = it },
                    label = { Text("What happened?") },
                    placeholder = { Text("e.g. Took D3 5000IU, Tried energy drink...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.addLog(logText) }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    editingLog?.let { log ->
        var editText by remember(log.id) { mutableStateOf(log.note) }
        var confirmDelete by remember { mutableStateOf(false) }

        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text("Delete Log?") },
                text = { Text("This will permanently delete this log entry.") },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDelete = false
                        editingLog = null
                        viewModel.deleteLog(log)
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDelete = false }) {
                        Text("Cancel")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { editingLog = null },
                title = { Text("Edit Log") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("What happened?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.updateLog(log, editText)
                        editingLog = null
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { confirmDelete = true }) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                        TextButton(onClick = { editingLog = null }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun AttackSummaryCard(
    attack: Attack,
    attackNumber: Int,
    onClick: () -> Unit
) {
    val isActive = attack.endTime == null
    val duration = if (isActive) {
        Duration.between(attack.shadowOnsetTime, Instant.now())
    } else {
        Duration.between(attack.shadowOnsetTime, attack.endTime)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = TimeFormatters.formatDateTime(attack.shadowOnsetTime),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (isActive) {
                    Text(
                        text = "IN PROGRESS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Text(
                    text = "#$attackNumber",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Text(
                    text = if (isActive) "Duration: ongoing" else "Duration: ${TimeFormatters.formatDurationShort(duration)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (attack.oxygenSessions.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "${attack.oxygenSessions.size} O2 sessions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun LogCard(log: CycleLog, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = TimeFormatters.formatDateTime(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = log.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
