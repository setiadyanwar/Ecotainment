package com.godongijo.ecotainment.models

data class SearchHistory(
    val id: Int,
    val userId: String,
    val searchQuery: String,
    val createdAt: String,
    val updatedAt: String
)