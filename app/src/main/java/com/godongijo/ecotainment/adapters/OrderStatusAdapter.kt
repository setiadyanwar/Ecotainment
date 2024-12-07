package com.godongijo.ecotainment.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.DialogFinishOrderBinding
import com.godongijo.ecotainment.databinding.DialogReviewBinding
import com.godongijo.ecotainment.databinding.SingleViewOrderCancelledBinding
import com.godongijo.ecotainment.databinding.SingleViewOrderCurrentBinding
import com.godongijo.ecotainment.databinding.SingleViewOrderFinishBinding
import com.godongijo.ecotainment.databinding.SingleViewOrderWaitingBinding
import com.godongijo.ecotainment.models.Transaction
import com.godongijo.ecotainment.services.transaction.TransactionService
import com.godongijo.ecotainment.ui.activities.PaymentActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class OrderStatusAdapter(
    private var context: Context,
    private val transactionService: TransactionService
) : RecyclerView.Adapter<OrderStatusAdapter.ViewHolder>() {

    private val items = mutableListOf<Transaction>()

    fun updateData(newData: List<Transaction>) {
        items.clear()
        items.addAll(newData)
        notifyDataSetChanged()
    }

    // Define view types
    companion object {
        const val VIEW_TYPE_WAITING = 0
        const val VIEW_TYPE_CURRENT = 1
        const val VIEW_TYPE_FINISHED = 2
        const val VIEW_TYPE_CANCELLED = 3
    }

    override fun getItemViewType(position: Int): Int {
        val transaction = items[position]
        return when (transaction.status) {
            "pending" -> VIEW_TYPE_WAITING
            "waiting_for_confirmation" -> VIEW_TYPE_WAITING
            "processed" -> VIEW_TYPE_CURRENT
            "on_shipment" -> VIEW_TYPE_CURRENT
            "completed" -> VIEW_TYPE_FINISHED
            "canceled" -> VIEW_TYPE_CANCELLED
            else -> VIEW_TYPE_WAITING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = when (viewType) {
            VIEW_TYPE_WAITING -> SingleViewOrderWaitingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VIEW_TYPE_CURRENT -> SingleViewOrderCurrentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VIEW_TYPE_FINISHED -> SingleViewOrderFinishBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            VIEW_TYPE_CANCELLED -> SingleViewOrderCancelledBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else -> SingleViewOrderWaitingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = items[position]
        when (holder.binding) {
            is SingleViewOrderWaitingBinding -> {
                holder.binding.apply {
                    totalAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"
                    totalProduct.text = "Total ${transaction.items.size} produk:"

                    if (transaction.status == "pending") {
                        layoutTimer.visibility = View.VISIBLE
                        orderStatus.visibility = View.GONE

                        // Timer logic for "pending" status
                        val createdAt = transaction.createdAt
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        try {
                            val createdAtTime = sdf.parse(createdAt)?.time ?: 0
                            val expirationTime = createdAtTime + 12 * 60 * 60 * 1000
                            val currentTime = System.currentTimeMillis()
                            val timeLeftInMillis = expirationTime - currentTime
                            if (timeLeftInMillis > 0) {
                                object : CountDownTimer(timeLeftInMillis, 1000) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        val hours = millisUntilFinished / (1000 * 60 * 60) % 24
                                        val minutes = millisUntilFinished / (1000 * 60) % 60
                                        val seconds = millisUntilFinished / 1000 % 60
                                        tvTimerHours.text = String.format("%02d", hours)
                                        tvTimerMinutes.text = String.format("%02d", minutes)
                                        tvTimerSeconds.text = String.format("%02d", seconds)
                                    }
                                    override fun onFinish() {
                                        tvTimerHours.text = "00"
                                        tvTimerMinutes.text = "00"
                                        tvTimerSeconds.text = "00"
                                        payButton.apply {
                                            isEnabled = false // Nonaktifkan tombol
                                            backgroundTintList =
                                                ContextCompat.getColorStateList(context, R.color.grey) // Ubah warna menjadi abu-abu
                                        }
                                        updateTransactionStatus("canceled", transaction.id)
                                    }
                                }.start()
                            } else {
                                layoutTimer.visibility = View.GONE
                                payButton.apply {
                                    isEnabled = false // Nonaktifkan tombol
                                    backgroundTintList = ContextCompat.getColorStateList(context, R.color.grey) // Ubah warna menjadi abu-abu
                                }
                                orderStatus.text = "Waktu \nHabis"
                                orderStatus.visibility = View.VISIBLE
                                orderStatus.setTextColor(ContextCompat.getColor(context, R.color.warning_300))
                                updateTransactionStatus("canceled", transaction.id)

                            }
                        } catch (e: Exception) {
                            tvTimerHours.text = "00"
                            tvTimerMinutes.text = "00"
                            tvTimerSeconds.text = "00"
                        }
                    } else if (transaction.status == "waiting_for_confirmation") {
                        layoutTimer.visibility = View.GONE
                        payButton.apply {
                            isEnabled = false // Nonaktifkan tombol
                            backgroundTintList = ContextCompat.getColorStateList(context, R.color.grey) // Ubah warna menjadi abu-abu
                        }
                        orderStatus.visibility = View.VISIBLE
                    }

                    // Setup nested RecyclerView
                    val productItemAdapter = ProductItemAdapter(context)
                    productItemAdapter.updateData(transaction.items) // List produk dari transaksi
                    productItemRecycler.adapter = productItemAdapter
                    productItemRecycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)


                    payButton.setOnClickListener {
                        val intent = Intent(context, PaymentActivity::class.java).apply {
                            putExtra("isNewTransaction", false)
                            putExtra("transactionId", transaction.id)
                        }
                        context.startActivity(intent)
                    }
                }

            }
            is SingleViewOrderCurrentBinding -> {
                holder.binding.apply {
                    totalAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"
                    totalProduct.text = "Total ${transaction.items.size} produk:"

                    if (transaction.status == "processed") {
                        orderStatus.text = "Sedang \nDiproses"
                        finishOrder.isEnabled = false
                        finishOrder.backgroundTintList = ContextCompat.getColorStateList(context, R.color.grey)
                    } else if(transaction.status == "on_shipment") {
                        orderStatus.text = "Dalam \nPengiriman"
                        finishOrder.isEnabled = true
                        finishOrder.backgroundTintList = ContextCompat.getColorStateList(context, R.color.secondary_500)
                    }

                    // Setup nested RecyclerView
                    val productItemAdapter = ProductItemAdapter(context)
                    productItemAdapter.updateData(transaction.items) // List produk dari transaksi
                    productItemRecycler.adapter = productItemAdapter
                    productItemRecycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)

                    finishOrder.setOnClickListener {
                        showConfirmDialog(
                            onConfirm = {
                                updateTransactionStatus("completed", transaction.id)
                            },
                            onCancel = {

                            }
                        )
                    }
                }
            }
            is SingleViewOrderFinishBinding -> {
                holder.binding.apply {
                    totalAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"
                    totalProduct.text = "Total ${transaction.items.size} produk:"

                    // Setup nested RecyclerView
                    val productItemAdapter = ProductItemAdapter(context)
                    productItemAdapter.updateData(transaction.items) // List produk dari transaksi
                    productItemRecycler.adapter = productItemAdapter
                    productItemRecycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)

                    addReview.setOnClickListener {
                        // Inflate layout dialog_review.xml menggunakan binding
                        val dialogBinding = DialogReviewBinding.inflate(LayoutInflater.from(holder.itemView.context))

                        // Buat AlertDialog dengan custom layout
                        val dialog = AlertDialog.Builder(holder.itemView.context)
                            .setView(dialogBinding.root)
                            .create()

                        // Interaksi dengan elemen di dialog menggunakan binding
                        dialogBinding.apply {
                            btnSubmit.setOnClickListener {
                                val rating = ratingBar.rating
                                val review = etReview.text.toString()

                                if(rating != 0f) {
                                    // Tutup dialog
                                    dialog.dismiss()
                                }
                            }

                            btnClose.setOnClickListener {
                                dialog.dismiss()
                            }
                        }

                        // Tampilkan dialog
                        dialog.show()
                    }

                }
            }
            is SingleViewOrderCancelledBinding -> {
                holder.binding.apply {
                    totalAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"
                    totalProduct.text = "Total ${transaction.items.size} produk:"

                    // Setup nested RecyclerView
                    val productItemAdapter = ProductItemAdapter(context)
                    productItemAdapter.updateData(transaction.items) // List produk dari transaksi
                    productItemRecycler.adapter = productItemAdapter
                    productItemRecycler.layoutManager = LinearLayoutManager(root.context, RecyclerView.VERTICAL, false)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder class to bind the view
    class ViewHolder(val binding: androidx.viewbinding.ViewBinding) : RecyclerView.ViewHolder(binding.root)

    private fun updateTransactionStatus(status: String, transactionId: Int) {
        transactionService.updateTransactionStatus(
            context,
            transactionId,
            status = status,
            onSuccess = {},
            onError = {}
        )
    }

    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }

    private fun showConfirmDialog(
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        // Inflate layout menggunakan View Binding
        val dialogBinding = DialogFinishOrderBinding.inflate(LayoutInflater.from(context))

        // Buat dialog
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)

        // Aksi untuk tombol Cancel
        dialogBinding.cancelButton.setOnClickListener {
            onCancel()
            dialog.dismiss()
        }

        // Aksi untuk tombol Confirm
        dialogBinding.confirmButton.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        // Tampilkan dialog
        dialog.show()
    }
}
