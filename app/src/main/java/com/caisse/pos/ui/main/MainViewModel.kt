package com.caisse.pos.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.caisse.pos.data.model.CartLine
import com.caisse.pos.data.model.CartSnapshot
import com.caisse.pos.data.model.Catalog
import com.caisse.pos.data.model.Product
import com.caisse.pos.data.repository.SaleRepository
import com.caisse.pos.util.Money
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val products: List<Product> = Catalog.products,
    val cart: CartSnapshot = CartSnapshot(),
    val totalLabel: String = Money.formatCents(0),
    val checkoutEnabled: Boolean = false,
    val online: Boolean = false,
    val devicePrefix: String = "",
    val checkingOut: Boolean = false
)

sealed class MainEvent {
    data class CheckoutSuccess(val ticketNumber: String, val amountLabel: String) : MainEvent()
    data class CheckoutError(val message: String) : MainEvent()
}

class MainViewModel(
    private val repository: SaleRepository,
    private val devicePrefix: String
) : ViewModel() {

    private val _state = MutableStateFlow(
        MainUiState(devicePrefix = devicePrefix)
    )
    val state: StateFlow<MainUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MainEvent>()
    val events: SharedFlow<MainEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.online.collect { online ->
                _state.update { it.copy(online = online) }
            }
        }
    }

    fun addProduct(product: Product) {
        mutateCart { lines ->
            val existing = lines.find { it.product.id == product.id }
            if (existing == null) {
                lines + CartLine(product, 1)
            } else {
                lines.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            }
        }
    }

    fun increase(productId: String) {
        mutateCart { lines ->
            lines.map {
                if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
            }
        }
    }

    fun decrease(productId: String) {
        mutateCart { lines ->
            lines.mapNotNull {
                when {
                    it.product.id != productId -> it
                    it.quantity <= 1 -> null
                    else -> it.copy(quantity = it.quantity - 1)
                }
            }
        }
    }

    fun checkout() {
        val cart = _state.value.cart
        if (cart.isEmpty || _state.value.checkingOut) return

        viewModelScope.launch {
            _state.update { it.copy(checkingOut = true) }
            val result = repository.checkout(cart)
            result.fold(
                onSuccess = { sale ->
                    // panier vidé tout de suite après l'écriture Room
                    _state.update {
                        it.copy(
                            cart = CartSnapshot(),
                            totalLabel = Money.formatCents(0),
                            checkoutEnabled = false,
                            checkingOut = false
                        )
                    }
                    _events.emit(
                        MainEvent.CheckoutSuccess(
                            ticketNumber = sale.ticketNumber,
                            amountLabel = Money.formatCents(sale.totalCents)
                        )
                    )
                },
                onFailure = { error ->
                    _state.update { it.copy(checkingOut = false) }
                    _events.emit(MainEvent.CheckoutError(error.message ?: "Erreur"))
                }
            )
        }
    }

    private fun mutateCart(transform: (List<CartLine>) -> List<CartLine>) {
        _state.update { current ->
            val next = CartSnapshot(transform(current.cart.lines))
            current.copy(
                cart = next,
                totalLabel = Money.formatCents(next.totalCents),
                checkoutEnabled = !next.isEmpty && !current.checkingOut
            )
        }
    }

    class Factory(
        private val repository: SaleRepository,
        private val devicePrefix: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, devicePrefix) as T
        }
    }
}
