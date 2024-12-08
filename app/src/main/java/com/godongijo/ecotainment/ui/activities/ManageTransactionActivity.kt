package com.godongijo.ecotainment.ui.activities

import com.godongijo.ecotainment.adapters.ManageTransactionAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.databinding.ActivityManageTransactionBinding
import com.godongijo.ecotainment.services.transaction.TransactionService

class ManageTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageTransactionBinding

    private lateinit var manageTransactionAdapter: ManageTransactionAdapter
    private val transactionService = TransactionService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        initTransactionList()
    }

    private fun init() {
        initTransactionList()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initTransactionList() {
        binding.manageTransactionRecyclerView.layoutManager = LinearLayoutManager(this)
        manageTransactionAdapter = ManageTransactionAdapter(
            context = this,
            transactionList = emptyList(),
            onCheckTransactionClick = { transaction ->
                checkTransaction(transaction.id)
            },
        )

        transactionService.getAllTransactionList(
            context = this,
            onResult = { transactionList ->
                // Filter daftar transaksi untuk menghilangkan yang berstatus "pending"
                val filteredTransactionList = transactionList.filter { it.status != "pending" }

                // Update UI berdasarkan apakah daftar yang difilter kosong atau tidak
                if (filteredTransactionList.isEmpty()) {
                    binding.manageTransactionRecyclerView.visibility = View.GONE
                    binding.emptyTransaction.visibility = View.VISIBLE
                } else {
                    binding.manageTransactionRecyclerView.visibility = View.VISIBLE
                    binding.emptyTransaction.visibility = View.GONE

                    binding.manageTransactionRecyclerView.adapter = manageTransactionAdapter
                    manageTransactionAdapter.updateList(filteredTransactionList)
                }
            },
            onError = {
                binding.manageTransactionRecyclerView.visibility = View.GONE
                binding.emptyTransaction.visibility = View.VISIBLE
            }
        )

    }

    private fun checkTransaction(transactionId: Int) {
        val intent = Intent(this, DetailTransactionActivity::class.java).apply {
            putExtra("transactionId", transactionId)
        }
        startActivity(intent)
    }


}