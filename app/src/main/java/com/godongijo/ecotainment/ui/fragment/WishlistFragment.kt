package com.godongijo.ecotainment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.adapters.WishlistAdapter
import com.godongijo.ecotainment.databinding.FragmentWishlistBinding
import com.godongijo.ecotainment.models.SkeletonLayoutType
import com.godongijo.ecotainment.services.product.WishlistService
import com.godongijo.ecotainment.ui.activities.NotificationActivity

class WishlistFragment : Fragment() {

    private lateinit var binding: FragmentWishlistBinding

    private lateinit var wishlistAdapter: WishlistAdapter

    private val wishlistService = WishlistService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init()
        setListeners()
    }

    private fun init() {
        initWishlist()
    }

    private fun setListeners() {
        binding.notificationButton.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
    }

    private fun initWishlist() {
        binding.wishlistRecycler.visibility = View.VISIBLE
        binding.emptyLayout.visibility = View.GONE

        wishlistAdapter = WishlistAdapter(requireContext(), wishlistService)
        binding.wishlistRecycler.layoutManager = LinearLayoutManager(requireContext())

        val skeletonAdapter = SkeletonAdapter(10, SkeletonLayoutType.WISHLIST)
        binding.wishlistRecycler.adapter = skeletonAdapter

        wishlistService.getAllWishlist(
            context = requireContext(),
            onResult = { wishlist ->
                if (wishlist.isEmpty()) {
                    binding.wishlistRecycler.visibility = View.GONE
                    binding.emptyLayout.visibility = View.VISIBLE
                } else {
                    // Update the adapter with new data
                    wishlistAdapter.updateData(wishlist)

                    binding.wishlistRecycler.adapter = wishlistAdapter

                    binding.wishlistRecycler.visibility = View.VISIBLE
                    binding.emptyLayout.visibility = View.GONE
                }
            },
            onError = {}
        )

    }
}
