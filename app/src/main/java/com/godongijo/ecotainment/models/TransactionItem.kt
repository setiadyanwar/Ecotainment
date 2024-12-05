package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionItem(
    val id: Int,
    val productId: Int,
    val quantity: Int,
    val product: Product? = null
) : Parcelable
