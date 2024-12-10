package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.SingleViewManageBankBinding
import com.godongijo.ecotainment.models.Bank
import com.godongijo.ecotainment.utilities.Glide

class ManageBankAdapter(
    private var context: Context,
    private var bankList: List<Bank>,
    private val onEditClick: (Bank) -> Unit, // Callback untuk tombol edit
    private val onDeleteClick: (Bank) -> Unit // Callback untuk tombol delete
) : RecyclerView.Adapter<ManageBankAdapter.ManageBankViewHolder>() {

    // ViewHolder with binding
    inner class ManageBankViewHolder(val binding: SingleViewManageBankBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ManageBankViewHolder {
        val binding = SingleViewManageBankBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ManageBankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ManageBankViewHolder, position: Int) {
        val bank = bankList[position]

        holder.binding.apply {
            if (bank.name == "QRIS") {
                bankLogo.setImageResource(R.drawable.ic_bankmaster_qris)
            } else {
                val logoImage = context.getString(R.string.base_url) + bank.logo
                Glide().loadImageFitCenter(bankLogo, logoImage)
            }

            accountNumber.text = bank.accountNumber
            accountHolder.text = bank.accountHolder

            // Listener untuk tombol edit
            editBank.setOnClickListener {
                onEditClick(bank)
            }

            // Listener untuk tombol delete
            deleteBank.setOnClickListener {
                onDeleteClick(bank)
            }
        }
    }

    override fun getItemCount(): Int = bankList.size

    fun updateList(newBankList: List<Bank>) {
        bankList = newBankList
        notifyDataSetChanged()
    }
}
