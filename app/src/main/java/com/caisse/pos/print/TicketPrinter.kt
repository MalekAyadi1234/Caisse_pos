package com.caisse.pos.print

import android.util.Log
import com.caisse.pos.data.local.SaleEntity
import kotlinx.coroutines.delay
import kotlin.random.Random

// Pas d'imprimante réelle → j'écris juste dans Logcat.
// Tu peux mettre failureRate > 0 pour tester les échecs.
interface TicketPrinter {
    suspend fun print(sale: SaleEntity): PrintResult
}

sealed class PrintResult {
    data object Success : PrintResult()
    data class Failure(val reason: String) : PrintResult()
}

class SimulatedTicketPrinter(
    private val failureRate: Double = 0.0
) : TicketPrinter {

    override suspend fun print(sale: SaleEntity): PrintResult {
        delay(350)
        val body = buildString {
            appendLine("======== TICKET ========")
            appendLine("N° ${sale.ticketNumber}")
            appendLine("Total : ${"%.3f".format(sale.totalCents / 1000.0)} DT")
            appendLine(sale.linesJson)
            appendLine("========================")
        }
        Log.i(TAG, body)

        return if (failureRate > 0.0 && Random.nextDouble() < failureRate) {
            PrintResult.Failure("Erreur imprimante simulée")
        } else {
            PrintResult.Success
        }
    }

    companion object {
        private const val TAG = "TicketPrinter"
    }
}
