package com.caisse.pos.data.model

data class CartLine(
    val product: Product,
    val quantity: Int
) {
    val lineTotalCents: Int get() = product.priceCents * quantity
}

data class CartSnapshot(
    val lines: List<CartLine> = emptyList()
) {
    val totalCents: Int get() = lines.sumOf { it.lineTotalCents }
    val isEmpty: Boolean get() = lines.isEmpty()
}
