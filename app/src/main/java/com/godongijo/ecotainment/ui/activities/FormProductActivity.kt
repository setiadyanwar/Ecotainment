package com.godongijo.ecotainment.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityFormProductBinding
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.utilities.Glide

class FormProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormProductBinding

    private var isEditing: Boolean = false

    private val productService = ProductService()

    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
        isEditing = intent.getBooleanExtra("isEditing", false)

        if (isEditing) {
            initProductInfo()
        }
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonSave.setOnClickListener {
            saveProduct()
        }

        binding.inputProductImage.setOnClickListener {
            openGallery()
        }
    }

    private fun initProductInfo() {
        val productId = intent.getIntExtra("productId", 0)

        if (productId != 0) {
            productService.getSingleProduct(
                context = this,
                productId = productId,
                onResult = { product ->
                    val imageProduct = applicationContext.getString(R.string.base_url) + product.imageUrl
                    Glide().loadImageFromUrl(binding.inputProductImage, imageProduct)

                    binding.inputProductName.setText(product.name)
                    binding.inputProductPrice.setText(product.price.toString())
                    binding.inputProductCategory.setText(product.category)
                    binding.inputProductDescription.setText(product.description)
                },
                onError = { errorMessage ->
                    Toast.makeText(this, "Error memuat data produk", Toast.LENGTH_SHORT).show()
                    Log.d("ERROR SINGLE PRODUCT", errorMessage)
                }
            )
        }
    }

    private fun saveProduct() {
        val productName = binding.inputProductName.text.toString().trim()
        val productPrice = binding.inputProductPrice.text.toString().toInt()
        val productCategory = binding.inputProductCategory.text.toString().trim()
        val productDescription = binding.inputProductDescription.text.toString().trim()
        val productImage = selectedUri

        // Validasi data input
        when {
            productName.isEmpty() -> {
                binding.inputProductName.error = "Nama produk tidak boleh kosong"
                binding.inputProductName.requestFocus()
                return
            }

            binding.inputProductPrice.text.toString().isEmpty() -> {
                binding.inputProductPrice.error = "Harga tidak boleh kosong"
                binding.inputProductPrice.requestFocus()
                return
            }

            productCategory.isEmpty() -> {
                binding.inputProductCategory.error = "Kategori tidak boleh kosong"
                binding.inputProductCategory.requestFocus()
                return
            }

            productDescription.isEmpty() -> {
                binding.inputProductDescription.error = "Deskripsi tidak boleh kosong"
                binding.inputProductDescription.requestFocus()
                return
            }

            else -> {

                if(isEditing) {
                    val productId = intent.getIntExtra("productId", 0)

                    productService.updateProduct(
                        this,
                        productId,
                        productName,
                        productPrice,
                        productCategory,
                        productDescription,
                        productImage,
                        onSuccess = {
                            Toast.makeText(this, "Berhasil mengedit produk", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = { errorMessage ->
                            Toast.makeText(this, "Gagal mengedit produk: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    productService.addNewProduct(
                        this,
                        productName,
                        productPrice,
                        productCategory,
                        productDescription,
                        productImage,
                        onSuccess = {
                            Toast.makeText(this, "Berhasil menambah produk", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onError = { errorMessage ->
                            Toast.makeText(this, "Gagal menambah produk: $errorMessage", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

            }

        }
    }



    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedUri = uri
                // Menampilkan gambar yang dipilih
                binding.inputProductImage.setImageURI(selectedUri)
            }
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
}