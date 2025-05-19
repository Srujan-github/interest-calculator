package dev.srujan.interestcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.srujan.interestcalculator.ui.theme.InterestCalculatorTheme
import kotlinx.coroutines.launch
import kotlin.math.pow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InterestCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorApp()
                }
            }
        }
    }
}

@Composable
fun CalculatorApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Simple Interest", "Compound Interest")

    // Hoist the scroll state and scrollable area height
    val scrollState = rememberScrollState()
    var scrollableAreaHeight by remember { mutableIntStateOf(0) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Interest Calculator",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Apply the scroll modifier here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coordinates ->
                    // Update the scrollable area height (still useful to know the container size)
                    scrollableAreaHeight = coordinates.size.height
                }
        ) {
            when (selectedTab) {
                0 -> SimpleInterestCalculator(scrollState = scrollState) // Removed scrollableContainerHeight parameter as it's not strictly needed for scrolling to bottom
                1 -> CompoundInterestCalculator(scrollState = scrollState) // Removed scrollableContainerHeight parameter
            }
        }
    }
}

@Composable
fun SimpleInterestCalculator(scrollState: ScrollState) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10") }
    var time by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var principalError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(bottom = 16.dp) // Added extra padding at the bottom
    ) {
        OutlinedTextField(
            value = principal,
            onValueChange = {
                principal = it
                principalError = false // Clear error on change
            },
            label = { Text("Principal (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (principalError) {
                    Text(
                        "Enter the principal amount",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = principalError
        )

        OutlinedTextField(
            value = time,
            onValueChange = {
                time = it
                timeError = false // Clear error on change
            },
            label = { Text("Time (years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (timeError) {
                    Text("Enter the time", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = timeError
        )

        OutlinedTextField(
            value = rate,
            onValueChange = {
                rate = it
                rateError = false // Clear error on change
            },
            label = { Text("Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (rateError) {
                    Text("Enter the rate", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = rateError
        )

        Slider(
            value = rate.toFloatOrNull() ?: 0f,
            onValueChange = { rate = "%.2f".format(it) }, // Format to 2 decimal places
            valueRange = 0f..100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
        // Display current rate value from slider
        Text(
            "Current Rate: ${rate}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start)
        )


        Button(onClick = {
            principalError = principal.isBlank()
            timeError = time.isBlank()
            rateError = rate.isBlank()

            if (!principalError && !timeError && !rateError) {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = rate.toDoubleOrNull() ?: 0.0
                val t = time.toDoubleOrNull() ?: 0.0
                val interest = p * r * t / 100
                val total = p + interest
                result = Pair(interest, total)
                keyboardController?.hide() // Hide keyboard on successful calculation

                // Scroll to the bottom of the page
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }

            } else {
                result = null // Clear previous result if there's an error
            }
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Calculate")
        }

        result?.let { (interest, total) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp), // Increased top padding
                shape = MaterialTheme.shapes.medium, // Using medium shape
                elevation = CardDefaults.cardElevation(4.dp) // Slightly less elevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Simple Interest: ₹${"%.2f".format(interest)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Total Amount: ₹${"%.2f".format(total)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}


@Composable
fun CompoundInterestCalculator(scrollState: ScrollState) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("5") } // Default rate for compound interest
    var time by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    var principalError by remember { mutableStateOf(false) }
    var rateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var frequencyError by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .padding(bottom = 16.dp) // Added extra padding at the bottom
    ) {
        OutlinedTextField(
            value = principal,
            onValueChange = {
                principal = it
                principalError = false
            },
            label = { Text("Principal (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (principalError) {
                    Text(
                        "Enter the principal amount",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = principalError
        )
        OutlinedTextField(
            value = time,
            onValueChange = {
                time = it
                timeError = false
            },
            label = { Text("Time (years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (timeError) {
                    Text("Enter the time period", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = timeError
        )
        OutlinedTextField(
            value = frequency,
            onValueChange = {
                frequency = it
                frequencyError = false
            },
            label = { Text("Compounds per year") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (frequencyError) {
                    Text(
                        "Enter the compounding frequency",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            isError = frequencyError
        )

        OutlinedTextField(
            value = rate,
            onValueChange = {
                rate = it
                rateError = false
            },
            label = { Text("Annual Rate (%)") }, // More descriptive label
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            supportingText = {
                if (rateError) {
                    Text("Enter the annual rate", color = MaterialTheme.colorScheme.error)
                }
            },
            isError = rateError
        )

        // Add Slider for Rate in Compound Interest Calculator
        Slider(
            value = rate.toFloatOrNull() ?: 0f,
            onValueChange = { rate = "%.2f".format(it) }, // Format to 2 decimal places
            valueRange = 0f..100f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        )
        // Display current rate value from slider
        Text(
            "Current Rate: ${rate}%",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Start)
        )



        Button(onClick = {
            principalError = principal.isBlank()
            rateError = rate.isBlank()
            timeError = time.isBlank()
            frequencyError = frequency.isBlank()

            if (!principalError && !rateError && !timeError && !frequencyError) {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = rate.toDoubleOrNull() ?: 0.0
                val t = time.toDoubleOrNull() ?: 0.0
                val n = frequency.toIntOrNull() ?: 1

                // Ensure frequency is at least 1 to avoid division by zero
                val actualN = if (n > 0) n else 1

                val amount = p * (1 + r / (100.0 * actualN)).pow(actualN * t)
                val interest = amount - p
                result = Pair(interest, amount)
                keyboardController?.hide() // Hide keyboard on successful calculation

                // Scroll to the bottom of the page
                coroutineScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }


            } else {
                result = null // Clear previous result if there's an error
            }
        }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Calculate")
        }

        result?.let { (interest, total) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp), // Increased top padding
                shape = MaterialTheme.shapes.medium, // Using medium shape
                elevation = CardDefaults.cardElevation(4.dp) // Slightly less elevation
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Compound Interest: ₹${"%.2f".format(interest)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Total Amount: ₹${"%.2f".format(total)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}