package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.AddressAdapter
import com.godongijo.ecotainment.databinding.ActivityAddressBinding
import com.godongijo.ecotainment.services.auth.AuthService

class AddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddressBinding

    private lateinit var addressAdapter: AddressAdapter

    private val authService = AuthService()

    private var fromCheckout: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()

    }

    override fun onResume() {
        super.onResume()
        init()
    }

    private fun init() {
        initAddress()
    }

    private fun initAddress() {
        fromCheckout = intent.getBooleanExtra("fromCheckout", false)
        Log.d("AddressActivity", fromCheckout.toString())

        binding.addressRecycler.layoutManager = LinearLayoutManager(this)

        addressAdapter = AddressAdapter(
            context = this,
            addressList = emptyList(),
            fromCheckout = fromCheckout,
        ) { selectedAddress ->
            if (fromCheckout) {
                // Kirim hasil kembali ke CheckoutActivity
                val resultIntent = Intent().apply {
                    putExtra("selectedAddress", selectedAddress)
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Tutup AddressActivity
            }
        }

        authService.getUserAddress(
            this,
            onResult = { addressList ->
                // Update UI based on whether there are items in the list
                if (addressList.isEmpty()) {
                    binding.addressRecycler.visibility = View.GONE
                    binding.emptyAddress.visibility = View.VISIBLE
                } else {
                    binding.addressRecycler.visibility = View.VISIBLE
                    binding.emptyAddress.visibility = View.GONE

                    binding.addressRecycler.adapter = addressAdapter
                    addressAdapter.updateList(addressList)
                }
            },
            onError = {
                binding.addressRecycler.visibility = View.GONE
                binding.emptyAddress.visibility = View.VISIBLE
            }
        )
    }

    private fun setListeners() {
        binding.addAddress.setOnClickListener {
            startActivity(Intent(this, DetailAddressActivity::class.java))
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}