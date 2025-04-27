package com.ttt.cinevibe.presentation.mainActivity

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
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
import com.ttt.cinevibe.presentation.NavDestinations
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.auth.authNavGraph
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.main.MainScreen
import com.ttt.cinevibe.presentation.splash.SplashScreen
import com.ttt.cinevibe.ui.theme.CineVibeTheme
import com.ttt.cinevibe.utils.LanguageContextWrapper
import com.ttt.cinevibe.utils.LocalAppLocale
import com.ttt.cinevibe.utils.LocaleConfigurationProvider
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
            
            // Get the current app language as a collectAsState so composables can react to changes
            val appLocale by languageManager.getAppLanguage().collectAsState(initial = Locale.getDefault())
            
            // Apply locale changes when the composition starts
            val context = LocalContext.current
            DisposableEffect(appLocale) {
                // Apply locale to the context immediately when it changes
                LocaleHelper.setLocale(context, appLocale)
                
                onDispose { }
            }
            
            CineVibeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CineVibeApp(appLocale = appLocale)
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
fun CineVibeApp(appLocale: Locale = Locale.getDefault()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val localConfiguration = LocalConfiguration.current
    
    // Apply the app locale immediately and whenever it changes
    SideEffect {
        LocaleConfigurationProvider.applyLocaleToContext(context, appLocale)
    }
    
    // Provide the locale to all child composables
    CompositionLocalProvider(LocalAppLocale provides appLocale) {
        // Start with the splash screen route
        NavHost(navController = navController, startDestination = SPLASH_ROUTE) {
            // Splash screen
            composable(route = SPLASH_ROUTE) {
                // Splash screen doesn't need special locale handling
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(AUTH_GRAPH_ROUTE) {
                            popUpTo(SPLASH_ROUTE) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(NavDestinations.MAIN_FLOW) {
                            popUpTo(SPLASH_ROUTE) { inclusive = true }
                        }
                    }
                )
            }
            
            // Auth flow
            authNavGraph(
                navController = navController,
                onAuthSuccess = {
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
                    val detailContext = LocalContext.current
                    
                    // Force locale application on each navigation to detail screen
                    LaunchedEffect(appLocale) {
                        Log.d("MovieDetail", "Forcefully applying locale: ${appLocale.language} on navigation to Movie Detail")
                        Locale.setDefault(appLocale) // Set JVM default locale
                        LocaleConfigurationProvider.applyLocaleToContext(detailContext, appLocale)
                    }
                    
                    // Use our improved LanguageContextWrapper
                    LanguageContextWrapper(locale = appLocale) {
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
