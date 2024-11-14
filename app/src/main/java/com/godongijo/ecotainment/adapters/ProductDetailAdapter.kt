package com.godongijo.ecotainment.adapters


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.godongijo.ecotainment.ui.fragment.CommentFragment
import com.godongijo.ecotainment.ui.fragment.DescriptionFragment

class ProductDetailAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 2 // Jumlah tab
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DescriptionFragment()
            1 -> CommentFragment()
            else -> throw IllegalStateException("Invalid position $position")
        }
    }
}
