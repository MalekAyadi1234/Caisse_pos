package com.caisse.pos.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.caisse.pos.data.model.PrintStatus

// Vente en local. saleId = clé Firebase aussi, pour éviter les doublons au sync.
// ticketNumber = PREFIX-000042 (préfixe appareil + compteur local)
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val saleId: String,
    val ticketNumber: String,
    val deviceId: String,
    val sequence: Long,
    val totalCents: Int,
    val linesJson: String,
    val createdAtEpochMs: Long,
    val printStatus: PrintStatus,
    val synced: Boolean,
    val printAttempts: Int = 0
)
