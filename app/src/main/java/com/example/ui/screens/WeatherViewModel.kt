package com.example.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val dao = database.savedLocationDao()

    // 1. Saved locations flow from Room
    val savedLocations: StateFlow<List<SavedLocationEntity>> = dao.getAllLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Active Screen/Selection state
    private val _activeLocation = MutableStateFlow<SavedLocationEntity?>(null)
    val activeLocation: StateFlow<SavedLocationEntity?> = _activeLocation.asStateFlow()

    private val _activeLocationName = MutableStateFlow("Kyoto, Japan")
    val activeLocationName: StateFlow<String> = _activeLocationName.asStateFlow()

    private val _activeWeatherState = MutableStateFlow(WeatherState.SUNNY)
    val activeWeatherState: StateFlow<WeatherState> = _activeWeatherState.asStateFlow()

    private val _activeTemp = MutableStateFlow(28)
    val activeTemp: StateFlow<Int> = _activeTemp.asStateFlow()

    private val _hourlyForecasts = MutableStateFlow<List<HourlyForecast>>(emptyList())
    val hourlyForecasts: StateFlow<List<HourlyForecast>> = _hourlyForecasts.asStateFlow()

    private val _dailyForecasts = MutableStateFlow<List<DailyForecast>>(emptyList())
    val dailyForecasts: StateFlow<List<DailyForecast>> = _dailyForecasts.asStateFlow()

    // 3. Search and lookup states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults.asStateFlow()

    // Loading & Error States
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMsg = MutableStateFlow<String?>(null)
    val errorMsg: StateFlow<String?> = _errorMsg.asStateFlow()

    private var searchJob: Job? = null
    private var weatherFetchJob: Job? = null

    init {
        // Seed default locations if empty
        viewModelScope.launch {
            if (dao.getCount() == 0) {
                // Kyoto, Japan
                dao.insertLocation(
                    SavedLocationEntity(
                        name = "Kyoto",
                        country = "Japan",
                        latitude = 35.02107,
                        longitude = 135.75385,
                        temp = 24,
                        weatherStateId = "sunny",
                        isCustom = false
                    )
                )
                // Seattle, USA
                dao.insertLocation(
                    SavedLocationEntity(
                        name = "Seattle",
                        country = "USA",
                        latitude = 47.6062,
                        longitude = -122.3321,
                        temp = 19,
                        weatherStateId = "rainy",
                        isCustom = false
                    )
                )
                // Chamonix, France
                dao.insertLocation(
                    SavedLocationEntity(
                        name = "Chamonix",
                        country = "France",
                        latitude = 45.9227,
                        longitude = 6.8685,
                        temp = -2,
                        weatherStateId = "snowy",
                        isCustom = false
                    )
                )
                // Mystic Forest, Enchanted
                dao.insertLocation(
                    SavedLocationEntity(
                        name = "Mystic Forest",
                        country = "Enchanted",
                        latitude = 0.0,
                        longitude = 0.0,
                        temp = 11,
                        weatherStateId = "foggy",
                        isCustom = false
                    )
                )
            }
            // Load first saved location as active
            savedLocations.collect { list ->
                if (list.isNotEmpty() && _activeLocation.value == null) {
                    val defaultLoc = list.first()
                    _activeLocation.value = defaultLoc
                    _activeLocationName.value = "${defaultLoc.name}, ${defaultLoc.country}"
                    fetchWeatherForLocation(defaultLoc)
                }
            }
        }
    }

    fun selectLocation(location: SavedLocationEntity) {
        _activeLocation.value = location
        _activeLocationName.value = "${location.name}, ${location.country}"
        fetchWeatherForLocation(location)
    }

    fun selectGeocodingResult(result: GeocodingResult) {
        val countryText = result.country ?: ""
        val tempLoc = SavedLocationEntity(
            id = 0,
            name = result.name,
            country = countryText,
            latitude = result.latitude,
            longitude = result.longitude,
            temp = 20,
            weatherStateId = "sunny",
            isCustom = true
        )
        _activeLocation.value = tempLoc
        _activeLocationName.value = "${result.name}, $countryText"
        fetchWeatherForLocation(tempLoc)
    }

    fun pinLocation(location: SavedLocationEntity) {
        viewModelScope.launch {
            dao.insertLocation(location.copy(id = 0, isCustom = true))
        }
    }

    fun unpinLocation(location: SavedLocationEntity) {
        viewModelScope.launch {
            if (location.id != 0) {
                dao.deleteLocation(location)
            } else {
                // If it's the active unsaved one, nothing in DB to delete
            }
        }
    }

    fun searchLocations(query: String) {
        _searchQuery.value = query
        if (query.trim().length < 2) {
            _searchResults.value = emptyList()
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isSearching.value = true
            try {
                val response = WeatherNetworkClient.api.searchLocations(query)
                _searchResults.value = response.results ?: emptyList()
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun fetchWeatherForLocation(location: SavedLocationEntity) {
        weatherFetchJob?.cancel()
        weatherFetchJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMsg.value = null
            
            // Special handling for simulated Enchanted Mystic Forest
            if (location.name == "Mystic Forest" && location.latitude == 0.0) {
                // Load local simulations smoothly
                loadFallbackSimulation(WeatherState.FOGGY)
                _isLoading.value = false
                return@launch
            }

            try {
                val response = WeatherNetworkClient.api.getForecast(location.latitude, location.longitude)
                
                // 1. Current Current Temp and Condition
                val curTemp = response.current?.temperature_2m?.toInt() ?: 20
                val wmoCode = response.current?.weather_code ?: 0
                val mappedState = WmoWeatherMapper.mapWmoToWeatherState(wmoCode)
                
                _activeTemp.value = curTemp
                _activeWeatherState.value = mappedState

                // 2. Format Hourly list (Take first 24 entries)
                val hourlyData = response.hourly
                if (hourlyData != null) {
                    val list = mutableListOf<HourlyForecast>()
                    val size = minOf(hourlyData.time.size, hourlyData.temperature_2m.size, hourlyData.weather_code.size, 24)
                    for (i in 0 until size) {
                        val originalTime = hourlyData.time[i]
                        val hourOnly = formatHour(originalTime)
                        val hTemp = hourlyData.temperature_2m[i].toInt()
                        val hState = WmoWeatherMapper.mapWmoToWeatherState(hourlyData.weather_code[i])
                        val hProb = hourlyData.precipitation_probability?.getOrNull(i) ?: 0
                        list.add(HourlyForecast(hourOnly, hTemp, hState, hProb))
                    }
                    _hourlyForecasts.value = list
                }

                // 3. Format Daily list (Take first 7 entries)
                val dailyData = response.daily
                if (dailyData != null) {
                    val list = mutableListOf<DailyForecast>()
                    val size = minOf(dailyData.time.size, dailyData.weather_code.size, dailyData.temperature_2m_max.size, dailyData.temperature_2m_min.size, 7)
                    val daysOfWeek = listOf("Today", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    for (i in 0 until size) {
                        val dateStr = dailyData.time[i]
                        val (dayName, datePretty) = formatDayAndDate(dateStr)
                        
                        // Set "Today" label for first index
                        val dayLabel = if (i == 0) "Today" else dayName
                        
                        val high = dailyData.temperature_2m_max[i].toInt()
                        val low = dailyData.temperature_2m_min[i].toInt()
                        val dState = WmoWeatherMapper.mapWmoToWeatherState(dailyData.weather_code[i])
                        val desc = WmoWeatherMapper.mapWmoToDescription(dailyData.weather_code[i])
                        
                        list.add(DailyForecast(dayLabel, datePretty, high, low, dState, desc))
                    }
                    _dailyForecasts.value = list
                }

                // Smoothly update location's cached temp and state in the database if it is pinned already
                if (location.id != 0) {
                    dao.insertLocation(
                        location.copy(
                            temp = curTemp,
                            weatherStateId = mappedState.id
                        )
                    )
                }

            } catch (e: Exception) {
                // Network error fallback
                _errorMsg.value = "Failed to fetch live open-source weather: ${e.message}. Using simulated data."
                
                // Fallback state mapping
                val fallbackState = try {
                    WeatherState.valueOf(location.weatherStateId.uppercase(Locale.US))
                } catch (ex: Exception) {
                    WeatherState.SUNNY
                }
                loadFallbackSimulation(fallbackState)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadFallbackSimulation(state: WeatherState) {
        _activeWeatherState.value = state
        _activeTemp.value = state.defaultTemp
        _hourlyForecasts.value = WeatherDataGenerator.getHourlyForecasts(state)
        _dailyForecasts.value = WeatherDataGenerator.get7DayForecast(state)
    }

    private fun formatHour(timeStr: String): String {
        return try {
            val parts = timeStr.split("T")
            if (parts.size == 2) {
                parts[1]
            } else {
                timeStr
            }
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun formatDayAndDate(dateStr: String): Pair<String, String> {
        return try {
            val date = LocalDate.parse(dateStr)
            val dayName = date.format(DateTimeFormatter.ofPattern("EEE", Locale.US))
            val datePretty = date.format(DateTimeFormatter.ofPattern("MMM dd", Locale.US))
            Pair(dayName, datePretty)
        } catch (e: Exception) {
            Pair("Day", dateStr)
        }
    }
}
