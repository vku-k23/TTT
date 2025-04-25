package com.ttt.cinevibe

import android.app.Application
import com.ttt.cinevibe.data.manager.LanguageManager
import com.ttt.cinevibe.utils.LocaleHelper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CineVibeApp : Application() {

    @Inject
    lateinit var languageManager: LanguageManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        
        // Set up language observer at application startup
        observeLanguageChanges()
    }
    
    private fun observeLanguageChanges() {
        applicationScope.launch {
            languageManager.getAppLanguage().collectLatest { locale ->
                LocaleHelper.setLocale(this@CineVibeApp, locale)
            }
        }
    }
}