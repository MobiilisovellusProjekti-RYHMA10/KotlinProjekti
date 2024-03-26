package com.example.kotlinprojekti

import retrofit2.http.GET
import retrofit2.Response

// Tämä rajapinta kertoo Retrofitille, miten hakea tietoa kaikista maista.
interface RestCountriesApiService {
    // '@GET("all")' kertoo, että haetaan tietoa käyttämällä HTTP GET -pyyntöä ja "all" polkua perus-URL:n jälkeen.
    // Funktio palauttaa vastauksena listan maita.
    @GET("all")
    suspend fun getAllCountries(): Response<List<Country>>
}

