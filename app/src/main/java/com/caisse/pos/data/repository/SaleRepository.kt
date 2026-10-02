package com.caisse.pos.data.repository

import com.caisse.pos.data.local.AppDatabase
import com.caisse.pos.data.local.SaleEntity
import com.caisse.pos.data.model.CartLine
import com.caisse.pos.data.model.CartSnapshot
import com.caisse.pos.data.model.PrintStatus
import com.caisse.pos.data.remote.FirebaseSaleRemote
import com.caisse.pos.print.PrintResult
import com.caisse.pos.print.TicketPrinter
import com.caisse.pos.util.DeviceIdProvider
import com.caisse.pos.util.DeviceIdentity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Gère l'encaissement.
 * Ordre important : Room d'abord, panier vidé, puis print + Firebase en fond.
 */
class SaleRepository(
    private val db: AppDatabase,
    private val remote: FirebaseSaleRemote,
    private val printer: TicketPrinter,
    private val device: DeviceIdentity,
    private val appScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val saleDao = db.saleDao()
    private val counterDao = db.ticketCounterDao()
    private val checkoutMutex = Mutex()
    private val printMutex = Mutex()

    private val _online = MutableStateFlow(false)
    val online: StateFlow<Boolean> = _online.asStateFlow()

    val sales: Flow<List<SaleEntity>> = saleDao.observeAll()

    fun start() {
        appScope.launch {
            remote.observeConnectivity().collect { connected ->
                _online.value = connected
                if (connected) {
                    syncUnsyncedSales()
                }
            }
        }
        // au boot on réessaie les tickets pas encore imprimés
        appScope.launch {
            retryPendingOrFailedPrints()
        }
    }

    // écrit en local, rend la main, print/sync après
    suspend fun checkout(cart: CartSnapshot): Result<SaleEntity> = checkoutMutex.withLock {
        if (cart.isEmpty) return Result.failure(IllegalStateException("Panier vide"))

        val sequence = counterDao.allocateNextSequence()
        val ticketNumber = DeviceIdProvider.formatTicket(device.prefix, sequence)
        val saleId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entity = SaleEntity(
            saleId = saleId,
            ticketNumber = ticketNumber,
            deviceId = device.deviceId,
            sequence = sequence,
            totalCents = cart.totalCents,
            linesJson = linesToJson(cart.lines),
            createdAtEpochMs = now,
            printStatus = PrintStatus.PENDING,
            synced = false,
            printAttempts = 0
        )

        saleDao.insert(entity)

        // print + firebase en async, on bloque pas l'UI
        appScope.launch {
            printAndMaybeSync(entity.saleId)
        }

        Result.success(entity)
    }

    suspend fun retryPendingOrFailedPrints() {
        val toRetry = saleDao.getByPrintStatuses(
            listOf(PrintStatus.PENDING, PrintStatus.FAILED)
        )
        toRetry.forEach { sale ->
            printAndMaybeSync(sale.saleId)
        }
    }

    private suspend fun printAndMaybeSync(saleId: String) {
        printMutex.withLock {
            val sale = saleDao.getById(saleId) ?: return@withLock
            if (sale.printStatus == PrintStatus.PRINTED) return@withLock

            when (printer.print(sale)) {
                is PrintResult.Success -> {
                    saleDao.updatePrintStatus(saleId, PrintStatus.PRINTED)
                    pushIfPossible(sale.copy(printStatus = PrintStatus.PRINTED))
                }
                is PrintResult.Failure -> {
                    saleDao.updatePrintStatus(saleId, PrintStatus.FAILED)
                    pushIfPossible(sale.copy(printStatus = PrintStatus.FAILED))
                }
            }
        }
    }

    private suspend fun pushIfPossible(sale: SaleEntity) {
        try {
            remote.upsert(sale)
            saleDao.markSynced(sale.saleId)
        } catch (_: Exception) {
            // offline ou erreur → on réessaiera plus tard
        }
    }

    suspend fun syncUnsyncedSales() {
        saleDao.getUnsynced().forEach { sale ->
            try {
                remote.upsert(sale)
                saleDao.markSynced(sale.saleId)
            } catch (_: Exception) {
                // on retentera quand on sera online
            }
        }
    }

    private fun linesToJson(lines: List<CartLine>): String {
        val arr = JSONArray()
        lines.forEach { line ->
            arr.put(
                JSONObject()
                    .put("productId", line.product.id)
                    .put("name", line.product.name)
                    .put("unitPriceCents", line.product.priceCents)
                    .put("qty", line.quantity)
                    .put("lineTotalCents", line.lineTotalCents)
            )
        }
        return arr.toString()
    }
}
