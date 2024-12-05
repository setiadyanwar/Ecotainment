package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewCheckoutBinding
import com.godongijo.ecotainment.models.CartItem
import com.godongijo.ecotainment.services.cart.CartService
import com.godongijo.ecotainment.utilities.Glide
import java.text.NumberFormat
import java.util.Locale

class CheckoutAdapter(
    private var context: Context,
    private var cartItemList: MutableList<CartItem>, // Menggunakan MutableList agar bisa diubah
    private val cartService: CartService,
) : RecyclerView.Adapter<CheckoutAdapter.CheckoutItemViewHolder>() {

    inner class CheckoutItemViewHolder(val binding: SingleViewCheckoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutItemViewHolder {
        val binding = SingleViewCheckoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CheckoutItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutItemViewHolder, position: Int) {
        val cartItem = cartItemList[position]
        val product = cartItem.product

        val imageProduct = context.getString(R.string.base_url) + (product?.imageUrl ?: "")
        Glide().loadImageFromUrl(holder.binding.productImage, imageProduct)

        // Format harga
        val price = product?.price?: 0
        val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(price)

        holder.binding.apply {
            productName.text = product?.name ?: "Produk Tidak Ditemukan"
            productPrice.text = "Rp$formattedPrice"

            var currentQuantity = cartItem.quantity
            productQuantity.text = "${currentQuantity.toString()} x"
        }
    }

    override fun getItemCount(): Int = cartItemList.size

    // Memperbarui list dengan data baru
    fun updateList(newCartItemList: List<CartItem>) {
        cartItemList = newCartItemList.toMutableList() // Pastikan list bersifat Mutable
        notifyDataSetChanged()
    }

}

