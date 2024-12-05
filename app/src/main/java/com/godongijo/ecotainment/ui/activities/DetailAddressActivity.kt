package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityDetailAddressBinding
import com.godongijo.ecotainment.services.auth.AuthService

class DetailAddressActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailAddressBinding

    private val authService = AuthService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()

    }

    private fun init() {
        loadProvinces()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonSave.setOnClickListener {
            saveAddress()
        }
    }

    private fun saveAddress() {
        // Ambil data dari input
        val recipientName = binding.inputRecipientName.text.toString().trim()
        val phoneNumber = binding.inputPhoneNumber.text.toString().trim()
        val province = binding.spinnerProvinsi.selectedItem?.toString()
        val city = binding.spinnerKabupatenKota.selectedItem?.toString()
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
            city.isNullOrEmpty() || city == "Pilih" -> {
                showErrorMessage("Silakan pilih kabupaten/kota")
                return
            }
            fullAddress.isEmpty() -> {
                binding.inputFullAddress.error = "Alamat lengkap tidak boleh kosong"
                binding.inputFullAddress.requestFocus()
                return
            }
            else -> {
                // Jika semua validasi lolos, panggil authService.addAddress
                authService.addAddress(
                    this,
                    recipientName,
                    phoneNumber,
                    province,
                    city,
                    fullAddress,
                    onSuccess = {
                        showSuccessMessage("Alamat berhasil disimpan")
                        finish() // Kembali ke aktivitas sebelumnya
                    },
                    onError = { errorMessage ->
                        showErrorMessage(errorMessage)
                    }
                )
            }
        }
    }

    // Fungsi untuk menampilkan pesan error
    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Fungsi untuk menampilkan pesan sukses
    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    // Fungsi untuk load provinsi
    private fun loadProvinces() {
        val queue = Volley.newRequestQueue(this)

        val url = "https://www.emsifa.com/api-wilayah-indonesia/api/provinces.json"
        val request = JsonArrayRequest(Request.Method.GET, url, null, { response ->
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
            val kabupaten = mutableListOf<String>()
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
                    val selectedKabupaten = kabupaten[position]
                    val kabupatenId = kabupatenMap[selectedKabupaten]
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

    // Fungsi reset spinner
    private fun resetSpinner(spinner: Spinner) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("Pilih"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }
}