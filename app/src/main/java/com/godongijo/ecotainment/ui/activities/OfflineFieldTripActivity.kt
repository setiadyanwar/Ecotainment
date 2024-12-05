package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityOfflineFieldTripBinding

class OfflineFieldTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOfflineFieldTripBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineFieldTripBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()


        // Set up the image slider with slide models
        val slideModels = listOf(
            SlideModel(R.drawable.banner_of, null, ScaleTypes.FIT),

            )
        binding.imageSlider.setImageList(slideModels)
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}