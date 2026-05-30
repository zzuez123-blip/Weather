package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyForecast
import com.example.data.HourlyForecast
import com.example.data.WeatherState

@Composable
fun ForecastContent(
    hourlyList: List<HourlyForecast>,
    dailyList: List<DailyForecast>,
    activeState: WeatherState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core header details
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Atmospheric Details & Trends",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Detailed meteorological indicators corresponding to climate simulation.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Curated Weather Parameters
        MeteorologicalGrid(activeState, modifier = Modifier.padding(horizontal = 20.dp))

        // Time forecasts embedded here
        Horizontal24HourForecast(hourlyList = hourlyList)

        Vertical7DayForecast(dailyList = dailyList)
    }
}

@Composable
fun MeteorologicalGrid(
    state: WeatherState,
    modifier: Modifier = Modifier
) {
    // Dynamic values based on active simulated climate state
    val wind = when (state) {
        WeatherState.SUNNY -> "6 km/h"
        WeatherState.RAINY -> "24 km/h"
        WeatherState.SNOWY -> "18 km/h"
        WeatherState.FOGGY -> "3 km/h"
    }
    
    val humidity = when (state) {
        WeatherState.SUNNY -> "35%"
        WeatherState.RAINY -> "92%"
        WeatherState.SNOWY -> "75%"
        WeatherState.FOGGY -> "98%"
    }

    val uvIndex = when (state) {
        WeatherState.SUNNY -> "9 / Very High"
        WeatherState.RAINY -> "1 / Minimal"
        WeatherState.SNOWY -> "3 / Moderate"
        WeatherState.FOGGY -> "2 / Minimal"
    }

    val pressure = when (state) {
        WeatherState.SUNNY -> "1016 hPa"
        WeatherState.RAINY -> "998 hPa"
        WeatherState.SNOWY -> "1009 hPa"
        WeatherState.FOGGY -> "1012 hPa"
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Wind Speed",
                value = wind,
                icon = Icons.Filled.Air,
                desc = "Air circulation velocity",
                modifier = Modifier.weight(1f).testTag("metric_card_wind")
            )
            MetricCard(
                title = "Humidity",
                value = humidity,
                icon = Icons.Filled.Opacity,
                desc = "Moisture ratio in active air",
                modifier = Modifier.weight(1f).testTag("metric_card_humidity")
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "UV Radiation",
                value = uvIndex,
                icon = Icons.Filled.WbSunny,
                desc = "Index of solar ultraviolet rays",
                modifier = Modifier.weight(1f).testTag("metric_card_uv")
            )
            MetricCard(
                title = "Pressure",
                value = pressure,
                icon = Icons.Filled.Speed,
                desc = "Surface atmospheric weight",
                modifier = Modifier.weight(1f).testTag("metric_card_pressure")
            )
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    desc: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = desc,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    lineHeight = 11.sp
                )
            }
        }
    }
}
