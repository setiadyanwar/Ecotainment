package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityGotoSchoolBinding
import com.godongijo.ecotainment.databinding.ActivityReservationBinding

class ReservationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReservationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReservationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()



    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
