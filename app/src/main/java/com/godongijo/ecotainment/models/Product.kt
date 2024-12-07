package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: Int = 0,
    val imageUrl: String = "",
    val name: String = "",
    val category: String = "",
    val price: Int = 0,
    val description: String = "",
    val rating: Double = 0.0,
    val totalSales: Int = 0,
    val createdAt: String,
    val updatedAt: String,
    val reviews: List<Review>? = null
): Parcelable

