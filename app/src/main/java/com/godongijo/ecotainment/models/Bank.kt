package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bank(
    val id: Int,
    val name: String?,
    val logo: String?,
    val accountNumber: String?,
    val accountHolder: String?,
    val paymentInstructions: String?,
    val createdAt: String?,
    val updatedAt: String?
) : Parcelable