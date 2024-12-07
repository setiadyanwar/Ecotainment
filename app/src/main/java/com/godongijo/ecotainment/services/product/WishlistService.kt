package com.godongijo.ecotainment.services.product

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.models.Wishlist
import com.godongijo.ecotainment.utilities.PreferenceManager
import org.json.JSONObject

class WishlistService {

    fun getAllWishlist(
        context: Context,
        onResult: (List<Wishlist>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_wishlist)

        val authToken = PreferenceManager(context).getString("auth_token")

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        val wishlistList = mutableListOf<Wishlist>()

                        // Ambil data wishlist
                        val dataArray = response.getJSONArray("data")

                        for (i in 0 until dataArray.length()) {
                            val wishlistObject = dataArray.getJSONObject(i)

                            // Ambil data wishlist
                            val id = wishlistObject.getInt("id")
                            val userId = wishlistObject.getString("user_id")
                            val productId = wishlistObject.getInt("product_id")
                            val createdAt = wishlistObject.getString("created_at")
                            val updatedAt = wishlistObject.getString("updated_at")

                            // Ambil data produk terkait
                            val productObject = wishlistObject.getJSONObject("product")
                            val product = Product(
                                id = productObject.getInt("id"),
                                name = productObject.getString("name"),
                                price = productObject.getInt("price"),
                                category = productObject.getString("category"),
                                description = productObject.getString("description"),
                                imageUrl = productObject.getString("image"),
                                totalSales = productObject.getInt("total_sales"),
                                createdAt = productObject.getString("created_at"),
                                updatedAt = productObject.getString("updated_at")
                            )

                            // Buat objek Wishlist dan tambahkan ke list
                            val wishlist = Wishlist(
                                id = id,
                                userId = userId,
                                productId = productId,
                                createdAt = createdAt,
                                updatedAt = updatedAt,
                                product = product // Menyertakan detail produk
                            )

                            wishlistList.add(wishlist)
                        }

                        // Kirim hasil wishlist
                        onResult(wishlistList)
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
                Log.e("WishlistService", "Error fetching wishlist: $errorMessage")
                onError(errorMessage)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $authToken"
                return headers
            }
        }

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }


    fun toggleWishlist(
        context: Context,
        productId: Int,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_wishlist) + "/toggle"

        val authToken = PreferenceManager(context).getString("auth_token")

        // Buat data untuk request body
        val requestBody = JSONObject()
        requestBody.put("product_id", productId)

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Jika sukses, kirim pesan berhasil
                        onResult(message)
                    } else {
                        // Jika gagal, kirim pesan error
                        onError(message)
                    }
                } catch (e: Exception) {
                    // Jika ada kesalahan saat memproses JSON
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
                    "Koneksi gagal. Periksa koneksi internet Anda."
                }
                Log.e("WishlistService", "Error fetching wishlist toggle: $errorMessage")
                onError(errorMessage)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $authToken"
                return headers
            }
        }

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }


}