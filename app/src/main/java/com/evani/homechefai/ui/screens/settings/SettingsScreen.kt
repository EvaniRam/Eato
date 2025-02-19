package com.evani.homechefai.ui.screens.settings

import com.evani.homechefai.data.model.UserPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.evani.homechefai.data.PreferencesManager
import com.evani.homechefai.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSaveSettings: (UserPreferences) -> Unit,
    preferencesManager: PreferencesManager
) {
    val scope = rememberCoroutineScope()
    val preferences = preferencesManager.userPreferencesFlow.collectAsState(
        initial = UserPreferences()
    ).value

    var selectedDietType by remember { mutableStateOf(preferences.dietType) }
    var selectedCuisine by remember { mutableStateOf(preferences.cuisine) }
    var selectedCountry by remember { mutableStateOf(preferences.country) }
    var selectedRegion by remember { mutableStateOf(preferences.region) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    
    // Expanded states for dropdowns
    var isDietExpanded by remember { mutableStateOf(false) }
    var isCuisineExpanded by remember { mutableStateOf(false) }
    var isCountryExpanded by remember { mutableStateOf(false) }
    var isRegionExpanded by remember { mutableStateOf(false) }

    // Get regions based on selected country
    val regions = remember(selectedCountry) {
        when (selectedCountry) {
            "India" -> listOf("North Indian", "South Indian", "East Indian", "West Indian")
            "USA" -> listOf("Southern", "Midwest", "Northeast", "Southwest", "Pacific Northwest")
            "China" -> listOf("Sichuan", "Cantonese", "Hunan", "Shandong")
            "Italy" -> listOf("Tuscany", "Sicily", "Lombardy", "Naples")
            "Japan" -> listOf("Kanto", "Kansai", "Hokkaido", "Kyushu")
            "Mexico" -> listOf("Northern", "Central", "Southern", "Yucatan")
            else -> emptyList()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.settings)) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // Only show save if any setting is selected
                if (selectedDietType.isNotEmpty() || selectedCuisine.isNotEmpty() || 
                    selectedCountry.isNotEmpty() || selectedRegion.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val newPreferences = UserPreferences(
                                dietType = selectedDietType,
                                cuisine = selectedCuisine,
                                country = selectedCountry,
                                region = selectedRegion
                            )
                            scope.launch {
                                preferencesManager.updatePreferences(newPreferences)
                                onSaveSettings(newPreferences)
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = "Save Settings"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Diet Type Dropdown
            ExposedDropdownMenuBox(
                expanded = isDietExpanded,
                onExpandedChange = { isDietExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedDietType.ifEmpty { stringResource(R.string.diet_preference) },
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDietExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = isDietExpanded,
                    onDismissRequest = { isDietExpanded = false }
                ) {
                    listOf("Vegetarian", "Non-Vegetarian", "Vegan", "Pescatarian").forEach { diet ->
                        DropdownMenuItem(
                            text = { Text(diet) },
                            onClick = {
                                selectedDietType = diet
                                isDietExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cuisine Type Dropdown
            ExposedDropdownMenuBox(
                expanded = isCuisineExpanded,
                onExpandedChange = { isCuisineExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCuisine.ifEmpty { stringResource(R.string.cuisine_type) },
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCuisineExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = isCuisineExpanded,
                    onDismissRequest = { isCuisineExpanded = false }
                ) {
                    listOf(
                        "Italian", "Chinese", "Indian", "Mexican", "Japanese",
                        "Thai", "French", "Mediterranean", "American", "Korean"
                    ).forEach { cuisine ->
                        DropdownMenuItem(
                            text = { Text(cuisine) },
                            onClick = {
                                selectedCuisine = cuisine
                                isCuisineExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Country Dropdown
            ExposedDropdownMenuBox(
                expanded = isCountryExpanded,
                onExpandedChange = { isCountryExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCountry.ifEmpty { stringResource(R.string.country) },
                    onValueChange = { },
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCountryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = isCountryExpanded,
                    onDismissRequest = { isCountryExpanded = false }
                ) {
                    listOf(
                        "USA", "Italy", "India", "China", "Japan",
                        "Mexico", "France", "Thailand", "Spain", "Greece"
                    ).forEach { country ->
                        DropdownMenuItem(
                            text = { Text(country) },
                            onClick = {
                                selectedCountry = country
                                isCountryExpanded = false
                            }
                        )
                    }
                }
            }

            // Add Region Dropdown if country is selected
            if (selectedCountry.isNotEmpty() && regions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                ExposedDropdownMenuBox(
                    expanded = isRegionExpanded,
                    onExpandedChange = { isRegionExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedRegion.ifEmpty { stringResource(R.string.region) },
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRegionExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )

                    ExposedDropdownMenu(
                        expanded = isRegionExpanded,
                        onDismissRequest = { isRegionExpanded = false }
                    ) {
                        regions.forEach { region ->
                            DropdownMenuItem(
                                text = { Text(region) },
                                onClick = {
                                    selectedRegion = region
                                    isRegionExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Advanced Settings
            Button(
                onClick = { showAdvancedSettings = !showAdvancedSettings },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Advanced Settings")
            }

            if (showAdvancedSettings) {
                // Add advanced settings options here
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Add advanced settings controls
                        Text("Advanced Settings Options")
                        // Add more advanced settings as needed
                    }
                }
            }
        }
    }
} 