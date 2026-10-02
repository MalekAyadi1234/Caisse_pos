package com.caisse.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// juste le prochain n° de ticket sur cet appareil
@Entity(tableName = "ticket_counter")
data class TicketCounterEntity(
    @PrimaryKey val id: String = "local",
    val nextSequence: Long = 1L
)
