package com.godongijo.ecotainment.ui.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.ProductDetailAdapter
import com.godongijo.ecotainment.databinding.ActivityDetailProductBinding
import com.godongijo.ecotainment.databinding.DialogAddCartBinding
import com.godongijo.ecotainment.databinding.DialogSelectQuantityBinding
import com.godongijo.ecotainment.services.cart.CartService
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.services.product.WishlistService
import com.godongijo.ecotainment.utilities.Glide
import com.godongijo.ecotainment.utilities.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.text.NumberFormat
import java.util.Locale

class DetailProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProductBinding
    private lateinit var adapter: ProductDetailAdapter

    private val productService = ProductService()
    private val cartService = CartService()
    private val wishlistService = WishlistService()

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var authToken: String

    private var productName = ""
    private var productImage = ""
    private var productPrice = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cartBadge.visibility = View.GONE

        init()
        setListeners()
    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        authToken = preferenceManager.getString("auth_token") ?: ""

        initDetailProduct()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.cartContainer.setOnClickListener {
            if(authToken == "") {
                startActivity(Intent(this, SignInActivity::class.java))
            } else {
                startActivity(Intent(this, CartActivity::class.java))
            }
        }

        binding.notificationButton.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        binding.addToCartButton.setOnClickListener {
            if(authToken == "") {
                startActivity(Intent(this, SignInActivity::class.java))
            } else {
                // Ambil id produk dari intent
                val productId = intent.getIntExtra("product_id", 0)

                cartService.addNewCart(
                    context = this,
                    productId = productId,
                    onSuccess = {  ->
                        showSuccessDialog()
                    },
                    onError = {
                    }
                )
            }
        }

        binding.buyButton.setOnClickListener {
            if(authToken == "") {
                startActivity(Intent(this, SignInActivity::class.java))
            } else {
                // Inisialisasi Dialog
                val dialogBinding = DialogSelectQuantityBinding.inflate(layoutInflater)
                val dialog = BottomSheetDialog(this)
                dialog.setContentView(dialogBinding.root)

                dialogBinding.productName.text = productName
                Glide().loadImageFromUrl(dialogBinding.productImage, productImage)
                dialogBinding.productPrice.text = "Rp${productPrice}"

                // Ambil data product_id dari intent
                val productId = intent.getIntExtra("product_id", 0)

                // Variabel untuk menyimpan quantity
                var quantity = 1
                dialogBinding.quantity.text = quantity.toString()

                // Fungsi untuk memperbarui harga total
                fun updateTotalPrice() {
                    val totalPrice = productPrice * quantity
                    dialogBinding.productPrice.text = "Rp$totalPrice"
                }

                // Tombol plus untuk menambah quantity
                dialogBinding.plusQuantity.setOnClickListener {
                    quantity++
                    dialogBinding.quantity.text = quantity.toString()
                    updateTotalPrice()
                }

                // Tombol minus untuk mengurangi quantity (min 1)
                dialogBinding.minusQuantity.setOnClickListener {
                    if (quantity > 1) {
                        quantity--
                        dialogBinding.quantity.text = quantity.toString()
                        updateTotalPrice()
                    }
                }

                // Tombol checkout
                dialogBinding.checkoutButton.setOnClickListener {
                    val intent = Intent(this, CheckoutActivity::class.java).apply {
                        putExtra("sourceActivity", "DetailProductActivity")
                        putExtra("selectedProductId", productId)
                        putExtra("selectedQuantity", quantity)
                    }
                    startActivity(intent)
                    dialog.dismiss() // Tutup dialog setelah checkout
                }

                dialogBinding.closeDialog.setOnClickListener {
                    dialog.dismiss()
                }

                // Tampilkan dialog
                dialog.show()
            }
        }

    }

    // Inisialisasi ViewPager dan TabLayout
    private fun initDetailProduct() {
        binding.shimmerProductDetailLayout.visibility = View.VISIBLE
        binding.shimmerRatingSalesLayout.visibility = View.VISIBLE
        binding.productDetailLayout.visibility = View.GONE
        binding.ratingSalesLayout.visibility = View.GONE

        adapter = ProductDetailAdapter(this)

        // Ambil id produk dari intent
        val productId = intent.getIntExtra("product_id", 0)

        productService.getSingleProduct(
            context = this,
            productId = productId,
            onResult = { product ->
                // Memperbarui UI dengan data produk
                val imageProduct = applicationContext.getString(R.string.base_url) + product.imageUrl
                Glide().loadImageFromUrl(binding.productImage, imageProduct)

                // Format harga
                val price = product.price
                val formattedPrice = NumberFormat.getInstance(Locale("id", "ID")).format(price) // Format ke format ribuan

                productName = product.name
                productImage = imageProduct
                productPrice = product.price

                binding.productTitle.text = product.name
                binding.productPrice.text = "Rp$formattedPrice"
                binding.productRating.text = product.rating.toString()
                binding.productTotalSales.text = "| ${product.totalSales} terjual"
                binding.reviewsCount.text = "(${product.reviews?.size.toString()}) Ulasan"


                // Set deskripsi produk ke adapter
                adapter.setProductDescription(product.description)

                // Set ulasan produk ke adapter
                product.reviews?.let { adapter.setProductReviews(it, product.rating) }

                // Mengatur adapter ke ViewPager setelah set description
                binding.vP2.adapter = adapter

                // Connect TabLayout with ViewPager2 using TabLayoutMediator
                TabLayoutMediator(binding.tabLayout, binding.vP2) { tab, position ->
                    tab.customView = createCustomTabView(
                        when (position) {
                            0 -> "Deskripsi"
                            1 -> "Ulasan Produk"
                            else -> ""
                        }
                    )
                }.attach()

                // Cek apakah produk ada di wishlist
                checkIfProductInWishlist(productId)

                binding.shimmerProductDetailLayout.visibility = View.GONE
                binding.shimmerRatingSalesLayout.visibility = View.GONE
                binding.productDetailLayout.visibility = View.VISIBLE
                binding.ratingSalesLayout.visibility = View.VISIBLE
            },
            onError = { errorMessage ->
                Log.d("ERROR SINGLE PRODUCT", errorMessage)
            }
        )

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val textView = tab?.customView as? TextView
                textView?.setTextColor(getColor(R.color.primary_700))
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val textView = tab?.customView as? TextView
                textView?.setTextColor(getColor(R.color.grey))
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    // Membuat tampilan kustom untuk TabLayout
    private fun createCustomTabView(text: String): TextView {
        val textView = LayoutInflater.from(this).inflate(R.layout.custom_tab, null) as TextView
        textView.text = text
        return textView
    }

    private fun showSuccessDialog() {
        // Inflate layout menggunakan binding
        val dialogBinding = DialogAddCartBinding.inflate(LayoutInflater.from(this))

        // Buat dialog dan pasang binding root sebagai view-nya
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(dialogBinding.root)

        // Set animasi pop-up pada ikon
        val animation = AnimationUtils.loadAnimation(this, R.anim.pop_up_animation)
        dialogBinding.checkIcon.startAnimation(animation)

        // Tutup dialog setelah beberapa detik
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 2000)

        // Tampilkan dialog
        dialog.show()
    }

    private fun checkIfProductInWishlist(productId: Int) {
        if (authToken == "") {
            return
        }

        wishlistService.getAllWishlist(
            context = this,
            onResult = { wishlistList ->
                val isInWishlist = wishlistList.any { it.productId == productId }
                binding.favoriteIcon.setImageResource(
                    if (isInWishlist) R.drawable.ic_wishlist_fill else R.drawable.ic_wishlist_outline
                )

                binding.favoriteIcon.setOnClickListener {
                    if (isInWishlist) {
                        removeProductFromWishlist(productId)
                    } else {
                        addProductToWishlist(productId)
                    }
                }
            },
            onError = { errorMessage ->
                Log.e("WishlistError", errorMessage)
            }
        )
    }


    private fun addProductToWishlist(productId: Int) {
        if (authToken == "") {
            return
        }

        wishlistService.toggleWishlist(
            context = this,
            productId = productId,
            onResult = {
                binding.favoriteIcon.setImageResource(R.drawable.ic_wishlist_fill)
                Log.d("Wishlist", "Product added to wishlist")
                checkIfProductInWishlist(productId)
            },
            onError = { errorMessage ->
                Log.e("WishlistError", errorMessage)
            }
        )
    }

    private fun removeProductFromWishlist(productId: Int) {
        if (authToken == "") {
            return
        }

        wishlistService.toggleWishlist(
            context = this,
            productId = productId,
            onResult = {
                binding.favoriteIcon.setImageResource(R.drawable.ic_wishlist_outline)
                Log.d("Wishlist", "Product removed from wishlist")
                checkIfProductInWishlist(productId)
            },
            onError = { errorMessage ->
                Log.e("WishlistError", errorMessage)
            }
        )
    }

}
