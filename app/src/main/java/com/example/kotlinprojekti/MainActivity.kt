package com.example.kotlinprojekti

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Obtain an instance of MainViewModel
            val mainViewModel: MainViewModel = viewModel()
            val countries by mainViewModel.countries.collectAsState()

            // Call CountryListScreen Composable function with countries
            CountryListScreen(countries = countries)
        }
    }
}

@Composable
fun CountryListScreen(countries: List<Country>) {
    LazyColumn {
        items(countries) { country ->
            Row {
                Image(
                    painter = rememberAsyncImagePainter(model = country.flagUrl),
                    contentDescription = "Flag of ${country.name}",
                    modifier = Modifier.size(50.dp),
                    contentScale = ContentScale.FillBounds
                )

                Text(text = "${country.name}, Capital: ${country.capital ?: "Unknown"}")
            }
        }
    }
}

class MainViewModel : ViewModel() {
    private val _countries = MutableStateFlow<List<Country>>(emptyList())
    val countries = _countries.asStateFlow()

    init {
        fetchCountries()
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
                        Log.d("MainViewModel", "Country: ${country.name}, Flag URL: ${country.flagUrl}")
                    }

                } else {
                    Log.e("MainViewModel", "API call failed with response code: ${response.code()} and message: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "API call failed with exception: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}
