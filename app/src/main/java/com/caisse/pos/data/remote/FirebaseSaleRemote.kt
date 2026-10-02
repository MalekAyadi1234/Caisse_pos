package com.caisse.pos.data.remote

import com.caisse.pos.data.local.SaleEntity
import com.caisse.pos.data.model.PrintStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Accès Firebase. On écrit avec saleId en clé → si on retry, pas de doublon.
 */
class FirebaseSaleRemote(
    private val database: FirebaseDatabase
) {
    private val salesRef get() = database.getReference("sales")

    suspend fun upsert(sale: SaleEntity) {
        val remote = RemoteSale(
            saleId = sale.saleId,
            ticketNumber = sale.ticketNumber,
            deviceId = sale.deviceId,
            sequence = sale.sequence,
            totalCents = sale.totalCents,
            linesJson = sale.linesJson,
            createdAtEpochMs = sale.createdAtEpochMs,
            printStatus = sale.printStatus.name,
            updatedAtEpochMs = System.currentTimeMillis()
        )
        salesRef.child(sale.saleId).setValue(remote).await()
    }

    suspend fun updatePrintStatus(saleId: String, status: PrintStatus) {
        salesRef.child(saleId).child("printStatus").setValue(status.name).await()
        salesRef.child(saleId).child("updatedAtEpochMs").setValue(System.currentTimeMillis()).await()
    }

    fun observeConnectivity(): Flow<Boolean> = callbackFlow {
        val connectedRef = database.getReference(".info/connected")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) == true)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(false)
            }
        }
        connectedRef.addValueEventListener(listener)
        awaitClose { connectedRef.removeEventListener(listener) }
    }
}
