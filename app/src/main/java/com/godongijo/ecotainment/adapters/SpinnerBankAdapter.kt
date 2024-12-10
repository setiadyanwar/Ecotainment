package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.godongijo.ecotainment.databinding.SingleViewSpinnerBankBinding

class SpinnerBankAdapter(
    private val context: Context,
    private val bankList: List<Pair<String, Int>>
) : BaseAdapter() {

    override fun getCount(): Int = bankList.size

    override fun getItem(position: Int): Pair<String, Int> = bankList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: SingleViewSpinnerBankBinding = if (convertView == null) {
            SingleViewSpinnerBankBinding.inflate(LayoutInflater.from(context), parent, false)
        } else {
            SingleViewSpinnerBankBinding.bind(convertView)
        }

        val (bankName, bankLogoResId) = getItem(position)
        binding.apply {
            logo.setImageResource(bankLogoResId)
            name.text = bankName
        }
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView, parent)
    }
}
