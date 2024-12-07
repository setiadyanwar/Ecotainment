package com.godongijo.ecotainment.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewProductBinding
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.services.product.WishlistService
import com.godongijo.ecotainment.ui.activities.DetailProductActivity
import com.godongijo.ecotainment.utilities.Glide
import com.godongijo.ecotainment.utilities.PreferenceManager
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var context: Context,
    private var productList: List<Product>,
    private val productService: ProductService
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    private val wishlistStatus = mutableListOf<Boolean>()

    inner class ProductViewHolder(val binding: SingleViewProductBinding) : RecyclerView.ViewHolder(binding.root)

    init {
        productList.forEach { _ ->
            wishlistStatus.add(false)
        }
        checkWishlistStatus()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = SingleViewProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        val imageProduct = context.getString(R.string.base_url) + product.imageUrl
        Glide().loadImageFromUrl(holder.binding.productImage, imageProduct)

        val price = product.price
        val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(price)

        holder.binding.apply {
            productName.text = product.name
            productPrice.text = "Rp$formattedPrice"
            productRating.text = product.rating.toString()
            productSales.text = "${product.totalSales} terjual"

            if (wishlistStatus[position]) {
                favoriteIcon.setImageResource(R.drawable.ic_wishlist_fill)
            } else {
                favoriteIcon.setImageResource(R.drawable.ic_wishlist_outline)
            }

            favoriteIcon.setOnClickListener {
                val authToken = PreferenceManager(context).getString("auth_token") ?: ""
                if (authToken != "") {
                    val wishlistService = WishlistService()
                    wishlistService.toggleWishlist(
                        context,
                        product.id,
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

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailProductActivity::class.java).apply {
                putExtra("product_id", product.id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = productList.size

    fun updateList(newProductList: List<Product>) {
        productList = newProductList
        wishlistStatus.clear()
        newProductList.forEach { _ ->
            wishlistStatus.add(false)
        }
        checkWishlistStatus()
        notifyDataSetChanged()
    }

    private fun checkWishlistStatus() {
        val authToken = PreferenceManager(context).getString("auth_token") ?: ""
        if (authToken != "") {
            val wishlistService = WishlistService()
            wishlistService.getAllWishlist(
                context,
                onResult = { wishlistList ->
                    wishlistList.forEach { wishlistItem ->
                        val index = productList.indexOfFirst { it.id == wishlistItem.productId }
                        if (index != -1) {
                            wishlistStatus[index] = true
                        }
                    }
                    notifyDataSetChanged()
                },
                onError = { errorMessage ->
                    // Tangani error jika perlu
                }
            )
        }
    }
}
