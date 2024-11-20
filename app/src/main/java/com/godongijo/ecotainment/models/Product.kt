package com.godongijo.ecotainment.models

import com.google.firebase.Timestamp

// Replace with your actual package name

data class Product(
    val id: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val description: String = "",
    val rating: Double = 0.0,
    val totalSales: Int = 0,
    val lastUpdate: String = "",
    val reviews: List<Review>
)

