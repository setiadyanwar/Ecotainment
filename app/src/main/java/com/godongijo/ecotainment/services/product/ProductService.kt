package com.godongijo.ecotainment.services.product

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.models.Review
import com.godongijo.ecotainment.models.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class ProductService {

    fun getProductList(
        context: Context,
        searchQuery: String? = null, // Parameter opsional
        onResult: (List<Product>) -> Unit,
        onError: (String) -> Unit
    ) {
        var url = context.getString(R.string.api_products)

        // Tambahkan query parameter jika searchQuery diberikan
        if (!searchQuery.isNullOrEmpty()) {
            url += "?search=${searchQuery}"
        }

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data produk jika success
                        val dataArray = response.getJSONArray("data")
                        val productList = parseProductList(dataArray)
                        onResult(productList)
                    } else {
                        // Kirim pesan error jika status success = false
                        onError(message)
                    }
                } catch (e: Exception) {
                    // Kirim pesan error jika parsing JSON gagal
                    onError("Terjadi kesalahan saat memproses data: ${e.message}")
                }
            },
            { error ->
                val networkResponse = error.networkResponse
                val errorMessage = if (networkResponse?.data != null) {
                    // Parsing respons error dari server
                    val errorJson = JSONObject(String(networkResponse.data))
                    errorJson.optString("message", "Terjadi kesalahan")
                } else {
                    // Default pesan jika tidak ada respons dari server
                    "Koneksi gagal. Periksa koneksi internet Anda."
                }
                Log.e("ProductService", "Error fetching products: $errorMessage")
                onError(errorMessage)
            }
        ) {}

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    // Helper function to parse JSONArray to List<Product>
    private fun parseProductList(dataArray: JSONArray): List<Product> {
        val productList = mutableListOf<Product>()

        for (i in 0 until dataArray.length()) {
            val productJson = dataArray.getJSONObject(i)

            val product = Product(
                id = productJson.optString("id", ""),
                name = productJson.optString("name", ""),
                category = productJson.optString("category", ""),
                price = productJson.optString("price", ""),
                description = productJson.optString("description", ""),
                imageUrl = productJson.optString("image", ""),
                rating = productJson.optString("average_rating", "0.0").toDoubleOrNull() ?: 0.0, // Default rating
                totalSales = productJson.optInt("total_sales", 0),
                lastUpdate = productJson.optString("updated_at", ""),
                reviews = emptyList()
            )

            productList.add(product)
        }

        return productList
    }




    fun getSingleProduct(
        context: Context,
        productId: String,
        onResult: (Product) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_products) + "/$productId"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data produk jika success
                        val productData = response.getJSONObject("data")
                        val product = parseSingleProduct(productData)
                        onResult(product)
                    } else {
                        // Kirim pesan error jika status success = false
                        onError(message)
                    }
                } catch (e: Exception) {
                    // Kirim pesan error jika parsing JSON gagal
                    onError("Terjadi kesalahan saat memproses data: ${e.message}")
                }
            },
            { error ->
                val networkResponse = error.networkResponse
                val errorMessage = if (networkResponse?.data != null) {
                    // Parsing respons error dari server
                    val errorJson = JSONObject(String(networkResponse.data))
                    errorJson.optString("message", "Terjadi kesalahan")
                } else {
                    // Default pesan jika tidak ada respons dari server
                    "Koneksi gagal. Periksa koneksi internet Anda."
                }
                Log.e("ProductService", "Error fetching product: $errorMessage")
                onError(errorMessage)
            }
        ) {}

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    private fun parseSingleProduct(productJson: JSONObject): Product {
        val reviewsArray = productJson.optJSONArray("reviews") ?: JSONArray()
        val reviews = mutableListOf<Review>()

        for (i in 0 until reviewsArray.length()) {
            val reviewJson = reviewsArray.getJSONObject(i)
            val userJson = reviewJson.optJSONObject("user")

            val user = userJson?.let {
                User(
                    id = it.optString("id", ""),
                    email = it.optString("email", ""),
                    username = it.optString("username", ""),
                    phoneNumber = it.optString("phone_number", ""),
                    profilePicture = it.optString("profile_picture", ""),
                    address = it.optString("address", ""),
                    createdAt = it.optString("created_at", ""),
                    updatedAt = it.optString("updated_at", "")
                )
            }

            val review = Review(
                id = reviewJson.optInt("id", 0),
                userId = reviewJson.optString("user_id", ""),
                productId = reviewJson.optInt("product_id", 0),
                rating = reviewJson.optInt("rating", 0),
                comment = reviewJson.optString("comment", ""),
                createdAt = reviewJson.optString("created_at", ""),
                user = user
            )
            reviews.add(review)
        }

        return Product(
            id = productJson.optString("id", ""),
            name = productJson.optString("name", ""),
            category = productJson.optString("category", ""),
            price = productJson.optString("price", "0"),
            description = productJson.optString("description", ""),
            imageUrl = productJson.optString("image", ""),
            rating = productJson.optString("average_rating", "0.0").toDouble(),
            totalSales = productJson.optInt("total_sales", 0),
            lastUpdate = productJson.optString("updated_at", ""),
            reviews = reviews // Pass daftar reviews ke produk
        )
    }

}