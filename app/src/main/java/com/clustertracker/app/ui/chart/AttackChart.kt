package com.clustertracker.app.ui.chart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.clustertracker.app.domain.model.Attack
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import java.time.Duration
import java.time.Instant

private val notePositionsKey = ExtraStore.Key<Set<Double>>()
private val totalSecKey = ExtraStore.Key<Double>()

/** Whole seconds between two instants. */
private fun secsBetween(from: Instant, to: Instant): Double {
    return (Duration.between(from, to).toMillis() / 1000).toDouble()
}

/** Deduplicate sorted pairs by x, keeping last y per x value. */
private fun dedup(points: List<Pair<Double, Double>>): List<Pair<Double, Double>> =
    points.fold(mutableListOf()) { acc, p ->
        if (acc.isEmpty() || acc.last().first != p.first) acc.add(p)
        else acc[acc.lastIndex] = p
        acc
    }

@Composable
fun AttackChart(
    attack: Attack,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(attack) {
        val onset = attack.shadowOnsetTime

        val attackEndSec = attack.endTime?.let { secsBetween(onset, it) } ?: 0.0
        // If attack has an end time, use it as the bound; otherwise use latest data
        val totalSec = if (attackEndSec > 0.0) {
            maxOf(attackEndSec, 60.0)
        } else {
            val latestPainSec = attack.painDataPoints.maxOfOrNull { secsBetween(onset, it.timestamp) } ?: 0.0
            val latestO2Sec = attack.oxygenSessions.maxOfOrNull {
                secsBetween(onset, it.stopTime ?: onset)
            } ?: 0.0
            val latestNoteSec = attack.therapyNotes.maxOfOrNull { secsBetween(onset, it.timestamp) } ?: 0.0
            maxOf(latestPainSec, latestO2Sec, latestNoteSec, 60.0)
        }

        val notePositions = attack.therapyNotes.map {
            secsBetween(onset, it.timestamp).coerceIn(0.0, totalSec)
        }.toSet()

        // Build O2 step-function
        val o2Points = mutableListOf<Pair<Double, Double>>()
        o2Points.add(0.0 to 0.0)
        attack.oxygenSessions.sortedBy { it.startTime }.forEach { session ->
            val startSec = secsBetween(onset, session.startTime).coerceIn(0.0, totalSec)
            val endSec = secsBetween(onset, session.stopTime ?: attack.endTime ?: onset)
                .coerceIn(0.0, totalSec)
            val lpm = session.flowRate.toDouble()
            o2Points.add(startSec to 0.0)
            o2Points.add((startSec + 1.0) to lpm)
            o2Points.add(endSec to lpm)
            o2Points.add((endSec + 1.0) to 0.0)
        }
        o2Points.add(totalSec to 0.0)
        val sortedO2 = dedup(o2Points.sortedBy { it.first })

        // Build pain data
        val painData: Pair<List<Double>, List<Double>> = if (attack.painDataPoints.isNotEmpty()) {
            val raw = attack.painDataPoints.map {
                secsBetween(onset, it.timestamp).coerceIn(0.0, totalSec) to it.intensity.toDouble()
            }.sortedBy { it.first }
            val dd = dedup(raw)
            val px = dd.map { it.first }.toMutableList()
            val py = dd.map { it.second }.toMutableList()
            if (px.last() < totalSec) { px.add(totalSec); py.add(py.last()) }
            px to py
        } else {
            listOf(0.0, totalSec) to listOf(0.0, 0.0)
        }

        modelProducer.runTransaction {
            // First lineSeries → first layer (O2 green area)
            lineSeries {
                series(x = sortedO2.map { it.first }, y = sortedO2.map { it.second })
            }
            // Second lineSeries → second layer (Pain red line)
            lineSeries {
                series(x = painData.first, y = painData.second)
            }
            extras {
                it[notePositionsKey] = notePositions
                it[totalSecKey] = totalSec
            }
        }
    }

    val kipFormatter = CartesianValueFormatter { _, value, _ ->
        value.toInt().toString()
    }
    val timeFormatter = CartesianValueFormatter { _, value, _ ->
        val totalSecs = value.toInt()
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        if (secs == 0) "${mins}m" else "${mins}:%02d".format(secs)
    }

    val noteMarker = LineCartesianLayer.point(
        rememberShapeComponent(fill(Color(0xFFFFD700)), CorneredShape.Pill),
        size = 10.dp
    )
    val notePointProvider = remember(noteMarker) {
        object : LineCartesianLayer.PointProvider {
            override fun getPoint(
                entry: com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel.Entry,
                seriesIndex: Int,
                extraStore: ExtraStore,
            ): LineCartesianLayer.Point? {
                val positions = extraStore.getOrNull(notePositionsKey) ?: return null
                val total = extraStore.getOrNull(totalSecKey) ?: 60.0
                val threshold = maxOf(total * 0.05, 15.0)
                return if (positions.any { kotlin.math.abs(it - entry.x) < threshold }) noteMarker else null
            }

            override fun getLargestPoint(extraStore: ExtraStore) = noteMarker
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                // Layer 1: O2 green shaded area (matched to first lineSeries)
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                fill(Color(0x004CAF50))
                            ),
                            areaFill = LineCartesianLayer.AreaFill.single(
                                fill(Color(0x664CAF50))
                            )
                        )
                    ),
                    verticalAxisPosition = Axis.Position.Vertical.End
                ),
                // Layer 2: Pain smooth red line (matched to second lineSeries)
                rememberLineCartesianLayer(
                    lineProvider = LineCartesianLayer.LineProvider.series(
                        LineCartesianLayer.rememberLine(
                            fill = LineCartesianLayer.LineFill.single(
                                fill(Color(0xFFFF6B6B))
                            ),
                            areaFill = null,
                            pointProvider = notePointProvider,
                            pointConnector = LineCartesianLayer.PointConnector.cubic()
                        )
                    ),
                    verticalAxisPosition = Axis.Position.Vertical.Start
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = kipFormatter
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = timeFormatter
                )
            ),
            modelProducer = modelProducer,
            scrollState = rememberVicoScrollState(scrollEnabled = true),
            zoomState = rememberVicoZoomState(
                zoomEnabled = true,
                initialZoom = Zoom.Content,
                minZoom = Zoom.Content,
                maxZoom = Zoom.fixed(20f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
    }
}

