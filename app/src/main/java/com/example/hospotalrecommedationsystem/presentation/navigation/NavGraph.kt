package com.example.hospotalrecommedationsystem.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.hospitalrecommendationsystem.presentation.screens.LoginScreen
import com.example.hospitalrecommendationsystem.presentation.screens.SignUpScreen
import com.example.hospotalrecommedationsystem.BottomNav
import com.example.hospotalrecommedationsystem.presentation.screens.HomeScreen

import com.example.hospotalrecommedationsystem.presentation.screens.ResultScreen

import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder

@Composable
fun NavGraph() {
    val navcontroller = rememberNavController()
    val Auth = FirebaseAuth.getInstance()
    val user = FirebaseAuth.getInstance().currentUser
    val start = if (user != null) "bottom" else "Signup"

    NavHost(navController = navcontroller, startDestination = start) {

        composable("Signup") {
            SignUpScreen(
                navController = navcontroller
            )

        }


        composable("login") {
            LoginScreen(
                navController = navcontroller
            )
        }

        composable("bottom") {
            BottomNav(
                navController = navcontroller
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToResults = { latitude, longitude, disease ->
                    navcontroller.navigate("results/$latitude/$longitude/$disease")
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToResults = { latitude, longitude, disease ->
                    navcontroller.navigate("results/$latitude/$longitude/$disease")
                }
            )
        }
        composable("results/{latitude}/{longitude}/{disease}") { backStackEntry ->
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0
            val disease = backStackEntry.arguments?.getString("disease")?.let { URLDecoder.decode(it, "UTF-8") } ?: ""
            ResultScreen(latitude, longitude, disease)
        }
    }
}