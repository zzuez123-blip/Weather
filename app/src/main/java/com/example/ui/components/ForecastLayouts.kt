package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DailyForecast
import com.example.data.HourlyForecast
import com.example.data.WeatherState

@Composable
fun Horizontal24HourForecast(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hourly Forecast (24h)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Next 24 Hours",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hourly_forecast_row"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(hourlyList) { index, hourly ->
                HourlyForecastCard(
                    hourly = hourly,
                    isActive = index == 0
                )
            }
        }
    }
}

@Composable
fun HourlyForecastCard(
    hourly: HourlyForecast,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isActive) {
        Color(0xFFD6E3FF)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val cardBorder = if (isActive) {
        BorderStroke(1.dp, Color(0xFFADC6FF))
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
    
    val textColor = if (isActive) {
        Color(0xFF001B3D)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    val timeColor = if (isActive) {
        Color(0xFF001B3D)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .width(82.dp)
            .testTag("hourly_card_${hourly.time.replace(":", "_")}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isActive) "Now" else hourly.time,
                style = MaterialTheme.typography.bodySmall,
                color = timeColor,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Icon(
                imageVector = getWeatherIcon(hourly.weatherState),
                contentDescription = hourly.weatherState.displayName,
                tint = getWeatherColor(hourly.weatherState),
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${hourly.temp}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            if (hourly.probability > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${hourly.probability}%",
                    fontSize = 9.sp,
                    color = if (isActive) Color(0xFF001B3D) else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "-",
                    fontSize = 9.sp,
                    color = Color.Transparent
                )
            }
        }
    }
}

@Composable
fun Vertical7DayForecast(
    dailyList: List<DailyForecast>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "7-Day Forecast",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(vertical = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_forecast_card"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            dailyList.forEach { daily ->
                DailyForecastCard(daily)
            }
        }
    }
}

@Composable
fun DailyForecastCard(
    daily: DailyForecast,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_row_${daily.day.lowercase()}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day name & date combined block
            Column(modifier = Modifier.width(90.dp)) {
                Text(
                    text = daily.day,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = daily.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            // Midsection: Icon + short condition tip
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = getWeatherIcon(daily.weatherState),
                    contentDescription = daily.weatherState.displayName,
                    tint = getWeatherColor(daily.weatherState),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 6.dp)
                )
                Text(
                    text = daily.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Start
                )
            }

            // Temp chips span (High / Low)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${daily.highTemp}°",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.End
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${daily.lowTemp}°",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

fun getWeatherIcon(state: WeatherState): ImageVector {
    return when (state) {
        WeatherState.SUNNY -> Icons.Filled.WbSunny
        WeatherState.RAINY -> Icons.Filled.Thunderstorm
        WeatherState.SNOWY -> Icons.Filled.AcUnit
        WeatherState.FOGGY -> Icons.Filled.Air
    }
}

fun getWeatherColor(state: WeatherState): Color {
    return when (state) {
        WeatherState.SUNNY -> Color(0xFFFFB300) // Ambitious golden
        WeatherState.RAINY -> Color(0xFF42A5F5) // Electric sky blue
        WeatherState.SNOWY -> Color(0xFF90CAF9) // Cold glacial ice
        WeatherState.FOGGY -> Color(0xFFB0BEC5) // Pastel grey-violet mist
    }
}
