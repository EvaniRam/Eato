package com.evani.homechefai.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.evani.homechefai.R
import com.evani.homechefai.data.model.UserPreferences
import com.evani.homechefai.data.PreferencesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit,
    preferencesManager: PreferencesManager
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedDietType by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1) / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
        )

        // Welcome text for first step
        if (currentStep == 0) {
            Text(
                text = "Welcome to Eato!",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Let's personalize your cooking experience",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // Question content
        when (currentStep) {
            0 -> DietTypeStep(
                selectedDietType = selectedDietType,
                onDietTypeSelected = { selectedDietType = it }
            )
            1 -> CuisineStep(
                selectedCuisine = selectedCuisine,
                onCuisineSelected = { selectedCuisine = it }
            )
            2 -> CountryStep(
                selectedCountry = selectedCountry,
                onCountrySelected = { selectedCountry = it }
            )
            3 -> RegionStep(
                selectedCountry = selectedCountry,
                selectedRegion = selectedRegion,
                onRegionSelected = { selectedRegion = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentStep > 0) {
                Button(onClick = { currentStep-- }) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(96.dp))
            }

            Button(
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        // Complete onboarding
                        val preferences = UserPreferences(
                            dietType = selectedDietType,
                            cuisine = selectedCuisine,
                            country = selectedCountry,
                            region = selectedRegion
                        )
                        onComplete(preferences)
                    }
                },
                enabled = when (currentStep) {
                    0 -> selectedDietType.isNotEmpty()
                    1 -> selectedCuisine.isNotEmpty()
                    2 -> selectedCountry.isNotEmpty()
                    3 -> selectedRegion.isNotEmpty()
                    else -> false
                }
            ) {
                Text(if (currentStep < 3) "Next" else "Get Started")
            }
        }
    }
}

@Composable
private fun DietTypeStep(
    selectedDietType: String,
    onDietTypeSelected: (String) -> Unit
) {
    StepContent(
        title = "What's your dietary preference?",
        options = listOf("Vegetarian", "Vegan", "Non-Vegetarian", "No Preference"),
        selectedOption = selectedDietType,
        onOptionSelected = onDietTypeSelected
    )
}

@Composable
private fun CuisineStep(
    selectedCuisine: String,
    onCuisineSelected: (String) -> Unit
) {
    StepContent(
        title = "What cuisine do you prefer?",
        options = listOf(
            "Indian",
            "Italian",
            "Chinese",
            "Japanese",
            "Mexican",
            "Mediterranean",
            "American",
            "Thai"
        ),
        selectedOption = selectedCuisine,
        onOptionSelected = onCuisineSelected
    )
}

@Composable
private fun CountryStep(
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    StepContent(
        title = "Select your country",
        options = listOf(
            "India",
            "USA",
            "China",
            "Italy",
            "Japan",
            "Mexico",
            "Thailand",
            "Greece"
        ),
        selectedOption = selectedCountry,
        onOptionSelected = onCountrySelected
    )
}

@Composable
private fun RegionStep(
    selectedCountry: String,
    selectedRegion: String,
    onRegionSelected: (String) -> Unit
) {
    val regions = when (selectedCountry) {
        "India" -> listOf("North Indian", "South Indian", "East Indian", "West Indian")
        "USA" -> listOf("Southern", "Midwest", "Northeast", "Southwest", "Pacific Northwest")
        "China" -> listOf("Sichuan", "Cantonese", "Hunan", "Shandong")
        "Italy" -> listOf("Tuscany", "Sicily", "Lombardy", "Naples")
        "Japan" -> listOf("Kanto", "Kansai", "Hokkaido", "Kyushu")
        "Mexico" -> listOf("Northern", "Central", "Southern", "Yucatan")
        "Thailand" -> listOf("Central", "Northern", "Southern", "Northeastern")
        "Greece" -> listOf("Macedonia", "Peloponnese", "Crete", "Aegean Islands")
        else -> listOf("No specific region")
    }

    StepContent(
        title = "Select your region in ${selectedCountry.ifEmpty { "your country" }}",
        options = regions,
        selectedOption = selectedRegion,
        onOptionSelected = onRegionSelected
    )
}

@Composable
private fun StepDescription(step: Int) {
    val description = when (step) {
        0 -> "This helps us suggest recipes that match your dietary needs"
        1 -> "We'll customize recipe suggestions based on your preferred cuisine"
        2 -> "This helps us understand your local ingredients availability"
        3 -> "Regional preferences help us suggest authentic local recipes"
        else -> ""
    }
    
    if (description.isNotEmpty()) {
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun StepContent(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    Button(
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (option == selectedOption)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
} 