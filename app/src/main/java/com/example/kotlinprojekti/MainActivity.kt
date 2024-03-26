package com.example.kotlinprojekti

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme






// Pääaktiviteetti, joka perii ComponentActivity-luokan. Tämä on sovelluksen pääsyöttökohta.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Asetetaan sisältö näytölle käyttäen Jetpack Composea.
        setContent {
            // Haetaan ViewModel, joka hallinnoi UI:n tilaa ja logiikkaa.
            val mainViewModel: MainViewModel = viewModel()

            // CollectAsState kerää flow't ja muuntaa ne Compose-yhteensopiviksi tiloiksi.
            val countries by mainViewModel.filteredCountries.collectAsState()
            val searchQuery by mainViewModel.searchQuery.collectAsState()
            val sortOrder by mainViewModel.sortOrder.collectAsState()

            // Kutsutaan CountryListScreen-funktiota näyttämään maiden lista,
            // hakukenttä ja järjestelytoiminnot.
            CountryListScreen(
                countries = countries,
                searchQuery = searchQuery,
                sortOrder = sortOrder,
                onSearchQueryChanged = { query -> mainViewModel.setSearchQuery(query) },
                onSortOrderChanged = { order -> mainViewModel.setSortOrder(order) }
            )
        }
    }
}

// Composable-funktio, joka näyttää maiden listan, hakukentän ja järjestelyn.
@Composable
fun CountryListScreen(
    countries: List<Country>,
    searchQuery: String,
    sortOrder: SortOrder,
    onSearchQueryChanged: (String) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit
) {
    Column {
        // Hakukenttä
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search countries") },
            singleLine = true
        )

        // Esimerkki järjestelykäyttöliittymästä
        Row(modifier = Modifier.padding(8.dp)) {
            // Järjestelyn valintapainikkeet
            Button(onClick = { onSortOrderChanged(SortOrder.ASCENDING) }) {
                Text("Ascending")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSortOrderChanged(SortOrder.DESCENDING) }) {
                Text("Descending")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSortOrderChanged(SortOrder.ALPHABETICAL) }) {
                Text("A-Z")
            }
        }

        // Näyttää maat LazyColumnissa, joka on tehokas tapa näyttää pitkä lista elementtejä.
        LazyColumn {
            items(countries) { country ->
                // Jokaiselle maalle oma rivi.
                CountryRow(country = country)
            }
        }
    }
}

// Composable-funktio, joka näyttää yksittäisen maan tiedot rivillä.
@Composable
fun CountryRow(country: Country) {
    // Tämän rivi-elementin laajennustila.
    var isExpanded by remember { mutableStateOf(false) }

    // Kun maata klikataan, laajennetaan tai piilotetaan lisätiedot.
    Column(modifier = Modifier
        .clickable { isExpanded = !isExpanded }
        .padding(8.dp)
        .fillMaxWidth()) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Maan lippu.
            Image(
                painter = rememberAsyncImagePainter(
                    model = country.flagUrl,
                    imageLoader = ImageLoader.Builder(LocalContext.current)
                        .components { add(SvgDecoder.Factory()) }
                        .build()
                ),
                contentDescription = "Flag of ${country.name}",
                modifier = Modifier
                    .size(width = 120.dp, height = 70.dp)
                    .padding(end = 8.dp),
                contentScale = ContentScale.FillBounds
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                // Maan nimi ja perustiedot.
                Text(text = country.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Capital: ${country.capital ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Population: ${"%,d".format(country.population)}", style = MaterialTheme.typography.bodyMedium)

                // Laajennettavissa oleva osio, joka näyttää lisätietoja maasta kun riviä klikataan.
                AnimatedVisibility(visible = isExpanded) {
                    Column {
                        // Maanosa
                        Text(text = "Region: ${country.region ?: "Unknown"}", style = MaterialTheme.typography.bodyMedium)
                        // Maan valuutta, näytetään ensimmäinen jos listassa on useita
                        country.currencies?.firstOrNull()?.let { currency ->
                            Text(text = "Currency: ${currency.name} (${currency.symbol})", style = MaterialTheme.typography.bodyMedium)
                        }
                        // Maan pinta-ala, jos tiedossa
                        country.landArea?.let {
                            Text(text = "Land Area: ${it} km²", style = MaterialTheme.typography.bodyMedium)
                        }
                        // Maan kielet, listattuna pilkulla erotettuna
                        country.languages?.joinToString(separator = ", ") { it.name ?: "" }?.let {
                            Text(text = "Languages: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}


// Järjestystyypit, joita voidaan käyttää maiden listauksen järjestämiseen.

enum class SortOrder { ASCENDING, DESCENDING, ALPHABETICAL, NONE }

// ViewModel, joka hallinnoi UI:n tilaa ja datan hakua.
class MainViewModel : ViewModel() {
    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.NONE)

    // Julkinen tila, jota UI voi tarkkailla.
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    // Filtteröityjen maiden lista, joka päivittyy kun hakuehtoja muutetaan.
    val filteredCountries = combine(_countries, _searchQuery, _sortOrder) { countries, query, sortOrder ->
        // Suodatetaan maat ensin hakuehdon perusteella.
        var filtered = if (query.isEmpty()) countries else countries.filter {
            it.name.contains(query, ignoreCase = true)
        }
        // Järjestetään suodatetut maat valitun järjestystyypin mukaan.
        filtered = when (sortOrder) {
            SortOrder.ASCENDING -> filtered.sortedBy { it.population }
            SortOrder.DESCENDING -> filtered.sortedByDescending { it.population }
            SortOrder.ALPHABETICAL -> filtered.sortedBy { it.name }
            else -> filtered
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Asetetaan uusi järjestystyyppi.
    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }


    // Alustaja, jossa käynnistetään maiden haku.
    init {
        fetchCountries()
    }

    // Asetetaan uusi hakuehto.
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Hakee maiden tiedot käyttäen Retrofit-kirjastoa.
    private fun fetchCountries() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAllCountries()
                if (response.isSuccessful) {
                    // Tallennetaan haetut maat tilaan.
                    val countriesList = response.body() ?: listOf()
                    _countries.value = countriesList

                    // Loggaa jokaisen maan lipun URL osoitteen.
                    countriesList.forEach { country ->
                        Log.d(
                            "MainViewModel",
                            "Country: ${country.name}, Flag URL: ${country.flagUrl}"
                        )
                    }

                } else {
                    Log.e(
                        "MainViewModel",
                        "API call failed with response code: ${response.code()} and message: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "API call failed with exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}