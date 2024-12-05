package com.godongijo.ecotainment.models

data class Transaction (
    val id: Int,
    val userId: String,
    val totalAmount: Int,
    val status: String,
    val items: List<TransactionItem>,
    val createdAt: String,
    val updatedAt: String,
)