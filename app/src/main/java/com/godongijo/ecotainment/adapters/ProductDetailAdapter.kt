package com.godongijo.ecotainment.adapters

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.godongijo.ecotainment.models.Review
import com.godongijo.ecotainment.ui.fragment.CommentFragment
import com.godongijo.ecotainment.ui.fragment.DescriptionFragment

class ProductDetailAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private var productDescription: String? = null

    private var productReviews: List<Review> = emptyList()

    private var rating: Double = 0.0

    // Set deskripsi produk untuk fragment Description
    fun setProductDescription(description: String?) {
        this.productDescription = description
    }

    fun setProductReviews(reviews: List<Review>, rating: Double) {
        productReviews = reviews
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return 2 // Jumlah tab
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                val descriptionFragment = DescriptionFragment()
                val bundle = Bundle()
                bundle.putString("description", productDescription)
                descriptionFragment.arguments = bundle
                descriptionFragment
            }
            1 -> {
                val commentFragment = CommentFragment()
                val bundle = Bundle()
                bundle.putParcelableArrayList("reviews", ArrayList(productReviews))
                bundle.putDouble("rating", rating)
                commentFragment.arguments = bundle
                commentFragment
            }
            else -> throw IllegalStateException("Invalid position $position")
        }
    }

}
