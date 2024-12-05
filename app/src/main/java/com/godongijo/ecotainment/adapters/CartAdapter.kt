package com.godongijo.ecotainment.adapters

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.DialogDeleteCartBinding
import com.godongijo.ecotainment.databinding.SingleViewCartBinding
import com.godongijo.ecotainment.models.CartItem
import com.godongijo.ecotainment.services.cart.CartService
import com.godongijo.ecotainment.utilities.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var context: Context,
    private var cartItemList: MutableList<CartItem>, // Menggunakan MutableList agar bisa diubah
    private val cartService: CartService,
    private val listener: OnCartInteractionListener,
    private val onQuantityChanged: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartItemViewHolder>() {

    private val selectedItems = mutableSetOf<Int>() // Menyimpan indeks item yang dipilih

    interface OnCartInteractionListener {
        fun onSelectionChanged(selectedItems: List<CartItem>)
    }

    inner class CartItemViewHolder(val binding: SingleViewCartBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        val binding = SingleViewCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
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
            quantity.text = currentQuantity.toString()

            // Checkbox seleksi
            checkbox.isChecked = selectedItems.contains(position)

            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedItems.add(position)
                } else {
                    selectedItems.remove(position)
                }
                listener.onSelectionChanged(getSelectedItems()) // Memperbarui list item yang dipilih
                onQuantityChanged()  // Memperbarui harga setelah perubahan seleksi
            }

            var updateJob: Job? = null

            plusQuantity.setOnClickListener {
                currentQuantity++  // Tambah kuantitas
                quantity.text = currentQuantity.toString()

                // Memperbarui kuantitas di cartItemList
                cartItemList[position].quantity = currentQuantity

                updateJob?.cancel()
                updateJob = CoroutineScope(Dispatchers.IO).launch {
                    delay(500)
                    cartService.updateQuantity(
                        context,
                        cartItem.productId,
                        currentQuantity,
                        onSuccess = {
                            Log.d("CartAdapter", "Quantity updated successfully")
                            onQuantityChanged()  // Memperbarui total harga di Activity
                        }, onError = { error ->
                            Log.e("CartAdapter", "Error updating quantity: $error")
                        }
                    )
                }
            }

            minusQuantity.setOnClickListener {
                if (currentQuantity > 1) {
                    currentQuantity--  // Kurangi kuantitas
                    quantity.text = currentQuantity.toString()

                    // Memperbarui kuantitas di cartItemList
                    cartItemList[position].quantity = currentQuantity

                    updateJob?.cancel()
                    updateJob = CoroutineScope(Dispatchers.IO).launch {
                        delay(500)
                        cartService.updateQuantity(
                            context,
                            cartItem.productId,
                            currentQuantity,
                            onSuccess = {
                                Log.d("CartAdapter", "Quantity updated successfully")
                                onQuantityChanged()  // Memperbarui total harga di Activity
                            }, onError = { error ->
                                Log.e("CartAdapter", "Error updating quantity: $error")
                            }
                        )
                    }
                }
            }

            deleteProduct.setOnClickListener{
                showConfirmDialog(
                    onConfirm = {
                        cartService.deleteCart(
                            context = context,
                            productId = cartItem.productId,
                            onSuccess = {  ->
                                (cartItemList as MutableList).removeAt(position)
                                notifyItemRemoved(position)  // Memberitahukan RecyclerView bahwa item telah dihapus

                                // Memperbarui seleksi jika item yang dihapus terpilih
                                selectedItems.remove(position)

                                // Memperbarui harga total di CartActivity setelah item dihapus
                                onQuantityChanged()
                            },
                            onError = {
                                Log.d("CartAdapter", "Failed delete product from cart")
                            }
                        )
                    },
                    onCancel = {

                    }
                )

            }
        }
    }

    override fun getItemCount(): Int = cartItemList.size

    // Memperbarui list dengan data baru
    fun updateList(newCartItemList: List<CartItem>) {
        cartItemList = newCartItemList.toMutableList() // Pastikan list bersifat Mutable
        notifyDataSetChanged()
    }

    // Mendapatkan item yang dipilih
    fun getSelectedItems(): List<CartItem> {
        return selectedItems.map { cartItemList[it] }
    }

    // Pilih semua item
    fun selectAllItems() {
        selectedItems.clear() // Bersihkan terlebih dahulu
        cartItemList.indices.forEach { index ->
            selectedItems.add(index)
        }
        notifyDataSetChanged() // Beri tahu RecyclerView untuk memperbarui tampilan
    }

    // Hapus semua seleksi
    fun unselectAllItems() {
        selectedItems.clear() // Bersihkan semua pilihan
        notifyDataSetChanged() // Beri tahu RecyclerView untuk memperbarui tampilan
    }

    // Periksa apakah semua item sudah dipilih
    fun areAllItemsSelected(): Boolean {
        return selectedItems.size == cartItemList.size
    }


    private fun showConfirmDialog(
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        // Inflate layout menggunakan View Binding
        val dialogBinding = DialogDeleteCartBinding.inflate(LayoutInflater.from(context))

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

