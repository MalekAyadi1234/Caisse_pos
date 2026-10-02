package com.caisse.pos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caisse.pos.data.local.SaleEntity
import com.caisse.pos.data.model.PrintStatus
import com.caisse.pos.data.repository.SaleRepository
import com.caisse.pos.util.Dates
import com.caisse.pos.util.Money
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class SaleRow(
    val saleId: String,
    val ticketNumber: String,
    val amountLabel: String,
    val statusLabel: String,
    val status: PrintStatus,
    val syncLabel: String,
    val dateLabel: String
)

class HistoryViewModel(
    repository: SaleRepository
) : ViewModel() {

    val rows: StateFlow<List<SaleRow>> = repository.sales
        .map { list -> list.map { it.toRow() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun SaleEntity.toRow() = SaleRow(
        saleId = saleId,
        ticketNumber = ticketNumber,
        amountLabel = Money.formatCents(totalCents),
        statusLabel = when (printStatus) {
            PrintStatus.PENDING -> "En attente"
            PrintStatus.PRINTED -> "Imprimé"
            PrintStatus.FAILED -> "Échec"
        },
        status = printStatus,
        syncLabel = if (synced) "Sync OK" else "Local",
        dateLabel = Dates.format(createdAtEpochMs)
    )

    class Factory(
        private val repository: SaleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HistoryViewModel(repository) as T
        }
    }
}
