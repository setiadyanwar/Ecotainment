package com.godongijo.ecotainment.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityDetailAddressBinding
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.utilities.DialogLoader

class DetailAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailAddressBinding

    private val authService = AuthService()

    private var isEditing: Boolean = false

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()

    }

    private fun init() {
        isEditing = intent.getBooleanExtra("isEditing", false)

        // Muat provinsi terlebih dahulu dan panggil initAddressInfo setelah selesai
        loadProvinces {
            if (isEditing) {
                initAddressInfo()
            }
        }
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonSave.setOnClickListener {
            saveAddress()
        }

    }

    private fun initAddressInfo() {
        val addressId = intent.getIntExtra("addressId", 0)

        if (addressId != 0) {
            authService.getUserAddress(
                this,
                onResult = { addressList ->
                    val selectedAddress = addressList.find { it.id == addressId }

                    if (selectedAddress != null) {
                        binding.inputRecipientName.setText(selectedAddress.recipientName)
                        binding.inputPhoneNumber.setText(selectedAddress.phoneNumber)
                        binding.inputFullAddress.setText(selectedAddress.detailAddress)


                        // Simpan nama provinsi dan kabupaten dari selectedAddress
                        val selectedProvince = selectedAddress.province
                        val selectedCity = selectedAddress.cityOrDistrict

                        // Setelah provinsi selesai dimuat, set pilihan provinsi
                        binding.spinnerProvinsi.post {
                            val provinceIndex = (binding.spinnerProvinsi.adapter as ArrayAdapter<String>)
                                .getPosition(selectedProvince)
                            binding.spinnerProvinsi.setSelection(provinceIndex)

                            // Setelah kabupaten selesai dimuat, set pilihan kabupaten
//                            loadKabupaten(provinceIndex.toString())
                            binding.spinnerKabupatenKota.post {
                                val cityIndex = (binding.spinnerKabupatenKota.adapter as ArrayAdapter<String>)
                                    .getPosition(selectedCity)
                                binding.spinnerKabupatenKota.setSelection(cityIndex)
                            }
                        }
                    }
                },
                onError = {

                }
            )
        }

    }

    private fun saveAddress() {
        // Ambil data dari input
        val addressId = intent.getIntExtra("addressId", 0)

        val recipientName = binding.inputRecipientName.text.toString().trim()
        val phoneNumber = binding.inputPhoneNumber.text.toString().trim()
        val province = binding.spinnerProvinsi.selectedItem?.toString()
        val cityOrDistrict = binding.spinnerKabupatenKota.selectedItem?.toString()
        val fullAddress = binding.inputFullAddress.text.toString().trim()

        // Validasi data input
        when {
            recipientName.isEmpty() -> {
                binding.inputRecipientName.error = "Nama penerima tidak boleh kosong"
                binding.inputRecipientName.requestFocus()
                return
            }
            phoneNumber.isEmpty() -> {
                binding.inputPhoneNumber.error = "Nomor telepon tidak boleh kosong"
                binding.inputPhoneNumber.requestFocus()
                return
            }
            province.isNullOrEmpty() || province == "Pilih Provinsi" -> {
                showErrorMessage("Silakan pilih provinsi")
                return
            }
            cityOrDistrict.isNullOrEmpty() || cityOrDistrict == "Pilih Kabupaten/Kota" -> {
                showErrorMessage("Silakan pilih kabupaten/kota")
                return
            }
            fullAddress.isEmpty() -> {
                binding.inputFullAddress.error = "Alamat lengkap tidak boleh kosong"
                binding.inputFullAddress.requestFocus()
                return
            }
            else -> {
                dialog = DialogLoader.show(context = this, message = "Mohon tunggu, proses sedang berjalan...") ?: return

                if(isEditing) {
                    authService.editAddress(
                        this,
                        addressId,
                        recipientName,
                        phoneNumber,
                        province,
                        cityOrDistrict,
                        fullAddress,
                        onSuccess = {
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.success(it, context = this, message = "Berhasil Memperbarui Alamat")

                                    binding.root.postDelayed({
                                        finish()
                                    }, 1500L)
                                }
                            }

                        },
                        onError = {
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.error(it, context = this, message = "Gagal Memperbarui Alamat")

                                    binding.root.postDelayed({
                                        finish()
                                    }, 1500L)
                                }
                            }
                        }
                    )
                } else {
                    authService.addAddress(
                        this,
                        recipientName,
                        phoneNumber,
                        province,
                        cityOrDistrict,
                        fullAddress,
                        onSuccess = {
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.success(it, context = this, message = "Berhasil Menyimpan Alamat")

                                    binding.root.postDelayed({
                                        finish()
                                    }, 1500L)
                                }
                            }
                        },
                        onError = {
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.error(it, context = this, message = "Gagal Menyimpan Alamat")

                                    binding.root.postDelayed({
                                        finish()
                                    }, 1500L)
                                }
                            }
                        }
                    )
                }

            }
        }
    }

    // Fungsi untuk menampilkan pesan error
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // Fungsi untuk load provinsi
    private fun loadProvinces(onProvincesLoaded: () -> Unit) {
        val queue = Volley.newRequestQueue(this)

        val url = "https://www.emsifa.com/api-wilayah-indonesia/api/provinces.json"
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->

            binding.buttonSave.isEnabled = true
            binding.buttonSave.backgroundTintList = ContextCompat.getColorStateList(this, R.color.secondary_500)

            val provinces = mutableListOf("Pilih Provinsi")
            val provinceMap = mutableMapOf<String, String>() // Menyimpan id dan nama provinsi

            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)
                val id = item.getString("id")
                val name = item.getString("name")
                provinces.add(name)
                provinceMap[name] = id
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, provinces)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerProvinsi.adapter = adapter

            binding.spinnerProvinsi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) { // Abaikan item dummy
                        val selectedProvince = provinces[position]
                        val provinceId = provinceMap[selectedProvince]
                        loadKabupaten(provinceId)
                    } else {
                        // Reset spinner berikutnya jika user memilih item dummy
                        resetSpinner(binding.spinnerKabupatenKota)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle case where nothing is selected (optional)
                }
            }

            // Callback untuk memberi tahu bahwa provinsi sudah dimuat
            onProvincesLoaded()

        }, {
            // Handle error
        })
        queue.add(request)
    }

    // Fungsi untuk load kabupaten berdasarkan ID provinsi
    fun loadKabupaten(provinceId: String?) {
        val queue = Volley.newRequestQueue(this)

        val url = "https://www.emsifa.com/api-wilayah-indonesia/api/regencies/$provinceId.json"
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
            val kabupaten = mutableListOf("Pilih Kabupaten/Kota") // Tambahkan item dummy
            val kabupatenMap = mutableMapOf<String, String>()

            for (i in 0 until response.length()) {
                val item = response.getJSONObject(i)
                val id = item.getString("id")
                val name = item.getString("name")
                kabupaten.add(name)
                kabupatenMap[name] = id
            }

            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kabupaten)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerKabupatenKota.adapter = adapter

            // Listener untuk spinnerKabupatenKota
            binding.spinnerKabupatenKota.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position > 0) {
                        // Lakukan sesuatu jika item valid dipilih
                        val selectedKabupaten = kabupaten[position]
//                        val kabupatenId = kabupatenMap[selectedKabupaten]
                        // Proses kabupatenId jika diperlukan
                    } else {
                        // Handle jika user memilih item dummy
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Handle jika tidak ada yang dipilih (opsional)
                }
            }
        }, {
            // Handle error
        })
        queue.add(request)
    }


    private fun resetSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Pilih Kabupaten/Kota"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }


}