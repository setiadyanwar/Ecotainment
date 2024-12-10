package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.CartAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.ActivityCartBinding
import com.godongijo.ecotainment.models.CartItem
import com.godongijo.ecotainment.models.SkeletonLayoutType
import com.godongijo.ecotainment.services.cart.CartService
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity(), CartAdapter.OnCartInteractionListener {

    private lateinit var binding: ActivityCartBinding
    private val cartService = CartService()
    private lateinit var cartAdapter: CartAdapter
    private var selectedItems = listOf<CartItem>() // Menyimpan item yang dipilih

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
        binding.layoutEmptyCart.visibility = View.GONE
        binding.layoutCartContent.visibility = View.GONE
        binding.layoutSelectAll.visibility = View.GONE
        binding.layoutBottomPanel.visibility = View.GONE

        initCartList()
    }

    private fun setListeners() {
        binding.checkoutButton.setOnClickListener {
            val selectedProductIds = selectedItems.map { it.productId.toString() }

            if (selectedProductIds.isNotEmpty()) {
                val intent = Intent(this, CheckoutActivity::class.java).apply {
                    putExtra("sourceActivity", "CartActivity")
                    putStringArrayListExtra("selectedProductIds", ArrayList(selectedProductIds))
                }
                startActivity(intent)
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.notificationButton.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.selectAll.setOnClickListener {
            if (cartAdapter.areAllItemsSelected()) {
                // Jika semua item sudah dipilih, hapus semua seleksi
                cartAdapter.unselectAllItems()
            } else {
                // Jika belum, pilih semua item
                cartAdapter.selectAllItems()
            }

            // Perbarui list item yang dipilih di CartActivity
            selectedItems = cartAdapter.getSelectedItems()
            updateTotalPrice() // Hitung ulang harga total berdasarkan seleksi
        }

    }

    private fun initCartList() {
        binding.layoutCartContent.visibility = View.VISIBLE

        binding.cartItemRecycler.layoutManager = LinearLayoutManager(this)
        cartAdapter = CartAdapter(
            this,
            mutableListOf(),
            cartService,
            this,
            onQuantityChanged = {
                updateTotalPrice()  // Memperbarui total harga setelah kuantitas berubah
            }
        )

        val skeletonAdapter = SkeletonAdapter(5, SkeletonLayoutType.CART)
        binding.cartItemRecycler.adapter = skeletonAdapter

        cartService.getCartList(
            context = this,
            onResult = { cartList ->
                if (cartList.isEmpty()) {
                    binding.cartItemRecycler.visibility = View.GONE

                    binding.layoutEmptyCart.visibility = View.VISIBLE
                    binding.layoutCartContent.visibility = View.GONE
                    binding.layoutSelectAll.visibility = View.GONE
                    binding.layoutBottomPanel.visibility = View.GONE
                } else {
                    binding.cartCounts.text = "${cartList.size} items"

                    binding.cartItemRecycler.visibility = View.VISIBLE

                    binding.layoutEmptyCart.visibility = View.GONE
                    binding.layoutCartContent.visibility = View.VISIBLE
                    binding.layoutSelectAll.visibility = View.VISIBLE
                    binding.layoutBottomPanel.visibility = View.VISIBLE

                    binding.cartItemRecycler.adapter = cartAdapter
                    cartAdapter.updateList(cartList)
                    updateTotalPrice()
                }
            },
            onError = {
                binding.cartItemRecycler.visibility = View.GONE

                binding.layoutEmptyCart.visibility = View.VISIBLE
                binding.layoutCartContent.visibility = View.GONE
                binding.layoutSelectAll.visibility = View.GONE
                binding.layoutBottomPanel.visibility = View.GONE
            }
        )
    }

    override fun onSelectionChanged(selectedItems: List<CartItem>) {
        this.selectedItems = selectedItems
        updateTotalPrice()  // Memperbarui harga total ketika seleksi berubah

        // Perbarui jumlah item yang dipilih
        val selectedCount = selectedItems.size
        binding.checkoutQuantity.text = "(${selectedCount.toString()})"
    }

    private fun updateTotalPrice() {
        var totalPrice = 0L

        // Menghitung total harga produk yang dipilih
        val selectedItemsList = cartAdapter.getSelectedItems()
        if (selectedItemsList.isNotEmpty()) {
            totalPrice = selectedItemsList.sumOf { cartItem ->
                val price = cartItem.product?.price?: 0
                price * cartItem.quantity  // Menghitung total harga berdasarkan kuantitas dan harga produk
            }.toLong()
        }

        val formattedTotalPrice = NumberFormat.getInstance(Locale("id", "ID")).format(totalPrice)
        binding.totalPrice.text = "Rp$formattedTotalPrice"  // Menampilkan total harga yang sudah dihitung

        // Perbarui jumlah item yang dipilih
        val selectedCount = selectedItems.size
        binding.checkoutQuantity.text = "(${selectedCount.toString()})"
    }

}