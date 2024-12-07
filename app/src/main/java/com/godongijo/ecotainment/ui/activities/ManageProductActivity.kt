package com.godongijo.ecotainment.ui.activities

import ManageProductAdapter
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.ActivityManageProductBinding
import com.godongijo.ecotainment.databinding.DialogDeleteProductBinding
import com.godongijo.ecotainment.services.product.ProductService

class ManageProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageProductBinding

    private lateinit var manageProductAdapter: ManageProductAdapter

    private val productService = ProductService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        initProductList()
    }

    private fun init() {
        initProductList()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.addNewProduct.setOnClickListener {
            val intent = Intent(this, FormProductActivity::class.java).apply {
                putExtra("isEditing", false)
            }
            startActivity(intent)
        }
    }

    private fun initProductList() {
        binding.manageProductRecyclerView.layoutManager = LinearLayoutManager(this)
        manageProductAdapter = ManageProductAdapter(
            context = this,
            productList = emptyList(),
            onEditClick = { product ->
                // Logika untuk tombol edit
                editProduct(product.id)
            },
            onDeleteClick = { product ->
                // Logika untuk tombol delete
                deleteProduct(product.id)
            }
        )

        val skeletonAdapter = SkeletonAdapter(1, 1)
        binding.manageProductRecyclerView.adapter = skeletonAdapter

        productService.getProductList(
            context = this,
            onResult = { productList ->
                // Update UI based on whether there are items in the list
                if (productList.isEmpty()) {
                    binding.manageProductRecyclerView.visibility = View.GONE
                    binding.emptyProduct.visibility = View.VISIBLE
                } else {
                    binding.manageProductRecyclerView.visibility = View.VISIBLE
                    binding.emptyProduct.visibility = View.GONE

                    binding.manageProductRecyclerView.adapter = manageProductAdapter
                    manageProductAdapter.updateList(productList)
                }
            },
            onError = {
                binding.manageProductRecyclerView.visibility = View.GONE
                binding.emptyProduct.visibility = View.VISIBLE
            }
        )
    }

    private fun editProduct(productId: Int) {
        val intent = Intent(this, FormProductActivity::class.java).apply {
            putExtra("isEditing", true)
            putExtra("productId", productId)
        }
        startActivity(intent)
    }

    private fun deleteProduct(productId: Int) {
        showConfirmDialog(
            onConfirm = {
                productService.deleteProduct(
                    context = this,
                    productId = productId,
                    onResult = {
                        initProductList()
                    },
                    onError = {
                        Log.d("CartAdapter", "Failed delete product from cart")
                    }
                )
            },
            onCancel = {

            }
        )
    }


    private fun showConfirmDialog(
        onConfirm: () -> Unit,
        onCancel: () -> Unit
    ) {
        // Inflate layout menggunakan View Binding
        val dialogBinding = DialogDeleteProductBinding.inflate(LayoutInflater.from(this))

        // Buat dialog
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)

        // Aksi untuk tombol Cancel
        dialogBinding.cancelButton.setOnClickListener {
            onCancel()
            dialog.dismiss()
        }

        // Aksi untuk tombol Confirm
        dialogBinding.confirmButton.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        // Tampilkan dialog
        dialog.show()
    }
}