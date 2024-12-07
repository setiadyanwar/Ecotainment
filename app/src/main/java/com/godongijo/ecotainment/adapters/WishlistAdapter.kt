package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewWishlistBinding
import com.godongijo.ecotainment.models.Wishlist
import com.godongijo.ecotainment.services.product.WishlistService
import com.godongijo.ecotainment.utilities.Glide
import com.godongijo.ecotainment.utilities.PreferenceManager
import java.text.NumberFormat
import java.util.Locale

class WishlistAdapter(
    private val context: Context,
    private val wishlistService: WishlistService
) : RecyclerView.Adapter<WishlistAdapter.ViewHolder>() {

    private val items = mutableListOf<Wishlist>()
    private val wishlistStatus = mutableListOf<Boolean>()

    fun updateData(newData: List<Wishlist>) {
        items.clear()
        items.addAll(newData)
        wishlistStatus.clear()
        newData.forEach { _ ->
            wishlistStatus.add(true)  // Asumsi bahwa setiap item di daftar wishlist sudah ditandai
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleViewWishlistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val wishlist = items[position]
        holder.binding.apply {
            // Format data wishlist
            productTitle.text = wishlist.product?.name
            val productPriceText = wishlist.product?.price?.let { formatCurrency(it.toLong()) }
            productPrice.text = "Rp$productPriceText"

            val imageProduct = context.getString(R.string.base_url) + wishlist.product?.imageUrl
            Glide().loadImageFromUrl(holder.binding.productImage, imageProduct)

            // Gambar ikon favorit berdasarkan status wishlist
            if (wishlistStatus[position]) {
                favoriteIcon.setImageResource(R.drawable.ic_wishlist_fill)
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_wishlist_outline)
            }

            favoriteIcon.setOnClickListener {
                val authToken = PreferenceManager(context).getString("auth_token") ?: ""
                if (authToken != "") {
                    wishlist.product?.id?.let { it1 ->
                        wishlistService.toggleWishlist(
                            context,
                            it1,
                            onResult = { message ->
                                wishlistStatus[position] = !wishlistStatus[position]
                                notifyItemChanged(position)
                            },
                            onError = { errorMessage ->
                                // Tangani error jika perlu
                            }
                        )
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }

    class ViewHolder(val binding: SingleViewWishlistBinding) : RecyclerView.ViewHolder(binding.root)
}
