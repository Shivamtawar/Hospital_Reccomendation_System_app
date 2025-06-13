package com.example.hospotalrecommedationsystem.data.model

data class RecommendationRequest(
    val latitude: Double,
    val longitude: Double,
    val disease: String,
    val top_n: Int = 5
)