package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.CheckoutAdapter
import com.godongijo.ecotainment.databinding.ActivityCheckoutBinding
import com.godongijo.ecotainment.services.cart.CartService
import java.text.NumberFormat
import java.util.Locale

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding

    private val cartService = CartService()

    private lateinit var checkoutAdapter: CheckoutAdapter
    private lateinit var formattedSubtotal: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()

    }

    private fun init() {
        initCheckoutList()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.payNowButton.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java).apply {
                putExtra("totalAmount", formattedSubtotal)
            }
            startActivity(intent)
        }

    }

    private fun initCheckoutList() {
        binding.checkoutRecyclerView.layoutManager = LinearLayoutManager(this)
        checkoutAdapter = CheckoutAdapter(
            this,
            mutableListOf(),
            cartService
        )
        binding.checkoutRecyclerView.adapter = checkoutAdapter

        val selectedProductIds = intent.getStringArrayListExtra("selectedProductIds")

        if (selectedProductIds != null) {
            cartService.getCartListById(
                context = this,
                productIds = selectedProductIds.map { it.toInt() }, // Konversi String ke Int
                onResult = { cartList ->
                    if (cartList.isEmpty()) {
                        binding.checkoutRecyclerView.visibility = View.GONE
                    } else {
                        binding.checkoutRecyclerView.visibility = View.VISIBLE

                        checkoutAdapter.updateList(cartList)

                        // Hitung subtotal dan total amount
                        val subtotal = cartList.sumOf { cartItem ->
                            val price = cartItem.product?.price?.toLongOrNull() ?: 0L
                            price * cartItem.quantity
                        }

                        // Format subtotal dan total amount sebagai mata uang
                        formattedSubtotal = formatCurrency(subtotal)

                        // Isi nilai subtotal dan total amount ke binding
                        binding.subtotal.text = "Rp$formattedSubtotal"
                        binding.totalSummary.text = "Rp$formattedSubtotal"
                        binding.finalAmount.text = "Rp$formattedSubtotal"
                    }
                },
                onError = {
                    // Tangani error
                }
            )
        }

    }

    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }


}