package com.caisse.pos.data.model

// 8 produits en dur, prix en millimes (1 DT = 1000 millimes)
data class Product(
    val id: String,
    val name: String,
    val priceCents: Int
)

object Catalog {
    val products: List<Product> = listOf(
        Product("p1", "Café direct", 2500),
        Product("p2", "Thé à la menthe", 2000),
        Product("p3", "Brik à l'œuf", 3500),
        Product("p4", "Fricassé", 3000),
        Product("p5", "Chapati thon", 4500),
        Product("p6", "Lablabi", 5000),
        Product("p7", "Citronnade", 2500),
        Product("p8", "Makroudh", 1500)
    )

    fun byId(id: String): Product? = products.find { it.id == id }
}
