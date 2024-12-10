package com.godongijo.ecotainment.ui.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityFormProductBinding
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.utilities.DialogLoader
import com.godongijo.ecotainment.utilities.Glide
import com.godongijo.ecotainment.utilities.ImagePicker

class FormProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormProductBinding

    private var isEditing: Boolean = false

    private val productService = ProductService()

    private var imagePath: String? = null

    private lateinit var imagePicker: ImagePicker

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.dismiss()
        dialog = null
    }

    private fun init() {
        imagePicker = ImagePicker(this)

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
        val productPrice = binding.inputProductPrice.text.toString().trim()
        val productCategory = binding.inputProductCategory.text.toString().trim()
        val productDescription = binding.inputProductDescription.text.toString().trim()
        val productImage = imagePath

        // Validasi data input
        when {
            productName.isEmpty() -> {
                binding.inputProductName.error = "Nama produk tidak boleh kosong"
                binding.inputProductName.requestFocus()
                return
            }

            productPrice.isEmpty() -> {
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
                dialog = DialogLoader.show(context = this, message = "Mohon tunggu, proses sedang berjalan...") ?: return

                if(isEditing) {
                    val productId = intent.getIntExtra("productId", 0)

                    productService.updateProduct(
                        this,
                        productId,
                        productName,
                        productPrice.toInt(),
                        productCategory,
                        productDescription,
                        productImage,
                        onSuccess = {
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.success(it, context = this, message = "Berhasil Mengedit Produk")

                                    binding.root.postDelayed({
                                        finish()
                                    }, 1500L)
                                }
                            }
                        },
                        onError = { errorMessage ->
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.error(it, context = this, message = "Gagal Mengedit Produk")

                                    binding.root.postDelayed({
                                        Toast.makeText(this, "Gagal mengedit produk: $errorMessage", Toast.LENGTH_SHORT).show()
                                    }, 1500L)
                                }
                            }
                        }
                    )
                } else {
                    productService.addNewProduct(
                        this,
                        productName,
                        productPrice.toInt(),
                        productCategory,
                        productDescription,
                        productImage,
                        onSuccess = {
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.success(it, context = this, message = "Berhasil Menambah Produk")

                                    binding.root.postDelayed({
                                        finish()
                                    }, 1500L)
                                }
                            }
                        },
                        onError = { errorMessage ->
                            if (!isFinishing && !isDestroyed) {
                                dialog?.let {
                                    DialogLoader.error(it, context = this, message = "Gagal Menambah Produk")

                                    binding.root.postDelayed({
                                        Toast.makeText(this, "Gagal menambah produk: $errorMessage", Toast.LENGTH_SHORT).show()
                                    }, 1500L)
                                }
                            }
                        }
                    )
                }

            }

        }
    }


    private fun openGallery() {
        imagePicker.pickImage(ImagePicker.ImageSource.GALLERY) { uri ->
            uri?.let { selectedUri ->
                // Dapatkan path
                imagePath = imagePicker.getPathFromUri(this, selectedUri)
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedUri))
                binding.inputProductImage.setImageBitmap(bitmap)

            } ?: run {
                Toast.makeText(this, "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show()
            }
        }
    }
}