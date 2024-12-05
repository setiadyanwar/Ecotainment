package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class User(
    val id: String,
    val email: String?,
    val username: String?,
    val phoneNumber: String?,
    val profilePicture: String?,
    val address: List<Address>? = null,
    val createdAt: String?,
    val updatedAt: String?
) : Parcelable
