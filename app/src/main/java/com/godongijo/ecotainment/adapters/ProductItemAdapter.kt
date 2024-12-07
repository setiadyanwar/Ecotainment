package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewProductItemBinding
import com.godongijo.ecotainment.models.TransactionItem
import com.godongijo.ecotainment.utilities.Glide
import java.text.NumberFormat
import java.util.Locale

class ProductItemAdapter(
    private var context: Context,
) : RecyclerView.Adapter<ProductItemAdapter.ProductItemViewHolder>() {

    private val items = mutableListOf<TransactionItem>() // List produk dari API

    fun updateData(newData: List<TransactionItem>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItemViewHolder {
        val binding = SingleViewProductItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductItemViewHolder, position: Int) {
        val transactionItem = items[position]
        holder.binding.apply {
            productTitle.text = transactionItem.product?.name

            val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(transactionItem.product?.price) // Format ke format ribuan
            val productAmountText = "${transactionItem.quantity}x Rp${formattedPrice}"
            productAmount.text = productAmountText

            // Load image
            val productImage = context.getString(R.string.base_url) + (transactionItem.product?.imageUrl ?: "")

            Glide().loadImageFromUrl(holder.binding.productImage, productImage)
        }
    }

    override fun getItemCount(): Int = items.size

    class ProductItemViewHolder(val binding: SingleViewProductItemBinding) : RecyclerView.ViewHolder(binding.root)
}
