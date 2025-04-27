package com.ttt.cinevibe.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ttt.cinevibe.presentation.auth.AuthViewModel
import com.ttt.cinevibe.ui.theme.NetflixRed
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // Track if animations have been started and completed to prevent multiple executions
    var animationsStarted by remember { mutableStateOf(false) }
    var animationsCompleted by remember { mutableStateOf(false) }
    var navigationTriggered by remember { mutableStateOf(false) }
    
    // Add a screen alpha to control the entire screen's visibility
    val screenAlpha = remember { Animatable(1f) } // Bắt đầu với độ mờ đầy đủ để tránh màn hình đen
    
    // Animation state values
    val mainScale = remember { Animatable(0.9f) } // Tăng giá trị ban đầu để logo xuất hiện nhanh hơn
    val mainAlpha = remember { Animatable(0.2f) } // Bắt đầu với độ mờ nhẹ để logo xuất hiện ngay lập tức
    val rotationZ = remember { Animatable(0f) }
    val logoSpread = remember { Animatable(0f) }
    
    // For morphing effect
    val morphProgress = remember { Animatable(0f) }
    
    // Thêm hiệu ứng glow cho logo
    val glowIntensity = remember { Animatable(0f) }
    
    // Size of container for drawing elements
    var size by remember { mutableStateOf(IntSize(0, 0)) }
    val density = LocalDensity.current
    
    // Particle animation states
    val particleAlpha = remember { Animatable(0f) }
    val particleScale = remember { Animatable(0f) }
    val particleSpread = remember { Animatable(0f) }
    
    // Background animation
    val backgroundWave = remember { Animatable(0f) }
    
    // Character animation states for staggered text effect
    val charCount = "CINEVIBE".length
    val charAnimations = remember {
        List(charCount) { Animatable(0f) }
    }
    
    // Thêm hiệu ứng 3D cho từng ký tự
    val charRotations = remember {
        List(charCount) { Animatable(0f) }
    }
    
    // Get lifecycle to control animations based on screen visibility
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    // Observe lifecycle to prevent animation restart when returning to screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // Cancel any ongoing animations when leaving the screen
                    coroutineScope.cancel()
                }
                else -> { /* No action needed for other events */ }
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Launch animation sequence only once
    LaunchedEffect(Unit) {
        if (animationsStarted || navigationTriggered) return@LaunchedEffect
        animationsStarted = true
        
        // Không cần delay ban đầu nữa, chúng ta bắt đầu hiệu ứng ngay lập tức
        
        // Initial background wave animation
        launch {
            backgroundWave.animateTo(
                targetValue = 1f,
                animationSpec = tween(1500, easing = FastOutSlowInEasing)
            )
        }
        
        // Logo appears with fade-in
        launch {
            mainAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(600, easing = LinearEasing)
            )
        }
        
        // Logo scaling animation - sử dụng spring để tạo hiệu ứng nảy
        launch {
            mainScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        
        // Hiệu ứng glow cho logo
        launch {
            glowIntensity.animateTo(
                targetValue = 1f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
            
            glowIntensity.animateTo(
                targetValue = 0.5f,
                animationSpec = tween(500)
            )
            
            glowIntensity.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(400)
            )
        }
        
        // Subtle rotation effect - làm cho hiệu ứng xoay tinh tế hơn
        launch {
            rotationZ.animateTo(
                targetValue = 15f, // Giảm xuống chỉ xoay một chút
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
            rotationZ.animateTo(
                targetValue = -10f, // Xoay ngược lại
                animationSpec = tween(600, easing = FastOutSlowInEasing)
            )
            rotationZ.animateTo(
                targetValue = 0f, // Trở về vị trí ban đầu
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        }
        
        // Start morphing animation
        morphProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = LinearEasing)
        )
        
        // Animate each character in sequence - tạo độ trễ ngắn hơn giữa các ký tự
        charAnimations.forEachIndexed { index, anim ->
            launch {
                delay(70L * index) // Giảm độ trễ xuống để các chữ xuất hiện nhanh hơn
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
        
        // Thêm hiệu ứng xoay 3D cho từng ký tự
        charRotations.forEachIndexed { index, anim ->
            launch {
                delay(100L * index)
                anim.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(800, easing = FastOutSlowInEasing)
                )
            }
        }
        
        // Particle effects start after logo appears - hiển thị sớm hơn
        delay(300) // Giảm thời gian chờ xuống
        
        // Launch particle animations
        launch {
            particleAlpha.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(800, easing = FastOutSlowInEasing)
            )
            particleAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(700, easing = FastOutSlowInEasing)
            )
        }
        
        launch {
            particleScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing) // Giảm thời gian
            )
        }
        
        launch {
            particleSpread.animateTo(
                targetValue = 1f,
                animationSpec = tween(1200, easing = FastOutSlowInEasing) // Giảm thời gian
            )
        }
        
        // Logo finishing animation - hiển thị sớm hơn
        delay(500) // Giảm thời gian chờ xuống
        launch {
            logoSpread.animateTo(
                targetValue = 1f,
                animationSpec = tween(500, easing = FastOutSlowInEasing)
            )
        }
        
        // Final scale up animation before navigation
        delay(300) // Thêm delay ngắn
        mainScale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(300, easing = LinearEasing)
        )
        
        // Minimum guaranteed display time to ensure animations are seen - giảm thời gian hiển thị
        delay(200) // Giảm thời gian xuống
        
        // Mark animations as completed
        animationsCompleted = true
    }
    
    // Separate LaunchedEffect for navigation to ensure it only happens once
    LaunchedEffect(animationsCompleted) {
        if (animationsCompleted && !navigationTriggered) {
            navigationTriggered = true
            
            // Fade out smoothly before navigating
            screenAlpha.animateTo(
                targetValue = 0f,
                animationSpec = tween(300, easing = LinearEasing)
            )
            
            // Check if user is already logged in
            if (viewModel.isUserLoggedIn()) {
                // Navigate directly to home screen if logged in
                onNavigateToHome()
            } else {
                // Navigate to auth flow if not logged in
                onSplashFinished()
            }
        }
    }
    
    // Netflix-style splash screen with black background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(screenAlpha.value) // Apply the screen alpha to the entire screen
            .onSizeChanged { size = it },
        contentAlignment = Alignment.Center
    ) {
        // Background wave animation
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.3f) // Tăng độ rõ lên
        ) {
            val canvasWidth = size.width.toFloat()
            val canvasHeight = size.height.toFloat()
            
            val waveHeight = canvasHeight * 0.1f
            val frequency = 5f
            val phase = backgroundWave.value * 2f * Math.PI.toFloat()
            
            for (i in 0..4) {
                val path = Path()
                val amplitude = waveHeight * (1f - (i / 5f))
                val verticalOffset = canvasHeight * 0.4f + (i * canvasHeight * 0.1f)
                
                path.moveTo(0f, verticalOffset)
                
                for (x in 0..canvasWidth.toInt() step 10) {
                    val y = sin(x * frequency / canvasWidth + phase + i) * amplitude + verticalOffset
                    path.lineTo(x.toFloat(), y)
                }
                
                path.lineTo(canvasWidth, verticalOffset)
                path.close()
                
                drawPath(
                    path = path,
                    color = NetflixRed.copy(alpha = 0.3f - (i * 0.04f)), // Tăng độ mờ lên
                    style = Stroke(width = 3.dp.toPx()) // Tăng độ dày của đường
                )
            }
        }
        
        // Hiệu ứng glow cho logo
        if (glowIntensity.value > 0) {
            Box(
                modifier = Modifier
                    .size(with(density) { minOf(size.width, size.height).toDp() * 0.6f })
                    .alpha(glowIntensity.value * 0.4f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NetflixRed.copy(alpha = 0.5f),
                                NetflixRed.copy(alpha = 0.2f),
                                NetflixRed.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Particle effects
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(particleAlpha.value)
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val maxRadius = minOf(size.width, size.height).toFloat() * 0.5f
            
            // Tăng số lượng hạt và thay đổi góc để khỏi đều
            for (angle in 0..360 step 12) { // Tăng mật độ hạt
                for (i in 0..3) { // Thêm một lớp hạt nữa
                    val progress = particleSpread.value
                    val distance = maxRadius * (0.3f + (i * 0.2f)) * progress
                    val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
                    val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance
                    
                    // Thay đổi màu sắc giữa các hạt
                    val particleColor = if (i % 2 == 0) NetflixRed else Color.White
                    
                    drawCircle(
                        color = particleColor.copy(alpha = 0.7f - (i * 0.15f)),
                        radius = (5 - i).dp.toPx() * particleScale.value,
                        center = Offset(x, y)
                    )
                }
            }
        }
        
        // Calculate rotation value before using it
        val currentRotation = if (morphProgress.value > 0.5f) {
            0f
        } else {
            rotationZ.value * (1f - morphProgress.value * 2f)
        }
        
        // Main logo with morphing animation
        Box(
            modifier = Modifier
                .size(with(density) { minOf(size.width, size.height).toDp() * 0.5f })
                .scale(mainScale.value)
                .graphicsLayer(
                    rotationZ = currentRotation,
                    alpha = mainAlpha.value,
                    shadowElevation = 8f * glowIntensity.value
                )
        ) {
            // Text animation with character-by-character appearance, fixing overlapping issue
            "CINEVIBE".forEachIndexed { index, char ->
                // Tính toán offset để tránh chữ bị đè lên nhau
                // Tăng khoảng cách giữa các chữ
                val offset = 36.dp * logoSpread.value * (index - charCount / 2f) 
                val charScale = charAnimations[index].value
                val rotation = charRotations[index].value
                
                // Thêm hiệu ứng shadow cho chữ
                val textStyle = TextStyle(
                    fontSize = 52.sp * (0.8f + 0.2f * charScale),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp,
                    shadow = Shadow(
                        color = NetflixRed.copy(alpha = 0.7f),
                        offset = Offset(1f, 1f),
                        blurRadius = 3f * glowIntensity.value
                    )
                )
                
                Text(
                    text = char.toString(),
                    color = NetflixRed,
                    style = textStyle,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(charScale)
                        .offset(x = offset, y = 0.dp)
                        .graphicsLayer(
                            rotationY = rotation
                        )
                )
            }
        }
    }
}