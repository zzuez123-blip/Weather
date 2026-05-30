package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @Json(name = "results") val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "country") val country: String? = null,
    @Json(name = "admin1") val admin1: String? = null
)

@JsonClass(generateAdapter = true)
data class ForecastResponse(
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "current") val current: CurrentForecast? = null,
    @Json(name = "hourly") val hourly: HourlyForecastData? = null,
    @Json(name = "daily") val daily: DailyForecastData? = null
)

@JsonClass(generateAdapter = true)
data class CurrentForecast(
    @Json(name = "temperature_2m") val temperature_2m: Double,
    @Json(name = "weather_code") val weather_code: Int
)

@JsonClass(generateAdapter = true)
data class HourlyForecastData(
    @Json(name = "time") val time: List<String>,
    @Json(name = "temperature_2m") val temperature_2m: List<Double>,
    @Json(name = "weather_code") val weather_code: List<Int>,
    @Json(name = "precipitation_probability") val precipitation_probability: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class DailyForecastData(
    @Json(name = "time") val time: List<String>,
    @Json(name = "weather_code") val weather_code: List<Int>,
    @Json(name = "temperature_2m_max") val temperature_2m_max: List<Double>,
    @Json(name = "temperature_2m_min") val temperature_2m_min: List<Double>
)

interface OpenMeteoApi {
    @GET("https://geocoding-api.open-meteo.com/v1/search")
    suspend fun searchLocations(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "en",
        @Query("format") format: String = "json"
    ): GeocodingResponse

    @GET("https://api.open-meteo.com/v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "weather_code,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "auto"
    ): ForecastResponse
}

object WeatherNetworkClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: OpenMeteoApi = retrofit.create(OpenMeteoApi::class.java)
}

object WmoWeatherMapper {
    fun mapWmoToWeatherState(code: Int): WeatherState {
        return when (code) {
            0, 1, 2 -> WeatherState.SUNNY
            3, 45, 48 -> WeatherState.FOGGY
            51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99 -> WeatherState.RAINY
            56, 57, 66, 67, 71, 73, 75, 77, 85, 86 -> WeatherState.SNOWY
            else -> WeatherState.SUNNY
        }
    }

    fun mapWmoToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy conditions"
            51, 53, 55 -> "Drizzling"
            61, 63, 65 -> "Continuous Rain showers"
            71, 73, 75 -> "Snowfall drifting"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95, 96, 99 -> "Severe Thunderstorms"
            else -> "Ambiance condition"
        }
    }
}
