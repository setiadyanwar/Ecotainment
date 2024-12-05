package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.databinding.SingleViewAddressBinding
import com.godongijo.ecotainment.databinding.SingleViewSelectAddressBinding
import com.godongijo.ecotainment.models.Address

class AddressAdapter(
    private var context: Context,
    private var addressList: List<Address>,
    private val fromCheckout: Boolean = false,
    private val onAddressSelected: (Address) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // ViewHolder dengan binding
    inner class AddressViewHolder(val binding: SingleViewAddressBinding) : RecyclerView.ViewHolder(binding.root)

    // ViewHolder untuk SingleViewSelectAddressBinding
    inner class SelectAddressViewHolder(val binding: SingleViewSelectAddressBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int {
        return if (fromCheckout) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> {
                val binding = SingleViewSelectAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SelectAddressViewHolder(binding)
            }
            else -> {
                val binding = SingleViewAddressBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AddressViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val address = addressList[position]

        when (holder) {
            is AddressViewHolder -> {
                // Binding untuk SingleViewAddressBinding
                holder.binding.apply {
                    recipientName.text = address.recipientName ?: "Nama Penerima Tidak Tersedia"
                    phoneNumber.text = address.phoneNumber ?: "Nomor Telepon Tidak Tersedia"

                    val textAddress = "${address.detailAddress}, ${address.cityOrDistrict}, ${address.province}"
                    detailAddress.text = textAddress
                }
            }
            is SelectAddressViewHolder -> {
                // Binding untuk SingleViewSelectAddressBinding
                holder.binding.apply {
                    recipientName.text = address.recipientName ?: "Nama Penerima Tidak Tersedia"
                    phoneNumber.text = address.phoneNumber ?: "Nomor Telepon Tidak Tersedia"

                    val textAddress = "${address.detailAddress}, ${address.cityOrDistrict}, ${address.province}"
                    detailAddress.text = textAddress

                    selectAddress.setOnClickListener {
                        onAddressSelected(address)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = addressList.size

    fun updateList(newAddressList: List<Address>) {
        addressList = newAddressList
        notifyDataSetChanged()
    }
}
