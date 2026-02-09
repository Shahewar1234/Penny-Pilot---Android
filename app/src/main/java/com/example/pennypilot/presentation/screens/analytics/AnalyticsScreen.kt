package com.example.pennypilot.presentation.screens.analytics

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pennypilot.presentation.viewmodel.AnalyticsUiState
import com.example.pennypilot.presentation.viewmodel.AnalyticsViewModel
import java.text.NumberFormat

// --- Theme Colors for Analytics Screen ---
private val AnalyticsBackground = Color(0xFF01140A)
private val AnalyticsPrimaryText = Color.White
private val AnalyticsSecondaryText = Color(0xFFB0B0B0)
private val AnalyticsCardBg = Color.White.copy(alpha = 0.05f)
private val AnalyticsErrorColor = Color(0xFFCF6679)
private val AnalyticsAccentGreen = Color(0xFF61BC79)

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedItemIndex by remember { mutableIntStateOf(2) } // Set to 2 for Analytics
    val navItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Add", Icons.Default.AddCircle, "add_expense"),
        BottomNavItem("Analytics", Icons.Default.BarChart, "analytics"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Analytics", color = AnalyticsPrimaryText) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AnalyticsPrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AnalyticsBackground.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            if (selectedItemIndex != index) {
                                selectedItemIndex = index
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AnalyticsAccentGreen,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = AnalyticsAccentGreen,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color(0xFF004D2B).copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = AnalyticsBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AnalyticsAccentGreen
                )
            } else if (uiState.errorMessage != null) {
                ErrorView(message = uiState.errorMessage!!, onRetry = { viewModel.refreshData() })
            } else {
                AnalyticsContent(uiState = uiState)
            }
        }
    }
}

@Composable
private fun AnalyticsContent(uiState: AnalyticsUiState) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InsightCard(
                    title = "Total Spent",
                    value = formatCurrency(uiState.totalSpent),
                    modifier = Modifier.weight(1f)
                )
                InsightCard(
                    title = "Daily Avg",
                    value = formatCurrency(uiState.dailyAverage),
                    modifier = Modifier.weight(1f)
                )
                InsightCard(
                    title = "Top Category",
                    value = uiState.topCategory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            SpendingByCategoryCard(spendingData = uiState.spendingByCategory)
        }
    }
}

@Composable
fun InsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsCardBg)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, fontSize = 13.sp, color = AnalyticsSecondaryText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsPrimaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SpendingByCategoryCard(spendingData: Map<String, Float>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AnalyticsCardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Spending by Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AnalyticsPrimaryText
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (spendingData.isEmpty()) {
                Text("No category data to display.", color = AnalyticsSecondaryText, modifier = Modifier.padding(vertical = 40.dp))
            } else {
                // Pie chart and legend
                Row(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpendingPieChart(
                        data = spendingData,
                        modifier = Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    CategoryLegend(
                        data = spendingData,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp)) // Increased space

                // Bar chart
                BarChart(spendingData = spendingData, modifier = Modifier.fillMaxWidth().height(200.dp))
            }
        }
    }
}

@Composable
private fun SpendingPieChart(data: Map<String, Float>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    if (total == 0f) return

    val sweepAngles = data.values.map { 360 * it / total }
    val colors = remember(data.keys) { generateChartColors(data.size) }

    Canvas(modifier = modifier) {
        var startAngle = -90f
        sweepAngles.forEachIndexed { index, sweepAngle ->
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle - 2f, // Small gap between arcs
                useCenter = false,
                style = Stroke(width = 50f)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun CategoryLegend(data: Map<String, Float>, modifier: Modifier = Modifier) {
    val colors = remember(data.keys) { generateChartColors(data.size) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        data.keys.forEachIndexed { index, categoryName ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(colors[index], CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = categoryName,
                    color = AnalyticsSecondaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@Composable
fun BarChart(spendingData: Map<String, Float>, modifier: Modifier = Modifier) {
    val maxAmount = spendingData.values.maxOrNull() ?: 0f
    val density = LocalDensity.current

    val textPaint = remember {
        Paint().apply {
            color = AnalyticsSecondaryText.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = with(density) { 12.sp.toPx() }
        }
    }
    val axisLineColor = Color.Gray.copy(alpha = 0.5f)

    val barSectionWidth = 64.dp
    val totalWidth = barSectionWidth * spendingData.size
    val scrollState = rememberScrollState()

    Box(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(modifier = Modifier.width(totalWidth).fillMaxHeight()) {
            val yAxisPadding = 16.dp.toPx()
            val xAxisPadding = 32.dp.toPx()
            val drawableAreaHeight = size.height - yAxisPadding - xAxisPadding

            // Draw the horizontal axis line
            drawLine(
                color = axisLineColor,
                start = Offset(0f, size.height - xAxisPadding),
                end = Offset(size.width, size.height - xAxisPadding),
                strokeWidth = 1.dp.toPx()
            )

            spendingData.entries.forEachIndexed { index, entry ->
                val barWidth = barSectionWidth.toPx() / 2f
                val barSectionX = index * barSectionWidth.toPx()
                val xOffset = barSectionX + (barSectionWidth.toPx() - barWidth) / 2f

                val barHeight = if (maxAmount > 0f) (entry.value / maxAmount) * drawableAreaHeight else 0f

                drawRoundRect(
                    color = AnalyticsAccentGreen,
                    topLeft = Offset(xOffset, size.height - xAxisPadding - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                drawContext.canvas.nativeCanvas.drawText(
                    entry.key,
                    barSectionX + barSectionWidth.toPx() / 2f,
                    size.height - (xAxisPadding / 2f) + (textPaint.textSize / 3f), // Center text vertically in padding area
                    textPaint
                )
            }
        }
    }
}


@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = AnalyticsErrorColor,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Could not load analytics data.\nReason: $message",
            color = AnalyticsErrorColor,
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            lineHeight = 22.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = AnalyticsAccentGreen, contentColor = Color.Black)
        ) {
            Text("Try Again", fontWeight = FontWeight.Bold)
        }
    }
}

private fun generateChartColors(count: Int): List<Color> {
    return List(count) { i ->
        val hue = (360f / count * i) % 360f
        Color.hsv(hue, 0.7f, 0.9f)
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance().format(amount)
}
