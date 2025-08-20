package com.example.group9temperatureconverter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.group9temperatureconverter.ui.theme.Group9TemperatureConverterTheme
import com.example.group9temperatureconverter.ui.theme.Pink80
import com.example.group9temperatureconverter.ui.theme.Red80
import com.example.group9temperatureconverter.ui.theme.SunYellow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Group9TemperatureConverterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    App()
                }
            }
        }
    }
}

enum class TempUnit(val label: String) { Celsius("°C"), Fahrenheit("°F"), Kelvin("K") }

@Composable
fun App() {
    val gradient = Brush.linearGradient(listOf(Red80, SunYellow, Pink80))
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(16.dp)
    ) {
        ConverterCard()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterCard() {
    val ctx = LocalContext.current
    val clipboard: ClipboardManager = LocalClipboardManager.current

    var input by remember { mutableStateOf("") }
    var from by remember { mutableStateOf(TempUnit.Celsius) }
    var to by remember { mutableStateOf(TempUnit.Fahrenheit) }
    var showError by remember { mutableStateOf(false) }

    val converted = remember(input, from, to) {
        val v = input.toDoubleOrNull()
        if (v == null) null else convertTemperature(v, from, to)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6E7).copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Temperature Converter",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFB00020),
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                "Type a value, pick units, and see the result instantly.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.85f),
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = input,
                onValueChange = {
                    input = it.replace(',', '.')
                    showError = input.isNotEmpty() && input.toDoubleOrNull() == null
                },
                label = { Text("Enter value") },
                supportingText = {
                    AnimatedVisibility(visible = showError) {
                        Text("Please enter a valid number")
                    }
                },
                isError = showError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Box(
                        Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF1744))
                    )
                }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                UnitPicker(title = "From", selected = from, onSelect = { from = it })
                UnitPicker(title = "To", selected = to, onSelect = { to = it })
            }

            ResultCard(converted)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AssistChip(
                    onClick = { val tmp = from; from = to; to = tmp },
                    label = { Text("Swap Units") },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                )
                AssistChip(
                    onClick = { input = "" },
                    label = { Text("Clear") }
                )
                AssistChip(
                    enabled = converted != null,
                    onClick = {
                        val text = converted?.let { formatResult(it, to) } ?: return@AssistChip
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(text))
                        Toast.makeText(ctx, "Result copied", Toast.LENGTH_SHORT).show()
                    },
                    label = { Text("Copy") },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                )
            }

            Spacer(Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFFF8A80), SunYellow.copy(alpha = 0.9f), Pink80)
                        )
                    )
                    .clickable { /* Easter egg */ }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Made with ❤️ in Kotlin", fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitPicker(
    title: String,
    selected: TempUnit,
    onSelect: (TempUnit) -> Unit,
    modifier: Modifier = Modifier // <-- Add Modifier parameter
) {

    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(title, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp, bottom = 4.dp))
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selected.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Unit") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                TempUnit.values().forEach { unit ->
                    DropdownMenuItem(text = { Text(unit.name) }, onClick = {
                        onSelect(unit); expanded = false
                    })
                }
            }
        }
    }
}

@Composable
fun ResultCard(converted: Double?) {
    val bg = Brush.verticalGradient(
        listOf(Color(0xFFFFEBEE), Color(0xFFFFFDE7), Color(0xFFFFF0F7))
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(Modifier.background(bg).padding(18.dp)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (converted == null) {
                    Text("—", fontSize = 28.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                } else {
                    Text(
                        text = String.format("%.2f", converted),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFAD1457),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun convertTemperature(value: Double, from: TempUnit, to: TempUnit): Double {
    val c = when (from) {
        TempUnit.Celsius -> value
        TempUnit.Fahrenheit -> (value - 32) * 5 / 9
        TempUnit.Kelvin -> value - 273.15
    }
    return when (to) {
        TempUnit.Celsius -> c
        TempUnit.Fahrenheit -> c * 9 / 5 + 32
        TempUnit.Kelvin -> c + 273.15
    }
}

fun formatResult(value: Double, unit: TempUnit): String =
    when (unit) {
        TempUnit.Celsius -> String.format("%.2f °C", value)
        TempUnit.Fahrenheit -> String.format("%.2f °F", value)
        TempUnit.Kelvin -> String.format("%.2f K", value)
    }
