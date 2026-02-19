package com.clustertracker.app.domain.model

import java.time.LocalDate

data class Cycle(
    val id: Long = 0,
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val notes: String? = null
)
