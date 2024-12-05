package com.godongijo.ecotainment.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Address(
    val id: Int,
    val recipientName: String?,
    val phoneNumber: String?,
    val province: String?,
    val cityOrDistrict: String?,
    val detailAddress: String?,
    val createdAt: String?,
    val updatedAt: String?
) : Parcelable
