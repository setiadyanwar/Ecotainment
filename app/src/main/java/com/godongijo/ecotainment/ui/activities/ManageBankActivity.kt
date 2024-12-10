package com.godongijo.ecotainment.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.ManageBankAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.ActivityManageBankBinding
import com.godongijo.ecotainment.databinding.DialogConfirmDeleteBinding
import com.godongijo.ecotainment.models.SkeletonLayoutType
import com.godongijo.ecotainment.services.bank.BankService

class ManageBankActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageBankBinding

    private lateinit var manageBankAdapter: ManageBankAdapter

    private val bankService = BankService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageBankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        initBankList()
    }

    private fun init() {
        initBankList()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addNewBank.setOnClickListener {
            val intent = Intent(this, FormBankActivity::class.java).apply {
                putExtra("isEditing", false)
            }
            startActivity(intent)
        }
    }

    private fun initBankList() {
        binding.manageBankRecycler.layoutManager = LinearLayoutManager(this)
        manageBankAdapter = ManageBankAdapter(
            context = this,
            bankList = emptyList(),
            onEditClick = { bank ->
                // Logika untuk tombol edit
                editBank(bank.id)
            },
            onDeleteClick = { bank ->
                // Logika untuk tombol delete
                deleteBank(bank.id)
            }
        )

        val skeletonAdapter = SkeletonAdapter(10, SkeletonLayoutType.MANAGE_BANK)
        binding.manageBankRecycler.adapter = skeletonAdapter

        bankService.getBankList(
            context = this,
            onResult = { bankList ->
                // Update UI based on whether there are items in the list
                if (bankList.isEmpty()) {
                    binding.manageBankRecycler.visibility = View.GONE
                    binding.emptyBank.visibility = View.VISIBLE
                } else {
                    binding.manageBankRecycler.visibility = View.VISIBLE
                    binding.emptyBank.visibility = View.GONE

                    binding.manageBankRecycler.adapter = manageBankAdapter
                    manageBankAdapter.updateList(bankList)
                }
            },
            onError = {
                binding.manageBankRecycler.visibility = View.GONE
                binding.emptyBank.visibility = View.VISIBLE
            }
        )
    }



    private fun editBank(bankId: Int) {
        val intent = Intent(this, FormBankActivity::class.java).apply {
            putExtra("isEditing", true)
            putExtra("bankId", bankId)
        }
        startActivity(intent)
    }

    private fun deleteBank(bankId: Int) {
        showConfirmDialog(
            onConfirm = {
                bankService.deleteBank(
                    context = this,
                    bankId = bankId,
                    onResult = {
                        initBankList()
                    },
                    onError = {
                        Log.d("Manage Bank", "Failed delete Bank")
                    }
                )
            },
            onCancel = {

            }
        )
    }


    private fun showConfirmDialog(
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        // Inflate layout menggunakan View Binding
        val dialogBinding = DialogConfirmDeleteBinding.inflate(LayoutInflater.from(this))

        // Buat dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.message.text = "Apakah Kamu yakin mau hapus bank ini?"

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