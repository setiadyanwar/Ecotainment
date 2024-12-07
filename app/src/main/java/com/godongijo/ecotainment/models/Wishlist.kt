package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Wishlist(
    val id: Int = 0,
    val userId: String = "",
    val productId: Int = 0,
    val createdAt: String,
    val updatedAt: String,
    val product: Product? = null
): Parcelable
