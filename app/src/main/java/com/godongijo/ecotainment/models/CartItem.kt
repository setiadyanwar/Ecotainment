package com.godongijo.ecotainment.models

data class CartItem(
    val id: Int,
    val userId: String,
    val productId: Int,
    var quantity: Int,
    val createdAt: String,
    val updatedAt: String,
    val product: Product? // Product adalah objek terkait produk dalam cart
)
