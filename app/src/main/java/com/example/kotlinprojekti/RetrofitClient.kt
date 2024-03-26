package com.example.kotlinprojekti

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

// Tämä koodi luo yhteyden nettisivulle, josta voi hakea tietoa eri maista.
object RetrofitClient {
    // Tässä kerrotaan, mistä nettiosoitteesta tiedot haetaan.
    private const val BASE_URL = "https://restcountries.com/v2/"

    // Moshi auttaa muuttamaan netistä saadut tiedot sellaiseen muotoon, että niitä on helppo käyttää ohjelmassa.
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // RetrofitClient käyttää Retrofit-kirjastoa, jotta ohjelma osaa puhua netin kanssa.
    // 'by lazy' tarkoittaa, että tämä valmistellaan käyttöön vasta, kun sitä ensimmäisen kerran tarvitaan.
    val instance: RestCountriesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)// Kertoo, mistä osoitteesta tiedot haetaan.
            .addConverterFactory(MoshiConverterFactory.create(moshi))// Käyttää Moshi-kirjastoa tiedon käsittelyyn.
            .build()// Rakentaa yhteyden.
            .create(RestCountriesApiService::class.java) // Luo työkalun, jolla tehdään kyselyjä nettisivulle.
    }
}
