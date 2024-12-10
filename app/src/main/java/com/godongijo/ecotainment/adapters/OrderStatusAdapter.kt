package com.godongijo.ecotainment.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
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
import com.godongijo.ecotainment.services.product.ReviewService
import com.godongijo.ecotainment.services.transaction.TransactionService
import com.godongijo.ecotainment.ui.activities.PaymentActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class OrderStatusAdapter(
    private var context: Context,
    private val transactionService: TransactionService,
    private val onFinishAction: (tabTitle: String) -> Unit
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
            "reviewed" -> VIEW_TYPE_FINISHED
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
                                        updateTransactionStatus(
                                            "canceled",
                                            transaction.id,
                                            onSuccess = {},
                                            onError = {}
                                        )
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
                                updateTransactionStatus(
                                    "canceled",
                                    transaction.id,
                                    onSuccess = {},
                                    onError = {}
                                )
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
                        // Inflate layout dialog_review.xml menggunakan binding
                        val dialogBinding = DialogFinishOrderBinding.inflate(LayoutInflater.from(holder.itemView.context))

                        // Buat AlertDialog dengan custom layout
                        val dialog = AlertDialog.Builder(holder.itemView.context)
                            .setView(dialogBinding.root)
                            .create()

                        // Interaksi dengan elemen di dialog menggunakan binding
                        dialogBinding.apply {

                            confirmButton.setOnClickListener {
                                updateTransactionStatus(
                                    "completed",
                                    transaction.id,
                                    onSuccess = {
                                        dialog.dismiss()
                                        onFinishAction("Saat Ini")
                                    },
                                    onError = {}
                                )

                            }

                            cancelButton.setOnClickListener {
                                dialog.dismiss()
                            }
                        }

                        // Tampilkan dialog
                        dialog.show()
                    }
                }
            }
            is SingleViewOrderFinishBinding -> {
                holder.binding.apply {
                    totalAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"
                    totalProduct.text = "Total ${transaction.items.size} produk:"

                    if (transaction.status == "completed") {
                        orderStatus.text = "Selesai"
                        addReview.isEnabled = true
                        addReview.backgroundTintList = ContextCompat.getColorStateList(context, R.color.secondary_500)
                    } else if(transaction.status == "reviewed") {
                        orderStatus.text = "Telah \nDiulas"
                        addReview.isEnabled = false
                        addReview.backgroundTintList = ContextCompat.getColorStateList(context, R.color.grey)
                    }

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

                        dialog.setOnShowListener {
                            dialog.window!!
                                .clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                        }

                        // Interaksi dengan elemen di dialog menggunakan binding
                        dialogBinding.apply {

                            val reviewProductAdapter = ReviewProductAdapter(context, transaction.items)
                            reviewItemRecycler.layoutManager = LinearLayoutManager(holder.itemView.context)
                            reviewItemRecycler.adapter = reviewProductAdapter

                            btnSubmitReview.setOnClickListener {
                                val reviewService = ReviewService()
                                // Dapatkan semua review dari adapter
                                val allReviews = reviewProductAdapter.getAllReviews()

                                // Filter data dengan rating > 0 (required) dan comment opsional
                                val reviewsForApi = allReviews.map { (productId, reviewData) ->
                                    mapOf(
                                        "productId" to productId,  // productId sekarang bertipe Int
                                        "rating" to reviewData.first,  // reviewData.first bertipe Int
                                        "comment" to (reviewData.second.takeIf { it.isNotEmpty() }
                                            ?: "")  // Jika komentar kosong atau null, gunakan string kosong
                                    )
                                }.filter { it["rating"] as Int > 0 }

                                if (reviewsForApi.isNotEmpty()) {
                                    // Kirim data ke API menggunakan Service
                                    reviewService.addProductReviews(
                                        context,
                                        reviewsForApi,
                                        onSuccess = {
                                            updateTransactionStatus(
                                                "reviewed",
                                                transaction.id,
                                                onSuccess = {
                                                    dialog.dismiss()
                                                    onFinishAction("Selesai")
                                                },
                                                onError = {}
                                            )

                                        }, onError = { errorMessage ->
                                            // Tampilkan pesan error jika diperlukan
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    Toast.makeText(context, "Please provide at least one rating", Toast.LENGTH_SHORT).show()
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

    private fun updateTransactionStatus(
        status: String,
        transactionId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        transactionService.updateTransactionStatus(
            context,
            transactionId,
            status = status,
            onSuccess = {
                // Logika tambahan ketika berhasil
                onSuccess() // Memanggil callback sukses
            },
            onError = { error ->
                // Logika tambahan ketika gagal
                onError(error) // Memanggil callback error dengan pesan error
            }
        )
    }


    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }
}
