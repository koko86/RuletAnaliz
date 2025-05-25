package com.example.ruletanaliz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RouletteAnalysisScreen()
        }
    }
}

@Composable
fun RouletteAnalysisScreen() {
    // Avrupa Ruleti renk paleti
    val bgDark = Color(0xFF1A1A1A) // Koyu kumarhane arka planı
    val textWhite = Color.White
    val rouletteGreen = Color(0xFF006400) // Klasik rulet masası yeşili
    val red = Color(0xFFC71515) // Rulet kırmızısı
    val black = Color(0xFF2F2F2F) // Rulet siyahı
    val gold = Color(0xFFFFD700) // Altın detaylar
    val goldGradient = Color(0x66FFD700) // Şeffaf altın
    val velvetRed = Color(0xFF8B0000) // Kadife kırmızı (detaylar için)
    val gradientBrush = Brush.verticalGradient(listOf(bgDark, Color.Black))

    var spinNumber by remember { mutableStateOf("") }
    var spinList by remember { mutableStateOf(listOf<Int>()) }
    var predictions by remember { mutableStateOf<SpinResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showError by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Error message animation
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            showError = true
            delay(3000)
            showError = false
            errorMessage = null
        }
    }

    fun fetchPredictions(newSpin: Int) {
        coroutineScope.launch {
            try {
                errorMessage = null
                val response = RetrofitClient.apiService.predictSpin(SpinRequest(newSpin))
                predictions = response
            } catch (e: HttpException) {
                errorMessage = "Sunucu hatası: ${e.message()}"
            } catch (e: IOException) {
                errorMessage = "Ağ hatası: ${e.message}"
            } catch (e: Exception) {
                errorMessage = "Beklenmeyen hata: ${e.message}"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(16.dp)
    ) {
        // Başlık: Kumarhane tarzı başlık
        Text(
            text = "Avrupa Ruleti Analiz Sistemi",
            color = gold,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .background(
                    Brush.linearGradient(listOf(velvetRed, Color.Black)),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
                .border(2.dp, gold, RoundedCornerShape(8.dp)),
            textAlign = TextAlign.Center
        )

        // Number History: Rulet çarkı tarzı görünüm
        AnimatedVisibility(
            visible = spinList.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(rouletteGreen.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.6f))
                        )
                    )
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shadow(8.dp, shape = RoundedCornerShape(12.dp))
                    .border(2.dp, goldGradient, RoundedCornerShape(12.dp))
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(spinList) { _, number ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    when {
                                        number == 0 -> rouletteGreen
                                        listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(number) -> red
                                        else -> black
                                    }, shape = CircleShape
                                )
                                .border(2.dp, gold, CircleShape)
                                .animateItemPlacement(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                color = textWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Input Section: Kadife dokulu giriş alanı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .background(velvetRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = spinNumber,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.toIntOrNull() in 0..36)) {
                        spinNumber = newValue
                    }
                },
                label = { Text("Son Spin (0-36)", color = gold) },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .animateContentSize(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = gold,
                    unfocusedBorderColor = goldGradient,
                    textColor = textWhite,
                    cursorColor = gold,
                    focusedLabelColor = gold,
                    unfocusedLabelColor = goldGradient
                ),
                singleLine = true,
                trailingIcon = {
                    if (spinNumber.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = gold,
                            modifier = Modifier.clickable { spinNumber = "" }
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    spinNumber.toIntOrNull()?.let {
                        spinList = spinList + it
                        fetchPredictions(it)
                        spinNumber = ""
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shadow(4.dp, RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = rouletteGreen, contentColor = textWhite)
            ) {
                Text("Ekle", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Error Message: Şık kumarhane tarzı bildirim
        AnimatedVisibility(
            visible = showError,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(containerColor = velvetRed.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, gold)
            ) {
                Text(
                    text = errorMessage ?: "",
                    color = textWhite,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Predictions Section: Rulet çarkı tarzı tahmin kutuları
        predictions?.let { prediction ->
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(animationSpec = tween(500)),
                exit = scaleOut(animationSpec = tween(500))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .shadow(8.dp, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = rouletteGreen.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, gold)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tahminler",
                            color = gold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PredictionBox("Kısa Vade", prediction.shortTerm, rouletteGreen, red, black)
                            PredictionBox("Orta Vade", prediction.midTerm, rouletteGreen, red, black)
                            PredictionBox("Uzun Vade", prediction.longTerm, rouletteGreen, red, black)
                        }
                    }
                }
            }
        }

        // Roulette Layout: Gerçek Avrupa Ruleti masası düzeni
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = rouletteGreen),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(3.dp, Brush.linearGradient(listOf(gold, velvetRed)))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 0 sayısı (üstte, geniş yeşil alan)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(rouletteGreen, shape = RoundedCornerShape(8.dp))
                        .border(2.dp, gold, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "0",
                        color = textWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 1-36 sayıları (Avrupa Ruleti masası düzeninde)
                val rouletteNumbers = listOf(
                    3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36, // İlk sütun
                    2, 5, 8, 11, 14, 17, 20, 23, 26, 29, 32, 35, // İkinci sütun
                    1, 4, 7, 10, 13, 16, 19, 22, 25, 28, 31, 34  // Üçüncü sütun
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (0..2).forEach { column ->
                        Column {
                            (0..11).forEach { row ->
                                val index = row + column * 12
                                val number = rouletteNumbers[index]
                                Button(
                                    onClick = { /* Handle number click */ },
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            number == 0 -> rouletteGreen
                                            listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(number) -> red
                                            else -> black
                                        }
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = number.toString(),
                                        color = textWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Overview: Kadife dokulu istatistik kartları
        AnimatedVisibility(
            visible = spinList.isNotEmpty(),
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(
                    text = "İstatistik Özeti",
                    color = gold,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .align(Alignment.CenterHorizontally)
                        .background(velvetRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard("Son 10 Tur", getLastTenData(spinList))
                    StatCard("Renk Dağılımı", getRedBlackDistribution(spinList))
                    StatCard("Bölge Analizi", getRangeDistribution(spinList))
                }
            }
        }
    }
}

@Composable
fun PredictionBox(
    label: String,
    prediction: Int,
    rouletteGreen: Color,
    red: Color,
    black: Color
) {
    val textWhite = Color.White
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(
                when {
                    prediction == 0 -> rouletteGreen
                    listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36).contains(prediction) -> red
                    else -> black
                }, shape = CircleShape
            )
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .shadow(4.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = prediction.toString(),
                color = textWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = textWhite,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatCard(title: String, data: List<Float>) {
    val bgDark = Color(0xFF1A1A1A)
    val textWhite = Color.White
    val gold = Color(0xFFFFD700)
    val goldGradient = Color(0x66FFD700)
    val velvetRed = Color(0xFF8B0000)

    Card(
        modifier = Modifier
            .weight(1f)
            .padding(8.dp)
            .shadow(8.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = bgDark.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(goldGradient, velvetRed)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = gold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            BarChartView(data, listOf(title.split(" ")[0]))
        }
    }
}

@Composable
fun BarChartView(data: List<Float>, labels: List<String>) {
    AndroidView(
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)

                val entries = data.mapIndexed { index, value ->
                    BarEntry(index.toFloat(), value)
                }
                val dataSet = BarDataSet(entries, "Dağılım").apply {
                    colors = listOf(gold, red, black)
                    valueTextColor = android.graphics.Color.WHITE
                    valueTextSize = 10f
                }
                val barData = BarData(dataSet)
                this.data = barData
                animateY(1500)
                invalidate()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 8.dp)
    )
}

fun getLastTenData(spinList: List<Int>): List<Float> {
    return spinList.takeLast(10).map { it.toFloat() }
}

fun getRedBlackDistribution(spinList: List<Int>): List<Float> {
    val redNumbers = listOf(1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)
    val redCount = spinList.count { it in redNumbers }
    val blackCount = spinList.count { it !in redNumbers && it in 0..36 }
    return listOf(redCount.toFloat(), blackCount.toFloat())
}

fun getRangeDistribution(spinList: List<Int>): List<Float> {
    val lowCount = spinList.count { it in 1..18 }
    val highCount = spinList.count { it in 19..36 }
    return listOf(lowCount.toFloat(), highCount.toFloat())
}
