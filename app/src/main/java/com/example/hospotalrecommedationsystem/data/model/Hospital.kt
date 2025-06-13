package com.example.hospotalrecommedationsystem.data.model

data class Hospital(
    val Hospital_Name: String,
    val Address_Original_First_Line: String,
    val State: String,
    val District: String,
    val Pincode: String,
    val Telephone: String,
    val Mobile_Number: String,
    val Emergency_Num: String,
    val Facilities: String,
    val Distance: Double
)