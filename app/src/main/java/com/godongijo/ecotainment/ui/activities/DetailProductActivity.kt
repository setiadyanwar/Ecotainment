package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.ProductDetailAdapter
import com.godongijo.ecotainment.databinding.ActivityDetailProductBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class DetailProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val adapter = ProductDetailAdapter(this)
        binding.vP2.adapter = adapter

        // Connect TabLayout with ViewPager2 using TabLayoutMediator
        TabLayoutMediator(binding.tabLayout, binding.vP2) { tab, position ->
            tab.customView = createCustomTabView(
                when (position) {
                    0 -> "Deskripsi"           // Format dengan huruf besar kecil
                    1 -> "Ulasan Produk"        // Format dengan huruf besar kecil
                    else -> ""
                }
            )
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Set the selected tab text color
                val textView = tab?.customView as? TextView
                textView?.setTextColor(getColor(R.color.primary_700)) // Warna teks untuk tab aktif
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Set the unselected tab text color
                val textView = tab?.customView as? TextView
                textView?.setTextColor(getColor(R.color.grey)) // Warna teks untuk tab tidak aktif
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Optional: Handle reselection if needed
            }
        })
    }


    private fun createCustomTabView(text: String): TextView {
        val textView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        textView.text = text
        return textView
    }

}