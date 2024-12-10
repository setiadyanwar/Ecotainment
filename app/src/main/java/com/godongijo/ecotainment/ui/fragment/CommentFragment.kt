package com.godongijo.ecotainment.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.ReviewsAdapter
import com.godongijo.ecotainment.databinding.FragmentCommentBinding
import com.godongijo.ecotainment.models.Review


class CommentFragment : Fragment() {

    private lateinit var binding: FragmentCommentBinding

    private var reviews: List<Review> = emptyList()

    private var rating: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reviews = it.getParcelableArrayList("reviews") ?: emptyList()
            rating = it.getDouble("rating")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCommentBinding.inflate(layoutInflater)

        if(reviews.isNotEmpty()) {
            // Setup RecyclerView
            val adapter = ReviewsAdapter()
            binding.recyclerViewReviews.layoutManager = LinearLayoutManager(context)
            binding.recyclerViewReviews.adapter = adapter

            // Set reviews data to RecyclerView
            adapter.submitList(reviews)

            binding.ratingBar.rating = rating.toFloat()
            binding.textRating.text = rating.toString()
            binding.countReviews.text = "(${reviews.size.toString()} Ulasan)"

            binding.layoutRating.visibility = View.VISIBLE
            binding.emptyReviews.visibility = View.GONE
        } else {
            binding.layoutRating.visibility = View.GONE
            binding.emptyReviews.visibility = View.VISIBLE
        }

        return binding.root
    }

}