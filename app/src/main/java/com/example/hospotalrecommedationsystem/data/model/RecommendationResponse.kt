package com.example.hospotalrecommedationsystem.data.model

data class RecommendationResponse(
    val success: Boolean,
    val count: Int,
    val hospitals: List<Hospital>,
    val search_params: RecommendationRequest
)