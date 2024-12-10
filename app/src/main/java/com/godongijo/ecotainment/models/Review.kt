package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Review(
    val id: Int,
    val userId: String,
    val productId: Int,
    val rating: Int,
    val comment: String,
    val createdAt: String,
    val user: User? = null
) : Parcelable