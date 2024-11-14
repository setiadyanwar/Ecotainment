package com.godongijo.ecotainment.models

data class BankAccount(

    val bankLogo: Int,
    val accountNumber: String,
    val accountHolder: String,
    val paymentInstructions: List<String>
)