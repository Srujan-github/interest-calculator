package dev.srujan.interestcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.srujan.interestcalculator.ui.theme.InterestCalculatorTheme

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

// add error text
// colse keyboard when calculate cliked
// add success confetti animation
@Composable
fun CalculatorApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Simple Interest", "Compound Interest")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Interest Calculator", fontSize = 28.sp, modifier = Modifier.padding(bottom = 8.dp))

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

        when (selectedTab) {
            0 -> SimpleInterestCalculator()
            1 -> CompoundInterestCalculator()
        }
    }
}


@Composable
fun SimpleInterestCalculator() {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("10") }
    var time by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var error by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = principal,
            onValueChange = { principal = it },
            label = { Text("Principal (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            supportingText = {
                if (error && principal.isBlank()) Text("Enter the principal amount",color = MaterialTheme.colorScheme.error)
            },
            isError = if (principal.isBlank()) error else false
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            supportingText = {
                if (error && time.isBlank()) Text("Enter the time",color = MaterialTheme.colorScheme.error)
            },
            isError = if (time.isBlank()) error else false
        )

        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it },
            label = { Text("Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            supportingText = {
                if (error && rate.isBlank()) Text("Enter the rate",color = MaterialTheme.colorScheme.error)
            },
            isError = if (rate.isBlank()) error else false
        )

        Slider(
            value = rate.toFloatOrNull() ?: 0f,
            onValueChange = { rate = it.toString() },
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth(),

            )
        Button(onClick = {
            if (principal.isBlank() || time.isBlank() || rate.isBlank()) {
                error = true
            } else {
                val p = principal.toDoubleOrNull() ?: 0.0
                val r = rate.toDoubleOrNull() ?: 0.0
                val t = time.toDoubleOrNull() ?: 0.0
                val interest = p * r * t / 100
                val total = p + interest
                result = Pair(interest, total)


                error = false

            }
        }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Calculate")
        }

        result?.let { (interest, total) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Interest: ₹${"%.2f".format(interest)}", fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Total Amount: ₹${"%.2f".format(total)}", fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun CompoundInterestCalculator() {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = principal,
            onValueChange = { principal = it },
            label = { Text("Principal (₹)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it },
            label = { Text("Rate (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = frequency,
            onValueChange = { frequency = it },
            label = { Text("Compounds per year") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Button(onClick = {
            val p = principal.toDoubleOrNull() ?: 0.0
            val r = rate.toDoubleOrNull() ?: 0.0
            val t = time.toDoubleOrNull() ?: 0.0
            val n = frequency.toIntOrNull() ?: 1
            val amount = p * (1 + r / (100 * n)).pow(n * t)
            val interest = amount - p
            result = Pair(interest, amount)
        }, modifier = Modifier.padding(top = 12.dp)) {
            Text("Calculate")
        }

        result?.let { (interest, total) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Interest: ₹${"%.2f".format(interest)}", fontSize = 20.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Total Amount: ₹${"%.2f".format(total)}", fontSize = 20.sp)
                }
            }
        }
    }
}
