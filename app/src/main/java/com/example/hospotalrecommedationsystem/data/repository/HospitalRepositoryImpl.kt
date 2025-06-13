package com.example.hospotalrecommedationsystem.data.repository




import com.example.hospotalrecommedationsystem.data.model.RecommendationRequest
import com.example.hospotalrecommedationsystem.data.model.RecommendationResponse
import com.example.hospotalrecommedationsystem.data.remote.ApiService
import com.example.hospotalrecommedationsystem.util.Resource
import javax.inject.Inject

class HospitalRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : HospitalRepository {
    override suspend fun getRecommendations(
        latitude: Double,
        longitude: Double,
        disease: String,
        topN: Int
    ): Resource<RecommendationResponse> {
        return try {
            val response = apiService.getRecommendations(
                RecommendationRequest(latitude, longitude, disease, topN)
            )
            if (response.success) {
                Resource.Success(response)
            } else {
                Resource.Error("Failed to fetch recommendations")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }

    override suspend fun checkHealth(): Resource<Map<String, Any>> {
        return try {
            val response = apiService.checkHealth()
            Resource.Success(response)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}