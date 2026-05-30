package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.WeatherState
import kotlin.math.sin

@Composable
fun WeatherScenery(
    state: WeatherState,
    modifier: Modifier = Modifier
) {
    // Shared infinite transition for background and scenery element animations
    val infiniteTransition = rememberInfiniteTransition(label = "weatherScenery")
    
    // Animation inputs for various scenes
    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "generalAnimationProgress"
    )

    val windSwayState by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = SineBorderEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "windSway"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            when (state) {
                WeatherState.SUNNY -> {
                    drawSunnyGhibli(width, height, animationProgress, windSwayState)
                }
                WeatherState.RAINY -> {
                    drawRainyLoFi(width, height, animationProgress)
                }
                WeatherState.SNOWY -> {
                    drawSnowyVillage(width, height, animationProgress)
                }
                WeatherState.FOGGY -> {
                    drawFoggyEnchanted(width, height, animationProgress, windSwayState)
                }
            }
        }
    }
}

// Custom easing for reverse wind sway
private val SineBorderEasing = Easing { fraction ->
    sin(fraction * Math.PI).toFloat()
}

/**
 * Sunny: A vibrant, high-contrast green hilltop in a Studio Ghibli art style
 * with fluffy, oversized white clouds.
 */
private fun DrawScope.drawSunnyGhibli(
    width: Float,
    height: Float,
    animProgress: Float,
    windSway: Float
) {
    // 1. Sky background gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF3AB6FB), Color(0xFF9FE3FF))
        ),
        size = Size(width, height)
    )

    // 2. Rising glowing Sun
    val sunRadius = height * 0.22f
    val sunCenterX = width * 0.8f
    val sunCenterY = height * 0.28f
    drawCircle(
        color = Color(0xFFFFEFA6),
        radius = sunRadius + 15 * windSway,
        center = Offset(sunCenterX, sunCenterY),
        alpha = 0.4f
    )
    drawCircle(
        color = Color(0xFFFFD54F),
        radius = sunRadius - 10,
        center = Offset(sunCenterX, sunCenterY)
    )

    // 3. Studio Ghibli Hills - Layered path curves
    // Distant soft blue-green hills
    val distantHillPath = Path().apply {
        moveTo(0f, height * 0.7f)
        cubicTo(
            width * 0.25f, height * 0.55f,
            width * 0.6f, height * 0.75f,
            width, height * 0.62f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(
        path = distantHillPath,
        color = Color(0xFF287D43)
    )

    // Foreground lush green hill with peak
    val hillPath = Path().apply {
        moveTo(0f, height * 0.85f)
        cubicTo(
            width * 0.35f, height * 0.62f,
            width * 0.75f, height * 0.58f,
            width, height * 0.78f
        )
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    
    // Highlighted hilltop gradient for high contrast
    drawPath(
        path = hillPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF5CBF4C), Color(0xFF2E8520)),
            startY = height * 0.6f,
            endY = height
        )
    )

    // Add a cute little wind turbine or flower cluster on the hill for interest
    val turbineBaseX = width * 0.25f
    val turbineBaseY = height * 0.78f
    // Shaft
    drawLine(
        color = Color(0xDDFFFFFF),
        start = Offset(turbineBaseX, turbineBaseY),
        end = Offset(turbineBaseX, turbineBaseY - 45f),
        strokeWidth = 3.5f
    )
    // Blades revolving based on animation progress
    val bladeLength = 22f
    val angle = animProgress * 2 * Math.PI
    for (i in 0..2) {
        val curAngle = angle + (i * 2 * Math.PI / 3)
        val bladeEndX = turbineBaseX + (bladeLength * kotlin.math.cos(curAngle)).toFloat()
        val bladeEndY = (turbineBaseY - 45f) + (bladeLength * sin(curAngle)).toFloat()
        drawLine(
            color = Color.White,
            start = Offset(turbineBaseX, turbineBaseY - 45f),
            end = Offset(bladeEndX, bladeEndY),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    // 4. Fluffy, oversized white Ghibli clouds floating
    // Animated cloud positions
    val cloudOffset1 = (animProgress * (width + 200f)) - 100f
    val cloudOffset2 = (((animProgress + 0.5f) % 1f) * (width + 300f)) - 150f

    // Cloud Group 1
    drawGhibliCloud(cloudOffset1, height * 0.25f, 65f)
    // Cloud Group 2
    drawGhibliCloud(cloudOffset2, height * 0.15f, 48f)
}

private fun DrawScope.drawGhibliCloud(centerX: Float, centerY: Float, r: Float) {
    val cloudColor = Color(0xECFFFFFF)
    drawCircle(cloudColor, r, Offset(centerX, centerY))
    drawCircle(cloudColor, r * 0.8f, Offset(centerX - r * 0.7f, centerY + r * 0.1f))
    drawCircle(cloudColor, r * 1.1f, Offset(centerX + r * 0.6f, centerY + r * 0.2f))
    drawCircle(cloudColor, r * 0.6f, Offset(centerX + r * 1.4f, centerY + r * 0.4f))
    drawCircle(cloudColor, r * 0.5f, Offset(centerX - r * 1.3f, centerY + r * 0.3f))
}

/**
 * Rainy: A cozy, neon-lit city street in a Lo-Fi anime style
 * with reflective puddles and falling rain stroke vectors.
 */
private fun DrawScope.drawRainyLoFi(
    width: Float,
    height: Float,
    animProgress: Float
) {
    // 1. Cozy violet/crimson city skyline gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0C0721), Color(0xFF1E1035), Color(0xFF2C133F))
        ),
        size = Size(width, height)
    )

    // 2. City buildings - Neon silhouettes
    // Background building array
    val buildingWidths = listOf(width * 0.15f, width * 0.18f, width * 0.14f, width * 0.22f, width * 0.16f)
    var currentX = 0f
    val buildingHeights = listOf(height * 0.55f, height * 0.65f, height * 0.48f, height * 0.62f, height * 0.5f)
    
    buildingHeights.forEachIndexed { idx, bHeight ->
        val bWidth = buildingWidths[idx]
        val rectTop = height - bHeight - 15f
        
        // Solid dark block
        drawRect(
            color = Color(0xFF130A21),
            topLeft = Offset(currentX, rectTop),
            size = Size(bWidth, bHeight + 15f)
        )
        
        // Neon outlining
        val neonColor = if (idx % 2 == 0) Color(0xFFFF2A85) else Color(0xFF00FFCC)
        // Draw neon top outline
        drawLine(
            color = neonColor,
            start = Offset(currentX + 2, rectTop),
            end = Offset(currentX + bWidth - 2, rectTop),
            strokeWidth = 3f,
            alpha = 0.85f
        )
        
        // Animated cute neon signs inside the buildings or billboards
        if (idx == 1) {
            // Neon billboard sign (pink)
            val signLeft = currentX + bWidth * 0.2f
            val signTop = rectTop + 35f
            val signW = bWidth * 0.6f
            val signH = 40f
            drawRoundRect(
                color = Color(0xFFFF2A85),
                topLeft = Offset(signLeft, signTop),
                size = Size(signW, signH),
                cornerRadius = CornerRadius(6f, 6f),
                style = strokeStyle(3f),
                alpha = if (animProgress * 10 % 2 < 1.2) 0.9f else 0.3f // Flicker effect for anime authenticity
            )
            // glowing center indicator
            drawCircle(
                color = Color(0xFFFF2A85),
                radius = 4f,
                center = Offset(signLeft + signW / 2, signTop + signH / 2),
                alpha = 0.8f
            )
        } else if (idx == 3) {
            // Neon vertical indicator (cyan)
            val signLeft = currentX + bWidth * 0.4f
            val signTop = rectTop + 20f
            val signW = 15f
            val signH = 80f
            drawRoundRect(
                color = Color(0xFF00FFCC),
                topLeft = Offset(signLeft, signTop),
                size = Size(signW, signH),
                cornerRadius = CornerRadius(4f, 4f),
                style = strokeStyle(2.5f),
                alpha = if (animProgress * 5 % 2 < 1.6) 0.85f else 0.4f
            )
        }
        
        currentX += bWidth
    }

    // 3. Dark road street surface
    val streetHeight = height * 0.28f
    val streetTop = height - streetHeight
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E1B33), Color(0xFF0A0714))
        ),
        topLeft = Offset(0f, streetTop),
        size = Size(width, streetHeight)
    )

    // Sidewalk border line with a yellow neon light reflection
    drawLine(
        color = Color(0xFFD4AF37),
        start = Offset(0f, streetTop),
        end = Offset(width, streetTop),
        strokeWidth = 2f,
        alpha = 0.5f
    )

    // 4. Reflective Puddles
    // Cozy horizontal neon-reflection ovals that add immense richness
    val puddle1X = width * 0.22f
    val puddle1Y = streetTop + 25f
    val puddle2X = width * 0.72f
    val puddle2Y = streetTop + 55f

    drawOval(
        color = Color(0xFF2E1C4B),
        topLeft = Offset(puddle1X - 45f, puddle1Y - 10f),
        size = Size(90f, 20f)
    )
    drawOval(
        color = Color(0xFF00FFCC),
        topLeft = Offset(puddle1X - 35f, puddle1Y - 8f),
        size = Size(70f, 16f),
        style = strokeStyle(1.5f),
        alpha = 0.4f
    )

    drawOval(
        color = Color(0xFF381440),
        topLeft = Offset(puddle2X - 55f, puddle2Y - 12f),
        size = Size(110f, 24f)
    )
    drawOval(
        color = Color(0xFFFF2A85),
        topLeft = Offset(puddle2X - 40f, puddle2Y - 9f),
        size = Size(80f, 18f),
        style = strokeStyle(1.5f),
        alpha = 0.4f
    )

    // 5. Falling rain stroke vectors
    // Using simple math to distribute dozens of falling angled strokes
    val strokeColor = Color(0x8CFFFFFF)
    val rainCount = 35
    for (i in 0..rainCount) {
        // distribute rain across screen based on index and animated flow
        val xSeed = (i * 37f) % width
        val speedMultiplier = 1f + (i % 3) * 0.15f
        val animatedProgressY = (animProgress * speedMultiplier) % 1f
        val yPos = (animatedProgressY * height)
        
        // Diagonal rain stroke
        val len = 32f
        val slant = 8f
        drawLine(
            color = strokeColor,
            start = Offset(xSeed + (yPos * 0.15f).toFloat(), yPos),
            end = Offset(xSeed + (yPos * 0.15f).toFloat() - slant, yPos + len),
            strokeWidth = 1.8f
        )
    }
}

