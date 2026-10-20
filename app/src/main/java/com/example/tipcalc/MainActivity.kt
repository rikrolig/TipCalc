package com.example.tipcalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tipcalc.ui.theme.TipCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TipCalcTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DemoScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DemoScreen(modifier: Modifier = Modifier) {
    var billInput by remember { mutableStateOf("") }
    var dishesInput by remember { mutableStateOf("") }
    var tipPercent by remember { mutableFloatStateOf(10f) }
    var selectedDiscount by remember { mutableIntStateOf(1) }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Калькулятор чаевых", fontSize = 24.sp)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = billInput,
            onValueChange = { billInput = it },
            label = { Text("Сумма счёта") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = dishesInput,
            onValueChange = { newValue ->
                dishesInput = newValue
                // Автоматический выбор скидки при вводе
                val dishes = newValue.trim().toIntOrNull() ?: 0
                selectedDiscount = when {
                    dishes <= 2 -> 1
                    dishes <= 5 -> 2
                    dishes <= 10 -> 3
                    else -> 4
                }
            },
            label = { Text("Количество блюд") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Чаевые: ${tipPercent.toInt()}%")
        Slider(
            value = tipPercent,
            onValueChange = { tipPercent = it },
            valueRange = 0f..25f,
            steps = 24,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Text("Скидка (выбирается автоматически):", fontSize = 16.sp)
        Column(Modifier.fillMaxWidth()) {
            RadioButtonWithLabel(1, "1–2 блюда — 3%", selectedDiscount) { selectedDiscount = it }
            RadioButtonWithLabel(2, "3–5 блюд — 5%", selectedDiscount) { selectedDiscount = it }
            RadioButtonWithLabel(3, "6–10 блюд — 7%", selectedDiscount) { selectedDiscount = it }
            RadioButtonWithLabel(4, "более 10 блюд — 10%", selectedDiscount) { selectedDiscount = it }
        }
        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val dishes = dishesInput.trim().toIntOrNull() ?: 0
            selectedDiscount = when {
                dishes <= 2 -> 1
                dishes <= 5 -> 2
                dishes <= 10 -> 3
                else -> 4
            }
            result = calculate(billInput, dishesInput, tipPercent.toDouble(), selectedDiscount)
        }) {
            Text("Рассчитать")
        }
        Spacer(Modifier.height(16.dp))

        Text(text = result, fontSize = 18.sp)
    }
}

@Composable
fun RadioButtonWithLabel(
    index: Int,
    label: String,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected == index,
            onClick = { onSelected(index) },
            enabled = false
        )
        Text(label)
    }
}

fun calculate(
    billInput: String,
    dishesInput: String,
    tipPercent: Double,
    discountCode: Int
): String {
    val bill = billInput.trim().replace(',', '.').toDoubleOrNull()
        ?: return "Ошибка: сумма счёта должна быть числом"

    val dishes = dishesInput.trim().toIntOrNull()
        ?: return "Ошибка: количество блюд должно быть целым числом"

    if (bill <= 0) return "Ошибка: сумма счёта должна быть > 0"
    if (dishes <= 0) return "Ошибка: количество блюд должно быть > 0"

    val discountPercent = when (discountCode) {
        1 -> 3.0
        2 -> 5.0
        3 -> 7.0
        4 -> 10.0
        else -> 0.0
    }

    val discountAmount = bill * discountPercent / 100.0
    val billAfterDiscount = bill - discountAmount
    val tipAmount = billAfterDiscount * tipPercent / 100.0
    val total = billAfterDiscount + tipAmount

    return buildString {
        appendLine("Сумма счёта: %.2f".format(bill))
        appendLine("Количество блюд: $dishes")
        appendLine("Скидка: $discountPercent%% = %.2f".format(discountAmount))
        appendLine("Чаевые: $tipPercent%% = %.2f".format(tipAmount))
        appendLine("─".repeat(20))
        appendLine("Итого к оплате: %.2f".format(total))
    }
}