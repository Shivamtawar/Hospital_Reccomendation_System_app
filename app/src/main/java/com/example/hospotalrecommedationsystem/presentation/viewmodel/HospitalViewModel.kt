package com.example.hospotalrecommedationsystem.presentation.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hospotalrecommedationsystem.data.model.RecommendationResponse
import com.example.hospotalrecommedationsystem.data.repository.HospitalRepository
import com.example.hospotalrecommedationsystem.util.LocationUtils
import com.example.hospotalrecommedationsystem.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HospitalViewModel @Inject constructor(
    private val repository: HospitalRepository,
    private val locationUtils: LocationUtils
) : ViewModel() {
    private val _recommendations = MutableStateFlow<Resource<RecommendationResponse>>(Resource.Idle)
    val recommendations: StateFlow<Resource<RecommendationResponse>> = _recommendations

    val latitude = mutableStateOf<Double?>(null)
    val longitude = mutableStateOf<Double?>(null)

    fun requestLocation(onLocationResult: (Double?, Double?) -> Unit) {
        viewModelScope.launch {
            locationUtils.getCurrentLocation { lat, lng ->
                latitude.value = lat
                longitude.value = lng
                onLocationResult(lat, lng)
            }
        }
    }

    fun getRecommendations(latitude: Double, longitude: Double, disease: String, topN: Int = 5) {
        viewModelScope.launch {
            if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) {
                _recommendations.value = Resource.Error("Invalid coordinates: latitude must be -90 to 90, longitude -180 to 180")
                return@launch
            }
            if (disease.isBlank()) {
                _recommendations.value = Resource.Error("Disease cannot be empty")
                return@launch
            }
            _recommendations.value = Resource.Loading
            _recommendations.value = repository.getRecommendations(latitude, longitude, disease, topN)
        }
    }
}