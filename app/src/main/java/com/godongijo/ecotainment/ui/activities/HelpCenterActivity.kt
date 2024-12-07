package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.godongijo.ecotainment.databinding.ActivityHelpCenterBinding

class HelpCenterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpCenterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpCenterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}