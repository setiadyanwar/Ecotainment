package com.godongijo.ecotainment.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.BankAdapter
import com.godongijo.ecotainment.databinding.ActivityPaymentBinding
import com.godongijo.ecotainment.models.Bank
import com.godongijo.ecotainment.models.BankAccount
import com.godongijo.ecotainment.services.transaction.TransactionService
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private lateinit var bankAdapter: BankAdapter

    private lateinit var countDownTimer: CountDownTimer

    private val transactionService = TransactionService()

    // Tambahkan variabel untuk menandai sumber activity
    private var isNewTransaction: Boolean = true
    private var transactionId: Int? = null

    // Konstanta untuk permission
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
        private const val GALLERY_PERMISSION_REQUEST_CODE = 101
    }

    // Uri untuk menyimpan foto dari kamera
    private var cameraImageUri: Uri? = null

    // Launcher untuk membuka kamera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            cameraImageUri?.let { uri ->
                // Proses upload gambar dari kamera
                uploadProofImage(uri)
            }
        }
    }

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                // Proses upload gambar dari galeri
                uploadProofImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Periksa sumber intent
        isNewTransaction = intent.getBooleanExtra("isNewTransaction", true)
        transactionId = intent.getIntExtra("transactionId", 0)

        init()
        setListeners()

    }

    override fun onDestroy() {
        super.onDestroy()
        // Hentikan timer jika aktivitas dihancurkan
        countDownTimer.cancel()
    }

    private fun init() {
        if (isNewTransaction) {
            initNewTransaction()
        } else {
            initExistingTransaction()
        }

        binding.btnUploadProof.visibility = View.VISIBLE
        binding.layoutProofFile.visibility = View.GONE
        binding.layoutConfirmProof.visibility = View.GONE
        binding.confirmMessage.visibility = View.GONE

        setupRecyclerView()
        loadBanks()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnUploadProof.setOnClickListener {
//            showImageSourceDialog()
            openGallery()
        }

        binding.editPaymentProof.setOnClickListener {
//            showImageSourceDialog()
            openGallery()
        }


    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Kamera", "Galeri")
        AlertDialog.Builder(this)
            .setTitle("Pilih Sumber Gambar")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        // Cek permission kamera
        if (checkCameraPermission()) {
            // Buat file untuk menyimpan gambar
            val imageFile = createImageFile()
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                imageFile
            )
            cameraImageUri = uri

            // Buka kamera dengan local uri
            uri?.let {
                cameraLauncher.launch(it)
            }
        } else {
            requestCameraPermission()
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
                launchGalleryIntent()
            }
        } else {
            // Android 12 ke bawah
            launchGalleryIntent()
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

    private fun launchGalleryIntent() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryLauncher.launch(galleryIntent)
    }


    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile(
            "proof_${System.currentTimeMillis()}",
            ".jpg",
            storageDir
        )
    }

    private fun uploadProofImage(imageUri: Uri) {
        binding.btnUploadProof.visibility = View.GONE
        binding.layoutProofFile.visibility = View.VISIBLE
        binding.layoutConfirmProof.visibility = View.VISIBLE

        val fileName = getFileNameFromUri(this, imageUri)
        binding.uploadFileName.text = fileName ?: "Bukti pembayaran"

        binding.layoutConfirmProof.setOnClickListener {
            transactionId?.let { it1 -> uploadTransactionProof(it1, imageUri) }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permission ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initNewTransaction() {
        binding.shimmerPaymentAmount.visibility = View.VISIBLE
        binding.paymentAmount.visibility = View.INVISIBLE

        val totalAmount = intent.getIntExtra("totalAmount", 0)
        binding.paymentAmount.text = "Rp${formatCurrency(totalAmount.toLong())}"

        binding.shimmerPaymentAmount.visibility = View.INVISIBLE
        binding.paymentAmount.visibility = View.VISIBLE

        val recipientName = intent.getStringExtra("recipientName") ?: ""
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        val transactionItems = intent.getSerializableExtra("transactionItems") as? ArrayList<Pair<Int, Int>>

        if (transactionItems != null) {
            transactionService.addNewTransaction(
                context = this,
                totalAmount = totalAmount,
                recipientNames = recipientName,
                phoneNumber = phoneNumber,
                address = address,
                items = transactionItems,
                onResult = { transactions ->
                    transactionId = transactions.id
                    initTimer()
                },
                onError = {
                    // Tangani error saat membuat transaksi
                    Toast.makeText(this, "Gagal membuat transaksi", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initExistingTransaction() {
        binding.shimmerPaymentAmount.visibility = View.VISIBLE
        binding.paymentAmount.visibility = View.INVISIBLE

        transactionId?.let {
            transactionService.getTransactionList(
                context = this,
                onResult = { transactionList ->
                    // Cari transaksi dengan ID yang sesuai
                    val transaction = transactionList.find { it.id == transactionId }

                    if (transaction != null) {
                        // Update UI dengan data transaksi
                        binding.paymentAmount.text = "Rp${formatCurrency(transaction.totalAmount.toLong())}"

                        binding.shimmerPaymentAmount.visibility = View.INVISIBLE
                        binding.paymentAmount.visibility = View.VISIBLE

                        // Inisialisasi timer berdasarkan waktu transaksi
                        initTimer(transaction.createdAt)
                    } else {
                        // Tangani jika transaksi tidak ditemukan
                        Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { errorMessage ->
                    // Tangani error saat mengambil data transaksi
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun initTimer(createdAt: String? = null) {
        // Total waktu dalam milidetik (contoh: 12 jam)
        val totalTimeInMillis: Long = 12 * 60 * 60 * 1000

        // Jika createdAt disediakan, hitung sisa waktu
        val remainingTime = if (createdAt != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val createdAtTime = sdf.parse(createdAt)?.time ?: 0

            val elapsedTime = System.currentTimeMillis() - createdAtTime
            maxOf(totalTimeInMillis - elapsedTime, 0)
        } else {
            totalTimeInMillis
        }

        // Inisialisasi CountDownTimer
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Hitung jam, menit, dan detik
                val hours = millisUntilFinished / (1000 * 60 * 60) % 24
                val minutes = millisUntilFinished / (1000 * 60) % 60
                val seconds = millisUntilFinished / 1000 % 60

                // Set teks pada TextView
                binding.tvTimerHours.text = String.format("%02d", hours)
                binding.tvTimerMinutes.text = String.format("%02d", minutes)
                binding.tvTimerSeconds.text = String.format("%02d", seconds)
            }

            override fun onFinish() {
                // Tindakan ketika timer selesai
                binding.tvTimerHours.text = "00"
                binding.tvTimerMinutes.text = "00"
                binding.tvTimerSeconds.text = "00"
            }
        }

        // Mulai timer
        countDownTimer.start()
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

    private fun uploadTransactionProof(transactionId: Int, imageUri: Uri) {
        transactionService.uploadProof(
            context = this,
            transactionId = transactionId,
            paymentProofImageUri = imageUri,
            onSuccess = {
                binding.btnUploadProof.visibility = View.GONE
                binding.editPaymentProof.visibility = View.GONE
                binding.layoutProofFile.visibility = View.VISIBLE
                binding.layoutConfirmProof.visibility = View.GONE
                binding.confirmMessage.visibility = View.VISIBLE
            },
            onError = { error ->
                Log.d("ERROR", error)
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
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

    private fun formatCurrency(amount: Long): String {
        return NumberFormat.getInstance(Locale("id", "ID")).format(amount)
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null

        // Cek apakah Uri berasal dari content provider
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    // Ambil nama file dari kolom DISPLAY_NAME
                    fileName = it.getString(it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        // Jika Uri berasal dari file (misalnya file://), gunakan lastPathSegment
        if (fileName == null) {
            fileName = uri.lastPathSegment
        }

        return fileName
    }


}
