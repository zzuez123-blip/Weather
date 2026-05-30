package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.WeatherDataGenerator
import com.example.data.WeatherState
import com.example.ui.components.*

@Composable
fun WeatherDashboard(
    modifier: Modifier = Modifier
) {
    // 1. Unified Jetpack ViewModel & States
    val viewModel: WeatherViewModel = viewModel()

    var selectedTab by remember { mutableStateOf(DockTab.HOME) }
    
    val activeState by viewModel.activeWeatherState.collectAsState()
    val activeLocationName by viewModel.activeLocationName.collectAsState()
    val activeTemp by viewModel.activeTemp.collectAsState()
    val hourlyList by viewModel.hourlyForecasts.collectAsState()
    val dailyList by viewModel.dailyForecasts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.errorMsg.collectAsState()

    // Default customizable dock configuration state
    var dockConfig by remember { 
        mutableStateOf(DockConfig(
            opacity = 0.88f,
            roundnessDp = 28f,
            blurIntensityDp = 12f,
            isBlurEnabled = true
        )) 
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 2. Active Screen Content Renderer using animated fade layouts
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedTab) {
                DockTab.HOME -> {
                    HomeView(
                        locationName = activeLocationName,
                        state = activeState,
                        temp = activeTemp,
                        hourlyList = hourlyList,
                        dailyList = dailyList,
                        isLoading = isLoading,
                        errorMsg = errorMsg,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                DockTab.FORECAST -> {
                    ForecastContent(
                        hourlyList = hourlyList,
                        dailyList = dailyList,
                        activeState = activeState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp) // Cushion to avoid dock visual overlap
                    )
                }
                DockTab.LOCATIONS -> {
                    LocationsContent(
                        viewModel = viewModel,
                        onLocationSelected = {
                            selectedTab = DockTab.HOME // Transition back to home layout
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp)
                    )
                }
                DockTab.SETTINGS -> {
                    SettingsContent(
                        config = dockConfig,
                        onConfigChange = { dockConfig = it },
                        simulatedState = activeState,
                        onStateChange = { viewModel.loadFallbackSimulation(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 60.dp)
                    )
                }
            }
        }

        // 3. Persistent Customizable Floating Dock Bar at the bottom center
        FloatingDockBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            config = dockConfig,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .testTag("floating_persistent_dock")
        )
    }
}

@Composable
fun HomeView(
    locationName: String,
    state: WeatherState,
    temp: Int,
    hourlyList: List<com.example.data.HourlyForecast>,
    dailyList: List<com.example.data.DailyForecast>,
    isLoading: Boolean,
    errorMsg: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subtle Loader overlay indicator
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        }

        if (errorMsg != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    text = errorMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // A. Integrated Scenic Header with text content overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(32.dp))
                .testTag("dynamic_scenery_header")
        ) {
            // Dynamic scenery base
            WeatherScenery(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            )

            // Dynamic readable text color based on weather scenery ambiance to lock high contrast
            val overlayContentColor = when (state) {
                WeatherState.SUNNY, WeatherState.FOGGY -> Color(0xFF001F2A)
                else -> Color.White
            }

            // Information Overlay
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Location Header & Style Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.testTag("main_location_label")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Active Location",
                            tint = overlayContentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = locationName.substringBefore(","),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = overlayContentColor
                        )
                    }

                    // Style Glass badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(overlayContentColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when(state) {
                                WeatherState.SUNNY -> "Ghibli Art"
                                WeatherState.RAINY -> "Lo-Fi Neon"
                                WeatherState.SNOWY -> "Flat Vector"
                                WeatherState.FOGGY -> "Mystic forest"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = overlayContentColor
                        )
                    }
                }

                // Bottom Row: Large temp + condition identifier
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_temp_block"),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$temp°",
                            fontSize = 62.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp,
                            color = overlayContentColor,
                            lineHeight = 62.sp
                        )
                        Text(
                            text = state.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = overlayContentColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // B. Horizontal 24-hour Scrolling Forecast section (Middle)
        Horizontal24HourForecast(
            hourlyList = hourlyList
        )

        // C. Vertical 7-Day Forecast section (Bottom)
        Vertical7DayForecast(
            dailyList = dailyList
        )

        // Bottom space to let views scroll completely above the dock barrier
        Spacer(
            modifier = Modifier
                .navigationBarsPadding()
                .height(110.dp)
        )
    }
}
