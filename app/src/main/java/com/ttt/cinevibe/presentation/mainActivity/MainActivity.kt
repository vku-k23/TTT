package com.ttt.cinevibe.presentation.mainActivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.domain.model.Movie
import com.ttt.cinevibe.presentation.NavDestinations
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.auth.authNavGraph
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.main.MainScreen
import com.ttt.cinevibe.presentation.splash.SplashScreen
import com.ttt.cinevibe.ui.theme.CineVibeTheme
import com.ttt.cinevibe.utils.LocaleHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

// Define a constant for the splash route
private const val SPLASH_ROUTE = "splash"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var languageManager: LanguageManager

    // Sử dụng phương pháp an toàn hơn cho attachBaseContext mà không phụ thuộc vào sớm vào dependency injection
    override fun attachBaseContext(newBase: Context) {
        // Sử dụng ngôn ngữ mặc định của hệ thống thay vì đọc từ languageManager
        // Ngôn ngữ sẽ được áp dụng chính xác trong onCreate sau khi languageManager được tiêm
        super.attachBaseContext(LocaleHelper.setLocale(newBase, Locale.getDefault()))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Áp dụng ngôn ngữ đã lưu ngay khi có thể sau khi languageManager được tiêm
        applyStoredLanguage()
        
        // Observe language changes to recreate activity when needed
        observeLanguageChanges()

        setContent {
            val navController = rememberNavController()
            
            // Apply locale changes when the composition starts
            val context = LocalContext.current
            DisposableEffect(Unit) {
                val job = lifecycleScope.launch {
                    languageManager.getAppLanguage().collectLatest { locale ->
                        LocaleHelper.setLocale(context, locale)
                    }
                }
                onDispose {
                    job.cancel()
                }
            }
            
            CineVibeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CineVibeApp()
                }
            }
        }
    }
    
    // Phương thức mới để áp dụng ngôn ngữ đã lưu ngay sau khi languageManager được tiêm
    private fun applyStoredLanguage() {
        lifecycleScope.launch {
            try {
                val locale = languageManager.getAppLanguage().first()
                LocaleHelper.setLocale(this@MainActivity, locale)
                // Cập nhật resources
                resources.configuration.setLocale(locale)
                resources.updateConfiguration(resources.configuration, resources.displayMetrics)
            } catch (e: Exception) {
                // Xử lý ngoại lệ nếu có
                e.printStackTrace()
            }
        }
    }
    
    private fun observeLanguageChanges() {
        var currentLanguage = ""
        
        lifecycleScope.launch {
            languageManager.getAppLanguage().collectLatest { locale ->
                // Only trigger recreation if we've previously set a language and it's changing
                if (currentLanguage.isNotEmpty() && currentLanguage != locale.language) {
                    // Critical: Use a delay to ensure preferences are saved before recreation
                    kotlinx.coroutines.delay(100)
                    
                    // Force activity recreation with proper flags
                    val intent = intent.apply { 
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    finish()
                    startActivity(intent)
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                currentLanguage = locale.language
            }
        }
    }
}

@Composable
fun CineVibeApp() {
    val navController = rememberNavController()
    
    // Start with the splash screen route
    NavHost(navController = navController, startDestination = SPLASH_ROUTE) {
        // Splash screen
        composable(route = SPLASH_ROUTE) {
            SplashScreen(
                onSplashFinished = {
                    // Navigate to auth flow when splash is done and user is NOT logged in
                    navController.navigate(AUTH_GRAPH_ROUTE) {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    // Navigate directly to main flow if user is already logged in
                    navController.navigate(NavDestinations.MAIN_FLOW) {
                        popUpTo(SPLASH_ROUTE) { inclusive = true }
                    }
                }
            )
        }
        
        // Firebase Auth flow using our new components
        authNavGraph(
            navController = navController,
            onAuthSuccess = {
                // Navigate to main flow after successful authentication
                navController.navigate(NavDestinations.MAIN_FLOW) {
                    popUpTo(AUTH_GRAPH_ROUTE) { inclusive = true }
                }
            }
        )
        
        // Main app flow
        navigation(
            startDestination = NavDestinations.HOME_ROUTE, 
            route = NavDestinations.MAIN_FLOW
        ) {
            composable(NavDestinations.HOME_ROUTE) {
                MainScreen(rootNavController = navController)
            }
            
            composable(
                route = NavDestinations.MOVIE_DETAIL_WITH_ARGS,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: -1
                
                MovieDetailScreen(
                    movieId = movieId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CineVibeTheme {
        Greeting("Android")
    }
}
