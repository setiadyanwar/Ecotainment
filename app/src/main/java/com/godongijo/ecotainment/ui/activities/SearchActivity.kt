package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.godongijo.ecotainment.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

    }
}