package com.godongijo.ecotainment

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.godongijo.ecotainment.ui.activities.DetailProductActivity
import com.godongijo.ecotainment.ui.activities.MainActivity
import com.godongijo.ecotainment.ui.activities.PaymentActivity
import com.godongijo.ecotainment.viewModels.SplashViewModel

class Startup : AppCompatActivity() {
    // Create an instance of SplashViewModel for managing the splash screen state
    private val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen and keep it on the screen based on the ViewModel's loading state
        val splashScreen = installSplashScreen()

        // Condition to keep the splash screen visible until isLoading value is false
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}