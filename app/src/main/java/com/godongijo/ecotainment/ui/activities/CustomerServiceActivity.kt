package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.godongijo.ecotainment.databinding.ActivityCustomerServiceBinding

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