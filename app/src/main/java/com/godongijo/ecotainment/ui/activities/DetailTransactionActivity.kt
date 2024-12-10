package com.godongijo.ecotainment.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.ProductItemAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.ActivityDetailTransactionBinding
import com.godongijo.ecotainment.databinding.DialogConfirmTransactionStatusBinding
import com.godongijo.ecotainment.databinding.PopupPaymentProofBinding
import com.godongijo.ecotainment.models.SkeletonLayoutType
import com.godongijo.ecotainment.services.transaction.TransactionService
import com.godongijo.ecotainment.utilities.DialogLoader
import com.godongijo.ecotainment.utilities.Glide
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class DetailTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTransactionBinding

    private val transactionService = TransactionService()

    private var selectedTransactionId = 0

    private var paymentProofUrl = ""

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
        initTransactionInfo()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonConfirmPayment.setOnClickListener {
            showConfirmDialog(
                type = "payment",
                onConfirm = {
                    updateTransactionStatus("processed")
                },
                onCancel = {

                }
            )
        }

        binding.buttonConfirmShipping.setOnClickListener {
            showConfirmDialog(
                type = "shipping",
                onConfirm = {
                    updateTransactionStatus("on_shipment")
                },
                onCancel = {

                }
            )
        }

        binding.paymentProof.setOnClickListener {
            if (paymentProofUrl.isNotEmpty()) {
                showPaymentProofPopup(paymentProofUrl)
            } else {
                Toast.makeText(this, "No image available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initTransactionInfo() {
        val transactionId = intent.getIntExtra("transactionId", 0)

        // Referensi ke Views
        val actualViews = listOf(
            binding.transactionId,
            binding.transactionDate,
            binding.recipientName,
            binding.phoneNumber,
            binding.address,
            binding.totalAmount,
            binding.paymentProof
        )

        val shimmerViews = listOf(
            binding.shimmerTransactionId,
            binding.shimmerTransactionDate,
            binding.shimmerBadge,
            binding.shimmerRecipientName,
            binding.shimmerPhoneNumber,
            binding.shimmerAddress,
            binding.shimmerTotalAmount,
            binding.shimmerPaymentProof
        )

        // Hide Actual Layout & Show Shimmer Layout
        setViewsVisibility(actualViews, View.GONE)
        setViewsVisibility(shimmerViews, View.VISIBLE)


        if(transactionId > 0) {
            transactionService.getAllTransactionList(
                context = this,
                onResult = { transactionList ->
                    val selectedTransaction = transactionList.find { it.id == transactionId }
                    if (selectedTransaction != null) {
                        selectedTransactionId = selectedTransaction.id

                        paymentProofUrl = ContextCompat.getString(this, R.string.base_url) + selectedTransaction.paymentProof

                        binding.transactionId.text = selectedTransaction.id.toString()
                        binding.transactionDate.text = formatDateTime(selectedTransaction.createdAt)

                        // Reset semua badge menjadi GONE
                        val badges = listOf(
                            binding.badgeWaitingConfirmation,
                            binding.badgeOnProcessed,
                            binding.badgeOnShipment,
                            binding.badgeCompleted,
                            binding.badgeCanceled
                        )
                        badges.forEach { it.visibility = View.GONE }

                        // Set badge yang sesuai menjadi VISIBLE
                        when (selectedTransaction.status) {
                            "waiting_for_confirmation" -> binding.badgeWaitingConfirmation.visibility = View.VISIBLE
                            "processed" -> binding.badgeOnProcessed.visibility = View.VISIBLE
                            "on_shipment" -> binding.badgeOnShipment.visibility = View.VISIBLE
                            "completed" -> binding.badgeCompleted.visibility = View.VISIBLE
                            "reviewed" -> binding.badgeCompleted.visibility = View.VISIBLE
                            "canceled" -> binding.badgeCanceled.visibility = View.VISIBLE
                        }

                        binding.recipientName.text = selectedTransaction.recipientName
                        binding.phoneNumber.text = selectedTransaction.phoneNumber
                        binding.address.text = selectedTransaction.address

                        binding.transactionitemRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

                        val skeletonAdapter = SkeletonAdapter(1, SkeletonLayoutType.PRODUCT)
                        binding.transactionitemRecycler.adapter = skeletonAdapter

                        // Setup Transaction Item
                        val productItemAdapter = ProductItemAdapter(this)
                        productItemAdapter.updateData(selectedTransaction.items) // List produk dari transaksi

                        binding.transactionitemRecycler.adapter = productItemAdapter

                        val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(selectedTransaction.totalAmount) // Format ke format ribuan
                        binding.totalAmount.text = "Rp${formattedPrice}"


                        // Tentukan apakah tombol Payment dan Shipping harus diaktifkan
                        val isPaymentEnabled = selectedTransaction.status == "waiting_for_confirmation"
                        val isShippingEnabled = selectedTransaction.status == "processed"

                        // Atur warna untuk tombol Payment dan Shipping
                        val paymentColorResId = if (isPaymentEnabled) R.color.primary_500 else R.color.grey
                        val shippingColorResId = if (isShippingEnabled) R.color.primary_500 else R.color.grey

                        // Konfigurasi tombol Payment
                        binding.buttonConfirmPayment.isEnabled = isPaymentEnabled
                        binding.buttonConfirmPayment.backgroundTintList = ContextCompat.getColorStateList(this, paymentColorResId)
                        binding.textButtonConfirmPayment.isEnabled = isPaymentEnabled
                        binding.progressBarConfirmPayment.isEnabled = isPaymentEnabled

                        // Konfigurasi tombol Shipping
                        binding.buttonConfirmShipping.isEnabled = isShippingEnabled
                        binding.buttonConfirmShipping.backgroundTintList = ContextCompat.getColorStateList(this, shippingColorResId)
                        binding.textButtonConfirmShipping.isEnabled = isShippingEnabled
                        binding.progressBarConfirmShipping.isEnabled = isShippingEnabled

                        // Hide Shimmer Layout & Show Actual Layout
                        setViewsVisibility(actualViews, View.VISIBLE)
                        setViewsVisibility(shimmerViews, View.GONE)
                    } else {
                        // Tampilkan pesan jika transaksi tidak ditemukan
                        Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = {

                }
            )
        }
    }

    private fun setViewsVisibility(views: List<View>, visibility: Int) {
        views.forEach { it.visibility = visibility }
    }

    private fun formatDateTime(isoDate: String): String {
        return try {
            // Format asli dari ISO 8601
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            originalFormat.timeZone = java.util.TimeZone.getTimeZone("UTC") // ISO format biasanya di UTC

            // Format tujuan
            val targetFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

            // Parse dan format ulang
            val date = originalFormat.parse(isoDate)
            targetFormat.format(date ?: "")
        } catch (e: Exception) {
            "Invalid date"
        }
    }

    private fun updateTransactionStatus(status: String) {
        if(selectedTransactionId > 0) {
            dialog = DialogLoader.show(context = this, message = "Mohon tunggu, proses sedang berjalan...") ?: return

            transactionService.adminUpdateTransactionStatus(
                this,
                selectedTransactionId,
                status = status,
                onSuccess = {
                    if (!isFinishing && !isDestroyed) {
                        dialog?.let {
                            DialogLoader.success(it, context = this, message = "Berhasil Mengubah Status")
                            initTransactionInfo()
                        }
                    }
                },
                onError = {
                    if (!isFinishing && !isDestroyed) {
                        dialog?.let {
                            DialogLoader.error(it, context = this, message = "Gagal Mengubah Status")
                        }
                    }
                }
            )
        } else {
            Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

    }

    private fun showConfirmDialog(
        type: String,
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        // Inflate layout menggunakan View Binding
        val dialogBinding = DialogConfirmTransactionStatusBinding.inflate(LayoutInflater.from(this))

        // Buat dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)

        if(type == "payment") {
            dialogBinding.confirmationTitle.text = "Konfirmasi Pembayaran"
            dialogBinding.confirmationMessage.text = "Apakah yakin ingin mengonfirmasi pembayaran? Pastikan pembayaran sudah benar"
        } else {
            dialogBinding.confirmationTitle.text = "Konfirmasi Pengiriman"
            dialogBinding.confirmationMessage.text = "Apakah yakin ingin mengonfirmasi pengiriman? Pastikan barang sudah dikirim"
        }

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


    private fun showPaymentProofPopup(imageUrl: String) {
        // Inflate layout menggunakan View Binding
        val dialogBinding = PopupPaymentProofBinding.inflate(LayoutInflater.from(this))

        // Buat dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)

        com.bumptech.glide.Glide.with(this)
            .load(imageUrl)
            .into(dialogBinding.imagePaymentProof)

        // Aksi untuk tombol Cancel
        dialogBinding.closeButton.setOnClickListener {
            dialog.dismiss()
        }

        // Tampilkan dialog
        dialog.show()
    }
}