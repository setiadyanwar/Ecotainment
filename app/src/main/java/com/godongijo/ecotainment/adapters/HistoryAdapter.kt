package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.databinding.SingleViewHistoryBinding
import com.godongijo.ecotainment.models.Transaction
import java.text.NumberFormat
import java.util.Locale

class HistoryAdapter(
    private val context: Context
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private val items = mutableListOf<Transaction>()

    fun updateData(newData: List<Transaction>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleViewHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = items[position]
        holder.binding.apply {
            // Format data transaksi
            totalAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"
            totalProduct.text = "Total ${transaction.items.size} produk"

            // Atur RecyclerView untuk produk dalam transaksi
            val productItemAdapter = ProductItemAdapter(context)
            productItemAdapter.updateData(transaction.items)
            productItemRecycler.adapter = productItemAdapter
            productItemRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }

    class ViewHolder(val binding: SingleViewHistoryBinding) : RecyclerView.ViewHolder(binding.root)
}