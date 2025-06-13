package com.example.hospotalrecommedationsystem.data.model



data class Users(
    val id : String = "",
    val username: String = "",
    val imageUrl:String? = null,
    val email : String = "",
    val phoneNumber : String = "",
    val password: String = "",
    val therapist: Boolean = false,
    val bio: String = ""
)