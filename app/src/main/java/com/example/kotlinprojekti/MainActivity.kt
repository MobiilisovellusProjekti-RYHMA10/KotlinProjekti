package com.example.kotlinprojekti

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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



// Assuming RetrofitClient and Country are defined elsewhere
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = viewModel()

            val countries by mainViewModel.filteredCountries.collectAsState()
            val searchQuery by mainViewModel.searchQuery.collectAsState()
            val sortOrder by mainViewModel.sortOrder.collectAsState() // Observe sortOrder

            CountryListScreen(
                countries = countries,
                searchQuery = searchQuery,
                sortOrder = sortOrder, // Pass sortOrder to Composable
                onSearchQueryChanged = { query -> mainViewModel.setSearchQuery(query) },
                onSortOrderChanged = { order -> mainViewModel.setSortOrder(order) } // Handle sortOrder change
            )
        }

    }
}

@Composable
fun CountryListScreen(
    countries: List<Country>,
    searchQuery: String,
    sortOrder: SortOrder, // Add sortOrder parameter
    onSearchQueryChanged: (String) -> Unit,
    onSortOrderChanged: (SortOrder) -> Unit // Add onSortOrderChanged callback
) {
    Column {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search countries") },
            singleLine = true
        )

        // Example Sorting UI
        Row(modifier = Modifier.padding(8.dp)) {
            Button(onClick = { onSortOrderChanged(SortOrder.ASCENDING) }) {
                Text("Sort Ascending")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSortOrderChanged(SortOrder.DESCENDING) }) {
                Text("Sort Descending")
            }
        }

        LazyColumn {
            items(countries) { country ->
                CountryRow(country = country)
            }
        }
    }
}


@Composable
fun CountryRow(country: Country) {
    val populationFormatted = "%,d".format(country.population)

    Row(
        modifier = Modifier
            .padding(all = 8.dp)
            .fillMaxWidth()
    ) {
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
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = country.name)
            Text(text = "Capital: ${country.capital ?: "Unknown"}")
            // Display the population
            Text(text = "Population: $populationFormatted")
        }
    }
}

enum class SortOrder { ASCENDING, DESCENDING, NONE }

class MainViewModel : ViewModel() {
    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.NONE)


    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()


    val filteredCountries = combine(_countries, _searchQuery, _sortOrder) { countries, query, sortOrder ->
        // Filter first
        var filtered = if (query.isEmpty()) countries else countries.filter {
            it.name.contains(query, ignoreCase = true)
        }
        // Then sort
        filtered = when (sortOrder) {
            SortOrder.ASCENDING -> filtered.sortedBy { it.population }
            SortOrder.DESCENDING -> filtered.sortedByDescending { it.population }
            else -> filtered // SortOrder.NONE
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }


    init {
        fetchCountries()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

                private fun fetchCountries() {
                    viewModelScope.launch {
                        try {
                            val response = RetrofitClient.instance.getAllCountries()
                            if (response.isSuccessful) {
                                val countriesList = response.body() ?: listOf()
                                _countries.value = countriesList

                                // Log each country's flag URL
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



