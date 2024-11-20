package com.godongijo.ecotainment.adapters

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.databinding.SingleViewBankBinding
import com.godongijo.ecotainment.models.Bank

class BankAdapter : RecyclerView.Adapter<BankAdapter.BankViewHolder>() {

    private var banks = listOf<Bank>()
    private var expandedPosition = -1

    fun setData(newBanks: List<Bank>) {
        banks = newBanks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankViewHolder {
        val binding = SingleViewBankBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BankViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BankViewHolder, position: Int) {
        holder.bind(banks[position], position == expandedPosition)
    }

    override fun getItemCount() = banks.size

    inner class BankViewHolder(
        private val binding: SingleViewBankBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                expandedPosition = if (expandedPosition == position) -1 else position
                notifyDataSetChanged()
            }

            binding.btnCopy.setOnClickListener {
                val bank = banks[adapterPosition]
                val clipboardManager = itemView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("account_number", bank.bankAccount.accountNumber)
                clipboardManager.setPrimaryClip(clipData)
                Toast.makeText(itemView.context, "Nomor rekening berhasil disalin", Toast.LENGTH_SHORT).show()
            }
        }

        fun bind(bank: Bank, isExpanded: Boolean) {
            binding.apply {
                ivBankLogo.setImageResource(bank.logo)
                ivChevron.rotation = if (isExpanded) 180f else 0f
                layoutContent.visibility = if (isExpanded) View.VISIBLE else View.GONE

                if (isExpanded) {
                    tvAccountNumber.text = bank.bankAccount.accountNumber
                    tvAccountHolder.text = "a.n ${bank.bankAccount.accountHolder}"

                    layoutInstructions.removeAllViews()
                    bank.bankAccount.paymentInstructions.forEachIndexed { index, instruction ->
                        val instructionView = TextView(itemView.context).apply {
                            text = "${index + 1}. $instruction"
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = 8
                            }
                        }
                        layoutInstructions.addView(instructionView)
                    }
                }
            }
        }
    }
}