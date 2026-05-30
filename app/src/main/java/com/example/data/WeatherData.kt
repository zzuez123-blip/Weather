package com.example.data

enum class WeatherState(
    val id: String,
    val displayName: String,
    val defaultLocation: String,
    val defaultTemp: Int,
    val description: String
) {
    SUNNY("sunny", "Sunny", "Kyoto, Japan", 28, "Sunny & Clear"),
    RAINY("rainy", "Rainy", "Seattle, USA", 19, "Heavy Rain showers"),
    SNOWY("snowy", "Snowy", "Chamonix, France", -2, "Drifting Snowfall"),
    FOGGY("foggy", "Foggy", "Mystic Forest", 11, "Thick Eerie Mist")
}

data class HourlyForecast(
    val time: String,
    val temp: Int,
    val weatherState: WeatherState,
    val probability: Int
)

data class DailyForecast(
    val day: String,
    val date: String,
    val highTemp: Int,
    val lowTemp: Int,
    val weatherState: WeatherState,
    val description: String
)

object WeatherDataGenerator {
    
    fun getHourlyForecasts(state: WeatherState): List<HourlyForecast> {
        val baseTemp = state.defaultTemp
        val times = listOf(
            "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", 
            "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
            "20:00", "21:00", "22:00", "23:00", "00:00", "01:00",
            "02:00", "03:00", "04:00", "05:00", "06:00", "07:00"
        )
        
        return times.mapIndexed { index, time ->
            // Modulate temperature based on daytime curves (warmest around 14:00, coolest at 04:00)
            val factor = when (index) {
                in 4..10 -> (index - 4) * 0.8f // morning rise
                in 11..15 -> 5.0f - (index - 11) * 0.5f // afternoon peak to slow drop
                else -> -1.0f - (index % 5) * 0.4f // night coolness
            }
            val temp = baseTemp + factor.toInt()
            
            // Occasionally vary states slightly for hourly realism, but keep overall vibe consistent
            val hourlyState = if (index % 6 == 0 && state == WeatherState.RAINY) {
                WeatherState.FOGGY
            } else if (index % 5 == 0 && state == WeatherState.SUNNY) {
                // minor clouds/fog
                state
            } else {
                state
            }
            
            val probability = when (state) {
                WeatherState.SUNNY -> 0
                WeatherState.RAINY -> if (index in 10..18) 95 else 70
                WeatherState.SNOWY -> if (index in 6..14) 80 else 40
                WeatherState.FOGGY -> 85
            }
            
            HourlyForecast(time, temp, hourlyState, probability)
        }
    }
    
    fun get7DayForecast(state: WeatherState): List<DailyForecast> {
        val baseTemp = state.defaultTemp
        val days = listOf(
            DayPair("Today", "May 30"),
            DayPair("Sun", "May 31"),
            DayPair("Mon", "Jun 01"),
            DayPair("Tue", "Jun 02"),
            DayPair("Wed", "Jun 03"),
            DayPair("Thu", "Jun 04"),
            DayPair("Fri", "Jun 05")
        )
        
        return days.mapIndexed { index, pair ->
            // Slightly offset temperature across week
            val tempOffset = (index - 3) * (if (index % 2 == 0) 1 else -1)
            val high = baseTemp + 4 + tempOffset
            val low = baseTemp - 5 + tempOffset
            
            // Dynamic daily description variations
            val dailyDescription = when (state) {
                WeatherState.SUNNY -> if (index % 3 == 0) "Partly Cloudy" else "Unblemished Sun"
                WeatherState.RAINY -> if (index % 2 == 0) "Heavy Downpour" else "Light Drizzle"
                WeatherState.SNOWY -> if (index % 3 == 1) "Blizzard Spells" else "Gentle Snow flurries"
                WeatherState.FOGGY -> if (index % 2 == 0) "Dense Fog overlay" else "Mild Morning mist"
            }
            
            // Vary states over the days, but stay matching the overall mood or transition
            val dailyState = if (index == 3 && state == WeatherState.RAINY) {
                WeatherState.FOGGY
            } else if (index == 4 && state == WeatherState.SUNNY) {
                WeatherState.SUNNY
            } else {
                state
            }
            
            DailyForecast(pair.dayName, pair.date, high, low, dailyState, dailyDescription)
        }
    }
}

private data class DayPair(val dayName: String, val date: String)
