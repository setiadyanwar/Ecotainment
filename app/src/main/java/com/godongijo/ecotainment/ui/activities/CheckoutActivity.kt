package com.godongijo.ecotainment.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.CheckoutAdapter
import com.godongijo.ecotainment.databinding.ActivityCheckoutBinding
import com.godongijo.ecotainment.models.Address
import com.godongijo.ecotainment.models.CartItem
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.services.cart.CartService
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.utilities.PreferenceManager
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding

    private val cartService = CartService()

    private val productService = ProductService()

    private val authService = AuthService()

    private lateinit var checkoutAdapter: CheckoutAdapter
    private var formattedSubtotal: Int = 0

    private val transactionItems = mutableListOf<Pair<Int, Int>>()

    private lateinit var selectedAddress: Address

    private val ADDRESS_SELECTION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()

    }

    override fun onResume() {
        super.onResume()
        loadUserAddress()
    }

    private fun init() {
        initCheckoutList()
        loadUserAddress()
    }

    private fun loadUserAddress() {
        authService.getUserAddress(
            this,
            onResult = { addressList ->
                if (addressList.isNotEmpty()) {
                    selectedAddress = addressList[0] // Ambil alamat pertama sebagai default
                    val nameAndPhone = "${selectedAddress.recipientName} | ${selectedAddress.phoneNumber}"
                    val fullAddress = "${selectedAddress.detailAddress}, ${selectedAddress.cityOrDistrict}, ${selectedAddress.province}"

                    // Update UI
                    binding.nameAndPhone.text = nameAndPhone
                    binding.address.text = fullAddress

                    // Tampilkan view address
                    binding.addressLayout.visibility = View.VISIBLE
                    binding.addressWarning.visibility = View.VISIBLE
                    binding.changeAddress.visibility = View.VISIBLE
                    binding.addressEmpty.visibility = View.GONE
                } else {
                    // Tampilkan view empty address
                    binding.addressLayout.visibility = View.GONE
                    binding.addressWarning.visibility = View.GONE
                    binding.changeAddress.visibility = View.GONE
                    binding.addressEmpty.visibility = View.VISIBLE
                }
            },
            onError = {
                // Tampilkan view empty address jika terjadi error
                binding.addressLayout.visibility = View.GONE
                binding.addressWarning.visibility = View.GONE
                binding.changeAddress.visibility = View.GONE
                binding.addressEmpty.visibility = View.VISIBLE
            }
        )
    }


    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.payNowButton.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("isNewTransaction", true)
                putExtra("totalAmount", formattedSubtotal)
                putExtra("transactionItems", ArrayList(transactionItems))
            }
            startActivity(intent)
        }

        binding.changeAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java).apply {
                putExtra("fromCheckout", true)
            }
            startActivityForResult(intent, ADDRESS_SELECTION_REQUEST_CODE)
        }

        binding.addressEmpty.setOnClickListener {
            val intent = Intent(this, DetailAddressActivity::class.java)
            startActivity(intent)
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDRESS_SELECTION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedAddress = data?.getParcelableExtra<Address>("selectedAddress")
            selectedAddress?.let { updateAddressUI(it) }
        }
    }

    private fun updateAddressUI(address: Address) {
        selectedAddress = address
        val nameAndPhone = "${address.recipientName} | ${address.phoneNumber}"
        val fullAddress = "${address.detailAddress}, ${address.cityOrDistrict}, ${address.province}"

        binding.nameAndPhone.text = nameAndPhone
        binding.address.text = fullAddress
    }

    private fun initCheckoutList() {
        binding.checkoutRecyclerView.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutAdapter(
            this,
            mutableListOf(),
            cartService
        )
        binding.checkoutRecyclerView.adapter = checkoutAdapter

        val sourceActivity = intent.getStringExtra("sourceActivity")

        when (sourceActivity) {
            "CartActivity" -> {
                val selectedProductIds = intent.getStringArrayListExtra("selectedProductIds")
                if (selectedProductIds != null) {
                    handleCartCheckout(selectedProductIds)
                }
            }
            "DetailProductActivity" -> {
                val selectedProductId = intent.getIntExtra("selectedProductId", -1)
                val selectedQuantity = intent.getIntExtra("selectedQuantity", 1)
                if (selectedProductId != -1) {
                    handleDetailProductCheckout(selectedProductId, selectedQuantity)
                }
            }
        }
    }

    private fun handleCartCheckout(selectedProductIds: ArrayList<String>) {
        cartService.getCartListById(
            context = this,
            productIds = selectedProductIds.map { it.toInt() },
            onResult = { cartList ->
                updateCheckoutUI(cartList)
            },
            onError = {

            }
        )
    }

    private fun handleDetailProductCheckout(productId: Int, quantity: Int) {
        productService.getSingleProduct(
            context = this,
            productId = productId,
            onResult = { product ->
                val cartItem = listOf(CartItem(
                    id = 1,
                    userId = "",
                    productId = product.id,
                    quantity = quantity,
                    createdAt = "",
                    updatedAt = "",
                    product = product
                ))
                updateCheckoutUI(cartItem)
            },
            onError = { error ->
                Log.d("Error Handle", error)
            }
        )
    }

    private fun updateCheckoutUI(cartList: List<CartItem>) {
        if (cartList.isEmpty()) {
            binding.checkoutRecyclerView.visibility = View.GONE
        } else {
            binding.checkoutRecyclerView.visibility = View.VISIBLE
            checkoutAdapter.updateList(cartList)

            val subtotal = cartList.sumOf { cartItem ->
                val price = cartItem.product?.price ?: 0
                price * cartItem.quantity
            }
            formattedSubtotal = subtotal

            binding.subtotal.text = "Rp${formatCurrency(subtotal.toLong())}"
            binding.totalSummary.text = "Rp${formatCurrency(subtotal.toLong())}"
            binding.finalAmount.text = "Rp${formatCurrency(subtotal.toLong())}"

            transactionItems.clear()
            transactionItems.addAll(
                cartList.map { cartItem ->
                    Pair(cartItem.product?.id ?: 0, cartItem.quantity)
                }
            )
        }
    }


    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }


}