package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.BankAdapter
import com.godongijo.ecotainment.databinding.ActivityPaymentBinding
import com.godongijo.ecotainment.models.Bank
import com.godongijo.ecotainment.models.BankAccount

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var bankAdapter: BankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi RecyclerView dan load data bank
        setupRecyclerView()
        loadBanks()
    }

    private fun setupRecyclerView() {
        // Inisialisasi BankAdapter
        bankAdapter = BankAdapter()

        // Atur RecyclerView
        binding.rvBanks.apply {
            adapter = bankAdapter
            layoutManager = LinearLayoutManager(this@PaymentActivity)

        }
    }

    private fun loadBanks() {
        val banks = listOf(
            // Data Bank BRI
            Bank(
                id = "bri",
                name = "BANK BRI",
                logo = R.drawable.ic_bank_bri,
                bankAccount = BankAccount(
                    bankLogo = R.drawable.ic_bank_bri,
                    accountNumber = "1424566485342",
                    accountHolder = "Ecotainment",
                    paymentInstructions = listOf(
                        "Copy nomor rekening di atas.",
                        "Anda dapat pergi ke ATM BRI terdekat atau menggunakan mobile banking.",
                        "Transfer sesuai dengan tagihan.",
                        "Upload bukti transfer dengan foto/screenshot"
                    )
                )
            ),
            // Data Bank BCA
            Bank(
                id = "bca",
                name = "BCA",
                logo = R.drawable.ic_bank_bca,
                bankAccount = BankAccount(
                    bankLogo = R.drawable.ic_bank_bca,
                    accountNumber = "728890",
                    accountHolder = "Ecotainment",
                    paymentInstructions = listOf(
                        "Copy nomor rekening di atas.",
                        "Anda dapat pergi ke ATM BCA terdekat atau menggunakan mobile banking.",
                        "Transfer sesuai dengan tagihan.",
                        "Upload bukti transfer dengan foto/screenshot"
                    )
                )
            ),
            // Data Bank BNI
            Bank(
                id = "bni",
                name = "BNI",
                logo = R.drawable.ic_bank_bni,
                bankAccount = BankAccount(
                    bankLogo = R.drawable.ic_bank_bni,
                    accountNumber = "1464853342",
                    accountHolder = "Ecotainment",
                    paymentInstructions = listOf(
                        "Copy nomor rekening di atas.",
                        "Anda dapat pergi ke ATM BNI terdekat atau menggunakan mobile banking.",
                        "Transfer sesuai dengan tagihan.",
                        "Upload bukti transfer dengan foto/screenshot"
                    )
                )
            ),
            // Data Bank Mandiri
            Bank(
                id = "mandiri",
                name = "mandiri",
                logo = R.drawable.ic_bank_mandiri,
                bankAccount = BankAccount(
                    bankLogo = R.drawable.ic_bank_mandiri,
                    accountNumber = "1424566485342",
                    accountHolder = "Ecotainment",
                    paymentInstructions = listOf(
                        "Copy nomor rekening di atas.",
                        "Anda dapat pergi ke ATM Mandiri terdekat atau menggunakan mobile banking.",
                        "Transfer sesuai dengan tagihan.",
                        "Upload bukti transfer dengan foto/screenshot"
                    )
                )
            )
        )

        // Set data ke adapter
        bankAdapter.setData(banks)
    }

}
