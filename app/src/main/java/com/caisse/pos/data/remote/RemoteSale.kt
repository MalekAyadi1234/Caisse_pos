package com.caisse.pos.data.remote

// ce qu'on envoie dans Firebase sous sales/{saleId}
data class RemoteSale(
    val saleId: String = "",
    val ticketNumber: String = "",
    val deviceId: String = "",
    val sequence: Long = 0L,
    val totalCents: Int = 0,
    val linesJson: String = "",
    val createdAtEpochMs: Long = 0L,
    val printStatus: String = "PENDING",
    val updatedAtEpochMs: Long = 0L
)
