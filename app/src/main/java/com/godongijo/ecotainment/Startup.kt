package com.godongijo.ecotainment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.godongijo.ecotainment.ui.activities.MainActivity
import com.godongijo.ecotainment.viewModels.SplashViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class Startup : AppCompatActivity() {
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen
        val splashScreen = installSplashScreen()

        // Keep splash screen while loading
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        super.onCreate(savedInstanceState)

        // Observe loading state and navigate when ready
        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                if (!isLoading) {
                    try {
                        val intent = Intent(this@Startup, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e("Startup", "Error saat membuka Aplikasi", e)
                        finish()
                    }
                }
            }
        }
    }
}