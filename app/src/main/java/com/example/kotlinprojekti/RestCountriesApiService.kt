package com.example.kotlinprojekti

import retrofit2.http.GET
import retrofit2.Response

interface RestCountriesApiService {
    @GET("all")
    suspend fun getAllCountries(): Response<List<Country>>
}

