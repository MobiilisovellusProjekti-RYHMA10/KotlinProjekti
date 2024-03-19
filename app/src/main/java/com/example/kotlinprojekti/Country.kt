package com.example.kotlinprojekti
import com.squareup.moshi.Json

data class Country(
    @Json(name = "name") val name: String,
    @Json(name = "capital") val capital: String?,
    @Json(name = "flag") val flagUrl: String, // Assuming "flag" is the property in the JSON
    @Json(name = "population") val population: Long // Add this line. Use String if your API returns population as a formatted string.

// Ensure all fields that you're using are correctly annotated
)