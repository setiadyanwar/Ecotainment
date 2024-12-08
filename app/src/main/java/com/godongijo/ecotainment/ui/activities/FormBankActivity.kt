package com.godongijo.ecotainment.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.SpinnerBankAdapter
import com.godongijo.ecotainment.databinding.ActivityFormBankBinding
import com.godongijo.ecotainment.databinding.ActivityFormProductBinding
import com.godongijo.ecotainment.services.bank.BankService
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.utilities.Glide

class FormBankActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormBankBinding

    private var isEditing: Boolean = false

    private val bankService = BankService()

    private var selectedBank: Pair<String, Int>? = null

    private var qrisImageUri: Uri? = null

    private var isHasQRSelected: Boolean = false

    private val banks = listOf(
        "Pilih Bank" to R.drawable.ic_bank, // Dummy item
        "QRIS" to R.drawable.ic_bankmaster_qris,
        "BCA" to R.drawable.ic_bankmaster_bca,
        "BNI" to R.drawable.ic_bankmaster_bni,
        "BRI" to R.drawable.ic_bankmaster_bri,
        "Mandiri" to R.drawable.ic_bankmaster_mandiri
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBankBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
        isEditing = intent.getBooleanExtra("isEditing", false)

        if (isEditing) {
            initBankInfo()
        }

        initBankSpinner()
        initDefaultInstructions()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonSave.setOnClickListener {
            saveBank()
        }

        binding.inputQRImage.setOnClickListener {
            openGallery()
        }
    }

    private fun initBankInfo() {
        val bankId = intent.getIntExtra("bankId", 0)

        if (bankId != 0) {
            bankService.getSingleBank(
                this,
                bankId,
                onResult = { bank ->
                    binding.apply {
                        inputAccountHolder.setText(bank.accountHolder)
                        inputAccountNumber.setText(bank.accountNumber)
                        inputPaymentInstructions.setText(bank.paymentInstructions?.replace("\\n", "\n"))

                        // Cari posisi bank berdasarkan nama
                        val bankIndex = banks.indexOfFirst { it.first == bank.name }
                        if (bankIndex >= 0) {
                            binding.spinnerBank.setSelection(bankIndex)
                        }

                        if (bank.name == "QRIS") {
                            val qrImage = getString(R.string.base_url) + bank.logo
                            isHasQRSelected = true
                            Glide().loadImageFitCenter(binding.inputQRImage, qrImage)
                        }

                    }
                },
                onError = {}
            )
        }
    }

    private fun initBankSpinner() {
        val spinnerBankAdapter = SpinnerBankAdapter(this, banks)
        binding.spinnerBank.adapter = spinnerBankAdapter

        binding.spinnerBank.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Abaikan item dummy
                    selectedBank = banks[position]

                    // Periksa apakah item yang dipilih adalah "QRIS"
                    if (selectedBank?.first == "QRIS") {
                        binding.layoutInputQRImage.visibility = View.VISIBLE
                        binding.inputQRError.visibility = View.GONE
                    } else {
                        binding.layoutInputQRImage.visibility = View.GONE
                        binding.inputQRError.visibility = View.GONE
                    }
                } else {
                    // Reset jika dummy item dipilih
                    selectedBank = null
                    binding.layoutInputQRImage.visibility = View.GONE
                    binding.inputQRError.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBank = null
                binding.layoutInputQRImage.visibility = View.GONE
                binding.inputQRError.visibility = View.GONE
            }
        }

    }

    private fun initDefaultInstructions() {
        val defaultInstruction = "1. Copy nomor rekening di atas \\n2. Anda dapat pergi ke ATM terdekat atau menggunakan mobile banking \\n3. Transfer sesuai dengan nominal tagihan \\n4. Upload bukti transfer"
        binding.inputPaymentInstructions.setText(defaultInstruction.replace("\\n", "\n"))
    }

    private fun saveBank() {
        val bankName = selectedBank?.first ?: ""
        val bankLogo = selectedBank?.second ?: 0
        val bankAccountHolder = binding.inputAccountHolder.text.toString().trim()
        val bankAccountNumber = binding.inputAccountNumber.text.toString().trim()
        val bankPaymentInstruction = binding.inputPaymentInstructions.text.toString().trim()

        when {
            bankName.isEmpty() -> {
                binding.spinnerBank.requestFocus()
                binding.spinnerBank.performClick() // Membuka spinner untuk memilih
                return
            }

            bankName == "QRIS" && !isHasQRSelected -> {
                binding.inputQRError.visibility = View.VISIBLE
            }

            bankLogo == 0 -> {
                binding.spinnerBank.requestFocus()
                binding.spinnerBank.performClick() // Membuka spinner untuk memilih
                return
            }

            bankAccountHolder.isEmpty() -> {
                binding.inputAccountHolder.error = "Nama pemilik bank tidak boleh kosong"
                binding.inputAccountHolder.requestFocus()
                return
            }

            bankAccountNumber.isEmpty() -> {
                binding.inputAccountNumber.error = "Nomor rekening bank tidak boleh kosong"
                binding.inputAccountNumber.requestFocus()
                return
            }

            bankPaymentInstruction.isEmpty() -> {
                binding.inputPaymentInstructions.error = "Instruksi tidak boleh kosong"
                binding.inputPaymentInstructions.requestFocus()
                return
            }

            else -> {
                if(isEditing) {
                    val bankId = intent.getIntExtra("bankId", 0)

                    bankService.updateBank(
                        this,
                        id = bankId,
                        name = bankName,
                        logoResId = bankLogo,
                        qrImageUri = if (bankName == "QRIS") qrisImageUri else null,
                        accountNumber = bankAccountNumber,
                        accountHolder = bankAccountHolder,
                        paymentInstructions = bankPaymentInstruction,
                        onSuccess = {
                            Toast.makeText(this, "Berhasil mengedit bank", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = {
                            Toast.makeText(this, "Gagal mengedit bank", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    bankService.addNewBank(
                        this,
                        name = bankName,
                        logoResId = bankLogo,
                        qrImageUri = if (bankName == "QRIS") qrisImageUri else null,
                        accountNumber = bankAccountNumber,
                        accountHolder = bankAccountHolder,
                        paymentInstructions = bankPaymentInstruction,
                        onSuccess = {
                            Toast.makeText(this, "Berhasil menambah bank", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = {
                            Toast.makeText(this, "Gagal menambah bank", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            qrisImageUri = selectedUri
            // Menampilkan gambar yang dipilih
            binding.inputQRImage.setImageURI(qrisImageUri)
            isHasQRSelected = true
            binding.inputQRError.visibility = View.GONE
        }
    }

    private fun openGallery() {
        // Cek versi Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Minta izin membaca media gambar
                requestPermissionMediaImages.launch(Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                // Izin sudah diberikan, buka galeri
                galleryLauncher.launch("image/*")
            }
        } else {
            // Android 12 ke bawah
            galleryLauncher.launch("image/*")
        }
    }

    // Launcher untuk meminta izin media
    private val requestPermissionMediaImages = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Izin diberikan, buka galeri
            openGallery()
        } else {
            // Izin ditolak, beri tahu pengguna
            Toast.makeText(
                this,
                "Izin akses gambar diperlukan untuk memilih foto",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}