/**
 * Snowy: A charming, glowing winter village in a minimalist flat 2D vector style
 * with soft drifting snowflake particles.
 */
private fun DrawScope.drawSnowyVillage(
    width: Float,
    height: Float,
    animProgress: Float
) {
    // 1. Deep blue-violet evening winter sky
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF141C38), Color(0xFF27325F))
        ),
        size = Size(width, height)
    )

    // Moon / soft glowing orb
    drawCircle(
        color = Color(0xFFFFFFFF),
        radius = 20f,
        center = Offset(width * 0.15f, height * 0.25f),
        alpha = 0.9f
    )
    drawCircle(
        color = Color(0xFFE3EDF7),
        radius = 35f,
        center = Offset(width * 0.15f, height * 0.25f),
        alpha = 0.18f
    )

    // 2. Snow mountains in flat 2D vector style
    val distantMountain = Path().apply {
        moveTo(width * 0.3f, height * 0.9f)
        lineTo(width * 0.65f, height * 0.45f)
        lineTo(width * 0.85f, height * 0.75f)
        lineTo(width * 0.95f, height * 0.62f)
        lineTo(width, height * 0.8f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(
        path = distantMountain,
        color = Color(0xFF1B2245)
    )

    // White snow tips on mountain
    val snowCap = Path().apply {
        moveTo(width * 0.65f, height * 0.45f)
        lineTo(width * 0.60f, height * 0.52f)
        lineTo(width * 0.63f, height * 0.54f)
        lineTo(width * 0.65f, height * 0.52f)
        lineTo(width * 0.68f, height * 0.56f)
        lineTo(width * 0.71f, height * 0.53f)
        close()
    }
    drawPath(path = snowCap, color = Color(0xFFE6EFFE))

    // 3. Main Snow banks (ground)
    val snowGround2 = Path().apply {
        moveTo(0f, height * 0.75f)
        quadraticTo(width * 0.4f, height * 0.68f, width, height * 0.78f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path = snowGround2, color = Color(0xFFBFD4F2))

    val snowGround1 = Path().apply {
        moveTo(0f, height * 0.88f)
        quadraticTo(width * 0.55f, height * 0.82f, width, height * 0.88f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path = snowGround1, color = Color(0xFFEBF3FC))

    // 4. Glowing Winter Village Houses
    // House 1 (Medium Left)
    val house1X = width * 0.35f
    val house1Y = height * 0.72f
    val house1W = 55f
    val house1H = 40f
    
    // House walls (Charming red)
    drawRect(
        color = Color(0xFFAD4943),
        topLeft = Offset(house1X, house1Y),
        size = Size(house1W, house1H)
    )
    // Triangle Roof Covered with thick Snow
    val roof1 = Path().apply {
        moveTo(house1X - 6f, house1Y + 2f)
        lineTo(house1X + house1W / 2, house1Y - 18f)
        lineTo(house1X + house1W + 6f, house1Y + 2f)
        close()
    }
    drawPath(path = roof1, color = Color(0xFF422C2C))
    val snowyRoof1 = Path().apply {
        moveTo(house1X - 6f, house1Y + 2f)
        lineTo(house1X + house1W / 2, house1Y - 18f)
        lineTo(house1X + house1W + 6f, house1Y + 2f)
        lineTo(house1X + house1W + 4f, house1Y - 3f)
        lineTo(house1X + house1W / 2, house1Y - 13f)
        lineTo(house1X - 4f, house1Y - 3f)
        close()
    }
    drawPath(path = snowyRoof1, color = Color.White)

    // Warm GLOWING yellow window!
    drawRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(house1X + 18f, house1Y + 12f),
        size = Size(16f, 16f)
    )
    // Window frames
    drawLine(Color(0xFF5E2724), Offset(house1X + 26f, house1Y + 12f), Offset(house1X + 26f, house1Y + 28f), 1.5f)
    drawLine(Color(0xFF5E2724), Offset(house1X + 18f, house1Y + 20f), Offset(house1X + 34f, house1Y + 20f), 1.5f)

    // House 2 (Slightly further and right)
    val house2X = width * 0.7f
    val house2Y = height * 0.76f
    val house2W = 45f
    val house2H = 32f

    // House walls (Charming dark blue wood)
    drawRect(
        color = Color(0xFF3B567D),
        topLeft = Offset(house2X, house2Y),
        size = Size(house2W, house2H)
    )
    val roof2 = Path().apply {
        moveTo(house2X - 5f, house2Y + 2f)
        lineTo(house2X + house2W / 2, house2Y - 14f)
        lineTo(house2X + house2W + 5f, house2Y + 2f)
        close()
    }
    drawPath(path = roof2, color = Color(0xFF283A52))
    
    // Glowing cozy lights
    drawRect(
        color = Color(0xFFFFD54F),
        topLeft = Offset(house2X + 14f, house2Y + 10f),
        size = Size(14f, 14f)
    )

    // Cozy chimney with tiny smoke particle ovals
    val chimneyLeft = house1X + 8f
    val chimneyTop = house1Y - 14f
    drawRect(
        color = Color(0xFF422C2C),
        topLeft = Offset(chimneyLeft, chimneyTop),
        size = Size(10f, 15f)
    )
    // Tiny smoke signals drifting
    val smokeProg = (animProgress * 1.5f) % 1.0f
    drawCircle(
        color = Color(0x66FFFFFF),
        radius = 5f + smokeProg * 4f,
        center = Offset(chimneyLeft + 5f + smokeProg * 25f, chimneyTop - 10f - smokeProg * 35f),
    )

    // 5. Pine Trees with Snowy boughs
    drawSnowyTree(width * 0.22f, height * 0.85f, 40f)
    drawSnowyTree(width * 0.86f, height * 0.9f, 50f)

    // 6. Soft drifting Snowflake particles
    val flakeCount = 45
    val snowColor = Color(0xECFFFFFF)
    for (i in 0..flakeCount) {
        val xSeed = (i * 47f) % width
        val speed = 0.5f + (i % 4) * 0.12f
        val animatedProgressY = (animProgress * speed) % 1f
        val yPos = animatedProgressY * height
        
        // Swaying sideways dynamically
        val sway = sin(animProgress * 2 * Math.PI + i).toFloat() * 12f
        val radius = 2.5f + (i % 3) * 1f
        
        drawCircle(
            color = snowColor,
            radius = radius,
            center = Offset(xSeed + sway, yPos)
        )
    }
}

private fun DrawScope.drawSnowyTree(baseX: Float, baseY: Float, baseHeight: Float) {
    // Pine tree trunk
    drawRect(
        color = Color(0xFF4E3629),
        topLeft = Offset(baseX - 4f, baseY - 12f),
        size = Size(8f, 12f)
    )
    // Multi layered triangles
    val greenColor = Color(0xFF1E3F2E)
    val snowCapColor = Color(0xFFF0F5FE)

    for (layer in 0..2) {
        val scale = 1.0f - layer * 0.25f
        val currentW = baseHeight * 0.8f * scale
        val currentH = baseHeight * 0.5f * scale
        val layerBaseY = baseY - 10f - layer * (baseHeight * 0.35f)

        // Tree structure
        val branchPath = Path().apply {
            moveTo(baseX - currentW / 2, layerBaseY)
            lineTo(baseX, layerBaseY - currentH)
            lineTo(baseX + currentW / 2, layerBaseY)
            close()
        }
        drawPath(path = branchPath, color = greenColor)

        // Layer top snow caps
        val layerCap = Path().apply {
            moveTo(baseX - currentW / 4, layerBaseY - currentH / 2)
            lineTo(baseX, layerBaseY - currentH)
            lineTo(baseX + currentW / 4, layerBaseY - currentH / 2)
            close()
        }
        drawPath(path = layerCap, color = snowCapColor)
    }
}

/**
 * Foggy: A mystical, pastel-toned enchanted forest with soft-edged layered
 * silhouettes and a low-opacity mist overlay.
 */
private fun DrawScope.drawFoggyEnchanted(
    width: Float,
    height: Float,
    animProgress: Float,
    windSway: Float
) {
    // 1. Mystical, pastel indigo-pink background
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE2E4EB), Color(0xFFF3DDD6))
        ),
        size = Size(width, height)
    )

    // Glowing dim peach sun showing through fog
    drawCircle(
        color = Color(0xFFFFF0E6),
        radius = height * 0.16f,
        center = Offset(width * 0.65f, height * 0.44f),
        alpha = 0.55f
    )

    // 2. Layered Forest Silhouettes
    // Deepest soft pastel layer (far background - high mist blending)
    drawEnchantedLayer(width, height, treeCount = 7, scale = 1.4f, alpha = 0.25f, tint = Color(0xFF9E8DB3), yOffset = 18f, windSway = windSway)
    
    // Midground magical violet-grey layer
    drawEnchantedLayer(width, height, treeCount = 9, scale = 1.0f, alpha = 0.55f, tint = Color(0xFF7E6F95), yOffset = 45f, windSway = windSway * 0.5f)

    // Soft rising ground behind foreground layer
    val mistRisePath = Path().apply {
        moveTo(0f, height * 0.8f)
        quadraticTo(width * 0.5f, height * 0.72f, width, height * 0.85f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(path = mistRisePath, color = Color(0xFFDACCC6))

    // Foreground dark mystical indigo layer
    drawEnchantedLayer(width, height, treeCount = 6, scale = 0.8f, alpha = 0.9f, tint = Color(0xFF4C3E61), yOffset = 70f, windSway = windSway * 0.2f)

    // 3. Mystical low-opacity creeping mist overlay
    // Multiple horizontal layered waves shifting slowly
    val mistColor1 = Color(0x33FFFFFF)
    val mistColor2 = Color(0x1AFFFFFF)

    val wave1Offset = animProgress * width
    val wave2Offset = ((animProgress + 0.5f) % 1f) * width

    // Draw wavy mist bands to create beautiful atmospheric parallax depth
    drawMistOverlayWave(width, height * 0.75f, wave1Offset, mistColor1, sizeVal = 60f)
    drawMistOverlayWave(width, height * 0.82f, -wave2Offset, mistColor2, sizeVal = 75f)
    drawMistOverlayWave(width, height * 0.55f, wave2Offset * 0.7f, Color(0x18F4DDD6), sizeVal = 50f)
}

private fun DrawScope.drawEnchantedLayer(
    width: Float,
    height: Float,
    treeCount: Int,
    scale: Float,
    alpha: Float,
    tint: Color,
    yOffset: Float,
    windSway: Float
) {
    val step = width / (treeCount - 1)
    for (i in 0 until treeCount) {
        val x = i * step + (sin(i.toDouble()) * 20f).toFloat()
        val treeH = (110f + (i * 13) % 45) * scale
        val treeW = (45f + (i * 7) % 25) * scale
        val baseY = height - 15f - yOffset
        
        // Render a cute pine/spruce tree silhouette with simple branches
        drawMysticTree(x, baseY, treeW, treeH, tint, alpha, windSway)
    }
}

private fun DrawScope.drawMysticTree(
    x: Float,
    baseY: Float,
    width: Float,
    treeHeight: Float,
    tint: Color,
    alpha: Float,
    sway: Float
) {
    val trunkW = width * 0.16f
    val trunkH = treeHeight * 0.2f
    
    // Trunk
    drawRect(
        color = tint,
        topLeft = Offset(x - trunkW / 2, baseY - trunkH),
        size = Size(trunkW, trunkH),
        alpha = alpha
    )

    // Animated sway angle
    val swayOffset = sway * 8f * (treeHeight / 150f)

    // 3 interlocking polygonal clusters
    for (c in 0..2) {
        val yFactor = c * 0.22f
        val layerW = width * (1.0f - c * 0.25f)
        val layerH = treeHeight * 0.35f
        val topY = baseY - trunkH - (treeHeight * yFactor)

        val branchPath = Path().apply {
            moveTo(x - layerW / 2, topY)
            lineTo(x + swayOffset, topY - layerH)
            lineTo(x + layerW / 2, topY)
            close()
        }
        drawPath(path = branchPath, color = tint, alpha = alpha)
    }
}

private fun DrawScope.drawMistOverlayWave(
    width: Float,
    centerY: Float,
    offset: Float,
    color: Color,
    sizeVal: Float
) {
    // Draws layered transparent soft-edged ovals that feel like misty fog creeping across
    for (i in -1..2) {
        val cx = i * (width / 2) + (offset % (width / 2))
        drawOval(
            color = color,
            topLeft = Offset(cx - width * 0.4f, centerY - sizeVal / 2),
            size = Size(width * 0.8f, sizeVal)
        )
    }
}

private fun strokeStyle(width: Float) = Stroke(
    width = width,
    cap = StrokeCap.Round,
    join = StrokeJoin.Round
)
