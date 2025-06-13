package com.example.hospotalrecommedationsystem.data.remote


import com.example.hospotalrecommedationsystem.data.model.RecommendationRequest
import com.example.hospotalrecommedationsystem.data.model.RecommendationResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("/api/recommend")
    suspend fun getRecommendations(@Body request: RecommendationRequest): RecommendationResponse

    @GET("/api/recommend")
    suspend fun getRecommendations(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("disease") disease: String,
        @Query("top_n") topN: Int = 5
    ): RecommendationResponse

    @GET("/health")
    suspend fun checkHealth(): Map<String, Any>
}