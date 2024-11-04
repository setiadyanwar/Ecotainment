package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityMainBinding
import com.godongijo.ecotainment.ui.fragment.*
import com.godongijo.ecotainment.ui.fragments.HomeFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedTab = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment to HomeFragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()

        setupNavbar()

        val shapeAppearanceModel = ShapeAppearanceModel.builder()
            .setAllCornerSizes(ShapeAppearanceModel.PILL) // Membuat FAB berbentuk bulat
            .build()

        // Buat drawable dengan stroke dan warna isi
        val backgroundDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
            setFillColor(ContextCompat.getColorStateList(this@MainActivity, R.color.primary_300)) // Warna isi FAB
            strokeWidth = 4f // Ketebalan garis tepi (stroke)
            strokeColor = ContextCompat.getColorStateList(this@MainActivity, R.color.stroke_color) // Warna garis tepi
        }

        // Terapkan drawable sebagai background untuk FloatingActionButton melalui binding
        binding.navShop.background = backgroundDrawable
    }

    private fun setupNavbar() {
        val homeLayout = binding.navHome
        val wishlistLayout = binding.navWishlist
        val shopLayout = binding.navShop
        val historyLayout = binding.navHistory
        val profileLayout = binding.navProfile

        // Set click listeners for each tab
        homeLayout.setOnClickListener {
            if (selectedTab != 1) {
                // Set up profile tab with icon and text visibility
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()

                // Hide other tab texts
//                binding.navHomeText.visibility = View.GONE
//                binding.navWishlistText.visibility = View.GONE
//                binding.navHistoryText.visibility = View.GONE

                // Set default icons for other tabs
                binding.navProfileIcon.setImageResource(R.drawable.ic_profile_not_fill)
                binding.navWishlistIcon.setImageResource(R.drawable.ic_love_not_fill)
                binding.navHistoryIcon.setImageResource(R.drawable.ic_history_not_fill)


                binding.navHistoryText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navWishlistText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navProfileText.setTextColor(ContextCompat.getColor(this, R.color.grey))


//                // Reset backgrounds for other tabs
//                homeLayout.setBackgroundResource(R.drawable.bg_round_50)
//                wishlistLayout.setBackgroundResource(R.drawable.bg_round_50)
//                historyLayout.setBackgroundResource(R.drawable.bg_round_50)

                // Show profile tab text and set icon
                binding.navHomeText.setTextColor(ContextCompat.getColor(this, R.color.primary_300))
                binding.navHomeIcon.setImageResource(R.drawable.ic_home_fill)
//                profileLayout.setBackgroundResource(R.drawable.round_back_home_100)

                // Create Animation
//                val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//                scaleAnimation.duration = 200
//                scaleAnimation.fillAfter = true
//                profileLayout.startAnimation(scaleAnimation)
                selectedTab = 1
            }
        }

        wishlistLayout.setOnClickListener {
            if (selectedTab != 2) {
                // Set up profile tab with icon and text visibility
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, WishlistFragment())
                    .commit()

                // Hide other tab texts
//                binding.navHomeText.visibility = View.GONE
//                binding.navWishlistText.visibility = View.GONE
//                binding.navHistoryText.visibility = View.GONE

                // Set default icons for other tabs
                binding.navHomeIcon.setImageResource(R.drawable.ic_home_not_fill)
                binding.navProfileIcon.setImageResource(R.drawable.ic_profile_not_fill)
                binding.navHistoryIcon.setImageResource(R.drawable.ic_history_not_fill)


//                // Reset backgrounds for other tabs
//                homeLayout.setBackgroundResource(R.drawable.bg_round_50)
//                wishlistLayout.setBackgroundResource(R.drawable.bg_round_50)
//                historyLayout.setBackgroundResource(R.drawable.bg_round_50)

                binding.navHomeText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navHistoryText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navProfileText.setTextColor(ContextCompat.getColor(this, R.color.grey))

                // Show profile tab text and set icon
                binding.navWishlistText.setTextColor(ContextCompat.getColor(this, R.color.primary_300))
                binding.navWishlistIcon.setImageResource(R.drawable.ic_love_fill)
//                profileLayout.setBackgroundResource(R.drawable.round_back_home_100)

                // Create Animation
//                val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//                scaleAnimation.duration = 200
//                scaleAnimation.fillAfter = true
//                profileLayout.startAnimation(scaleAnimation)

                selectedTab = 2
            }
        }

        // Shop tab click listener (without text)
        shopLayout.setOnClickListener {
            if (selectedTab != 3) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ShopFragment())
                    .commit()
                binding.navHomeIcon.setImageResource(R.drawable.ic_home_not_fill)
                binding.navWishlistIcon.setImageResource(R.drawable.ic_love_not_fill)
                binding.navProfileIcon.setImageResource(R.drawable.ic_profile_not_fill)
                binding.navHistoryIcon.setImageResource(R.drawable.ic_history_not_fill)// No text for Shop


                binding.navHomeText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navWishlistText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navProfileText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navHistoryText.setTextColor(ContextCompat.getColor(this, R.color.grey))


                selectedTab = 3
            }
        }

        historyLayout.setOnClickListener {
            if (selectedTab != 4) {
                // Set up profile tab with icon and text visibility
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HistoryFragment())
                    .commit()

                // Hide other tab texts
//                binding.navHomeText.visibility = View.GONE
//                binding.navWishlistText.visibility = View.GONE
//                binding.navHistoryText.visibility = View.GONE

                // Set default icons for other tabs
                binding.navHomeIcon.setImageResource(R.drawable.ic_home_not_fill)
                binding.navWishlistIcon.setImageResource(R.drawable.ic_love_not_fill)
                binding.navProfileIcon.setImageResource(R.drawable.ic_profile_not_fill)

                binding.navHomeText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navWishlistText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navProfileText.setTextColor(ContextCompat.getColor(this, R.color.grey))

//                // Reset backgrounds for other tabs
//                homeLayout.setBackgroundResource(R.drawable.bg_round_50)
//                wishlistLayout.setBackgroundResource(R.drawable.bg_round_50)
//                historyLayout.setBackgroundResource(R.drawable.bg_round_50)

                // Show profile tab text and set icon
                binding.navHistoryText.setTextColor(ContextCompat.getColor(this, R.color.primary_300))
                binding.navHistoryIcon.setImageResource(R.drawable.ic_history_fill)
//                profileLayout.setBackgroundResource(R.drawable.round_back_home_100)

                // Create Animation
//                val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//                scaleAnimation.duration = 200
//                scaleAnimation.fillAfter = true
//                profileLayout.startAnimation(scaleAnimation)
                selectedTab = 4
            }
        }

        profileLayout.setOnClickListener {
            if (selectedTab != 5) {
                // Set up profile tab with icon and text visibility
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ProfileFragment())
                    .commit()

                // Hide other tab texts
//                binding.navHomeText.visibility = View.GONE
//                binding.navWishlistText.visibility = View.GONE
//                binding.navHistoryText.visibility = View.GONE

                // Set default icons for other tabs
                binding.navHomeIcon.setImageResource(R.drawable.ic_home_not_fill)
                binding.navWishlistIcon.setImageResource(R.drawable.ic_love_not_fill)
                binding.navHistoryIcon.setImageResource(R.drawable.ic_history_not_fill)

                binding.navHomeText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navWishlistText.setTextColor(ContextCompat.getColor(this, R.color.grey))
                binding.navHistoryText.setTextColor(ContextCompat.getColor(this, R.color.grey))


//                // Reset backgrounds for other tabs
//                homeLayout.setBackgroundResource(R.drawable.bg_round_50)
//                wishlistLayout.setBackgroundResource(R.drawable.bg_round_50)
//                historyLayout.setBackgroundResource(R.drawable.bg_round_50)

                // Show profile tab text and set icon
                binding.navProfileText.setTextColor(ContextCompat.getColor(this, R.color.primary_300))
                binding.navProfileIcon.setImageResource(R.drawable.ic_profile_fill)
//                profileLayout.setBackgroundResource(R.drawable.round_back_home_100)

                // Create Animation
//                val scaleAnimation = ScaleAnimation(0.8f, 1.0f, 1f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//                scaleAnimation.duration = 200
//                scaleAnimation.fillAfter = true
//                profileLayout.startAnimation(scaleAnimation)

                selectedTab = 5
            }
        }
    }

    /**
     * Method to set the selected tab as active. Updates icon and background.
     */
    private fun setActiveTab(
        layout: View,
        icon: ImageView,
        activeIconRes: Int,
        fragment: Fragment
    ) {
        icon.setImageResource(activeIconRes) // Set the active icon
//        layout.setBackgroundResource(R.drawable.round_back_home_100) // Set background for active tab

        // Replace fragment based on the selected tab
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        // Add a small scale animation for the selected tab
//        val scaleAnimation = ScaleAnimation(
//            0.8f, 1.0f, 1f, 1f,
//            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
//        )
//        scaleAnimation.duration = 200
//        scaleAnimation.fillAfter = true
//        layout.startAnimation(scaleAnimation)
    }

    /**
     * Method specifically for tabs without text (e.g., Shop).
     */
    private fun setActiveTabNoText(
        layout: View,
        activeIconRes: Int,
        fragment: Fragment
    ) {
        val icon = layout.findViewById<ImageView>(R.id.navShop) // Assuming this ID is set correctly
        icon.setImageResource(activeIconRes) // Set the active icon for the Shop tab
//        layout.setBackgroundResource(R.drawable.round_back_home_100) // Set background for active tab

        // Replace fragment based on the selected tab
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

//        // Add a small scale animation for the selected tab
////        val scaleAnimation = ScaleAnimation(
////            0.8f, 1.0f, 1f, 1f,
////            Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
////        )
//        scaleAnimation.duration = 200
//        scaleAnimation.fillAfter = true
//        layout.startAnimation(scaleAnimation)
    }

    /**
     * Method to reset all other tabs to the default icon and background.
     */
    private fun resetTabs(vararg views: Any) {
        views.forEach { view ->
            when (view) {
                is TextView -> {
                    view.visibility = View.GONE
                    view.setTextColor(ContextCompat.getColor(this, R.color.grey)) // Default text color for inactive tabs
                }
//                is ImageView -> view.setImageResource(R.drawable.ic_default_icon) // Default icon for inactive tabs
//                is View -> view.setBackgroundResource(R.drawable.bg_round_50) // Default background for inactive tabs
            }
        }
    }



}
