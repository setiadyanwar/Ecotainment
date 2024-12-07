package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityVirtualTripBinding

class VirtualTripActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVirtualTripBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVirtualTripBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()


        // Set up the image slider with slide models
        val slideModels = listOf(
            SlideModel(R.drawable.banner_vt, null, ScaleTypes.FIT),

            )
        binding.imageSlider.setImageList(slideModels)
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}
