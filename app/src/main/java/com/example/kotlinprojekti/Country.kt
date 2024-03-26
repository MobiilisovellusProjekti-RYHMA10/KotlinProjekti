package com.example.kotlinprojekti
import com.squareup.moshi.Json

// Country-luokka edustaa maata ja sen perustietoja. Sisältää tiedot maan nimestä, pääkaupungista, lipun URL-osoitteesta,
// väkiluvusta, pinta-alasta, valuutoista, kielistä ja maanosasta. @Json-annotaatiot määrittävät JSON-avainten vastaavuuden.
data class Country(
    @Json(name = "name") val name: String,
    @Json(name = "capital") val capital: String?,
    @Json(name = "flag") val flagUrl: String,
    @Json(name = "population") val population: Long,
    @Json(name = "area") val landArea: Double?,
    @Json(name = "currencies") val currencies: List<Currency>?,
    @Json(name = "languages") val languages: List<Language>?,
    @Json(name = "region") val region: String?
)

// Currency-luokka kuvaa valuutan tietoja, mukaan lukien valuuttakoodin, nimen ja symbolin.
// Käytetään osana Country-luokkaa.
data class Currency(
    @Json(name = "code") val code: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "symbol") val symbol: String?
)

// Language-luokka sisältää tiedot kielestä, mukaan lukien kielen nimen ja sen alkuperäisen nimen.
// Käytetään osana Country-luokkaa.
data class Language(
    @Json(name = "name") val name: String?,
    @Json(name = "nativeName") val nativeName: String?
)