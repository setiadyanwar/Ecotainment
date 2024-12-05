package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityGotoSchoolBinding

class GotoSchoolActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGotoSchoolBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGotoSchoolBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()


        // Set up the image slider with slide models
        val slideModels = listOf(
            SlideModel(R.drawable.banner_gtc, null, ScaleTypes.FIT),

            )
        binding.imageSlider.setImageList(slideModels)

    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}