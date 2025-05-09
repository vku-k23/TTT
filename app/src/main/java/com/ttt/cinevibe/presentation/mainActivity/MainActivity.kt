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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.presentation.NavDestinations
import com.ttt.cinevibe.presentation.auth.AUTH_GRAPH_ROUTE
import com.ttt.cinevibe.presentation.auth.authNavGraph
import com.ttt.cinevibe.presentation.detail.MovieDetailScreen
import com.ttt.cinevibe.presentation.main.MainScreen
import com.ttt.cinevibe.presentation.splash.SplashScreen
import com.ttt.cinevibe.ui.theme.CineVibeTheme
import com.ttt.cinevibe.utils.FirebaseInitializer
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
private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    // Track current language to prevent unnecessary recreations
    private var currentLanguage = ""

    // Use a safer approach for attachBaseContext that doesn't depend early on dependency injection
    override fun attachBaseContext(newBase: Context) {
        // Use the system's default language instead of reading from languageManager
        // Language will be applied correctly in onCreate after languageManager is injected
        super.attachBaseContext(LocaleHelper.setLocale(newBase, Locale.getDefault()))
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Make the app immersive for the splash screen
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        
        // Check Google Play Services first
        if (!checkGooglePlayServices()) {
            Log.e("MainActivity", "Google Play Services not available or up-to-date")
        }
        
        lifecycleScope.launch {
            try {
                // Load language settings
                val locale = languageManager.getAppLanguage().first()
                currentLanguage = locale.language
                
                val currentUiMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                
                // Set content with the application theme
                setContent {
                    CineVibeTheme(
                        darkTheme = currentUiMode == Configuration.UI_MODE_NIGHT_YES
                    ) {
                        // Observe locale changes
                        val appLocale by languageManager.getAppLanguage().collectAsState(locale)
                        
                        // Use key to monitor actual language changes
                        val language = appLocale.language
                        
                        // Only trigger recreation if the language actually changed
                        LaunchedEffect(language) {
                            if (currentLanguage.isNotEmpty() && language != currentLanguage) {
                                Log.d("MainActivity", "Locale changed from $currentLanguage to $language")
                                
                                // Force activity recreation with proper flags
                                val intent = intent.apply { 
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                finish()
                                startActivity(intent)
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                                
                                // Update tracked language
                                currentLanguage = language
                            }
                        }
                        
                        CineVibeApp(appLocale = appLocale)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing app", e)
                // Fallback to default locale
                setContent {
                    CineVibeTheme {
                        CineVibeApp(appLocale = Locale.getDefault())
                    }
                }
            }
        }
    }

    /**
     * Checks if Google Play Services are available and up to date.
     * If not, it will show a dialog to update them.
     */
    private fun checkGooglePlayServices(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        
        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(
                    this, 
                    resultCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST
                )?.show()
            } else {
                Log.e("MainActivity", "Device doesn't support Google Play Services")
            }
            return false
        }
        
        return true
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        // Handle Google Play Services resolution
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d("MainActivity", "Google Play Services updated successfully")
                // Reinitialize Firebase after Play Services update
                FirebaseInitializer.initializeFirebase(this)
            } else {
                Log.e("MainActivity", "Google Play Services update failed or canceled")
            }
        }
    }
}

@Composable
fun CineVibeApp(appLocale: Locale = Locale.getDefault()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // Track whether navigation has occurred from SplashScreen 
    var navigatedFromSplash by remember { mutableStateOf(false) }
    
    // Apply the app locale immediately and whenever it changes
    SideEffect {
        LocaleConfigurationProvider.applyLocaleToContext(context, appLocale)
    }
    
    // Provide the locale to all child composables
    CompositionLocalProvider(LocalAppLocale provides appLocale) {
        // Start with the enhanced splash screen as the launcher
        NavHost(navController = navController, startDestination = SPLASH_ROUTE) {
            // Splash screen acts as launcher
            composable(route = SPLASH_ROUTE) {
                SplashScreen(
                    onSplashFinished = {
                        if (!navigatedFromSplash) {
                            navigatedFromSplash = true
                            // Navigate to auth flow when user is not logged in
                            navController.navigate(AUTH_GRAPH_ROUTE) {
                                // Remove splash from back stack
                                popUpTo(SPLASH_ROUTE) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToHome = {
                        if (!navigatedFromSplash) {
                            navigatedFromSplash = true
                            // Navigate directly to main flow when user is already logged in
                            navController.navigate(NavDestinations.MAIN_FLOW) {
                                // Remove splash from back stack
                                popUpTo(SPLASH_ROUTE) { inclusive = true }
                            }
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
                        Log.d("MovieDetail", "Applying locale: ${appLocale.language} on Movie Detail")
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
