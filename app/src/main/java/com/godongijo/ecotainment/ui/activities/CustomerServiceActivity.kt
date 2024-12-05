package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityCustomerServiceBinding
import com.godongijo.ecotainment.databinding.ActivityNotificationBinding

class CustomerServiceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCustomerServiceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerServiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}