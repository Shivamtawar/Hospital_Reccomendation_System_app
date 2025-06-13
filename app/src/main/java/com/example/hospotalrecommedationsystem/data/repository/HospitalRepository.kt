package com.example.hospotalrecommedationsystem.data.repository

import com.example.hospotalrecommedationsystem.data.model.RecommendationResponse
import com.example.hospotalrecommedationsystem.util.Resource


interface HospitalRepository {
    suspend fun getRecommendations(latitude: Double, longitude: Double, disease: String, topN: Int): Resource<RecommendationResponse>
    suspend fun checkHealth(): Resource<Map<String, Any>>
}