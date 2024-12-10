package com.godongijo.ecotainment.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewReviewItemBinding
import com.godongijo.ecotainment.models.TransactionItem
import com.godongijo.ecotainment.utilities.Glide

class ReviewProductAdapter(
    private var context: Context,
    private var transactionList: List<TransactionItem>
) : RecyclerView.Adapter<ReviewProductAdapter.ReviewViewHolder>() {

    private val reviews = mutableMapOf<Int, Pair<Int, String>>()

    inner class ReviewViewHolder(val binding: SingleViewReviewItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = SingleViewReviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val transactionItem = transactionList[position]

        holder.binding.apply {
            tvProductName.text = transactionItem.product?.name ?: ""

            val productPrice = transactionItem.product?.price ?: 0
            tvProductPrice.text = "Rp$productPrice"

            if (!transactionItem.product?.imageUrl.isNullOrEmpty()) {
                val imageProduct = context.getString(R.string.base_url) + transactionItem.product!!.imageUrl
                Glide().loadImageFromUrl(productImage, imageProduct)
            }

            // Set default rating to 5
            ratingBar.rating = 5f
            reviews[transactionItem.product!!.id] = Pair(5, reviews[transactionItem.product.id]?.second ?: "")

            ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                selectedRatingText.text = rating.toString()
                reviews[transactionItem.product!!.id] =
                    Pair(rating.toInt(), reviews[transactionItem.product.id]?.second ?: "")
            }

            etReview.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    reviews[transactionItem.product!!.id] =
                        Pair(reviews[transactionItem.product.id]?.first ?: 0, s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }

    override fun getItemCount(): Int = transactionList.size

    fun updateList(newTransactionList: List<TransactionItem>) {
        transactionList = newTransactionList
        notifyDataSetChanged()
    }

    fun getAllReviews(): Map<Int, Pair<Int, String>> {
        return reviews
    }
}
