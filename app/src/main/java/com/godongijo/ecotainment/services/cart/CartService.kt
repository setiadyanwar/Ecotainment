package com.godongijo.ecotainment.services.cart

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.CartItem
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.utilities.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

class CartService {

    fun getCartList(
        context: Context,
        onResult: (List<CartItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_cart)

        val authToken = PreferenceManager(context).getString("auth_token")

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data cart jika success
                        val dataArray = response.getJSONArray("data")
                        val cartList = parseCartList(dataArray)
                        onResult(cartList)
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
                Log.e("CartService", "Error fetching cart: $errorMessage")
                onError(errorMessage)
            }
        ) {
            // Menambahkan header Authorization dengan Bearer token
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

    fun getCartListById(
        context: Context,
        productIds: List<Int>, // Tambahkan parameter daftar ID produk
        onResult: (List<CartItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_cart) + "/filter-by-products"

        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            put("product_ids", JSONArray(productIds)) // Tambahkan product_ids sebagai JSONArray
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data cart jika success
                        val dataArray = response.getJSONArray("data")
                        val cartList = parseCartList(dataArray)
                        onResult(cartList)
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
                Log.e("CartService", "Error fetching cart: $errorMessage")
                onError(errorMessage)
            }
        ) {
            // Menambahkan header Authorization dengan Bearer token
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

    // Helper function to parse JSONArray to List<CartItem>
    private fun parseCartList(dataArray: JSONArray): List<CartItem> {
        val cartList = mutableListOf<CartItem>()

        for (i in 0 until dataArray.length()) {
            val cartItemJson = dataArray.getJSONObject(i)

            val productJson = cartItemJson.optJSONObject("product")
            val product = productJson?.let {
                Product(
                    id = it.optString("id", ""),
                    name = it.optString("name", ""),
                    category = it.optString("category", ""),
                    price = it.optString("price", ""),
                    description = it.optString("description", ""),
                    imageUrl = it.optString("image", ""),
                    rating = it.optString("average_rating", "0.0").toDouble(),
                    totalSales = it.optInt("total_sales", 0),
                    lastUpdate = it.optString("updated_at", ""),
                    reviews = emptyList()
                )
            }

            val cartItem = CartItem(
                id = cartItemJson.optInt("id", 0),
                userId = cartItemJson.optString("user_id", ""),
                productId = cartItemJson.optInt("product_id", 0),
                quantity = cartItemJson.optInt("quantity", 0),
                createdAt = cartItemJson.optString("created_at", ""),
                updatedAt = cartItemJson.optString("updated_at", ""),
                product = product
            )

            cartList.add(cartItem)
        }

        return cartList
    }

    fun addNewCart(
        context: Context,
        productId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_cart)

        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            put("product_id", productId)
            put("quantity", 1)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        onSuccess()
                    } else {
                        onError(message)
                    }
                } catch (e: Exception) {
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
                Log.e("CartService", "Error storing cart: $errorMessage")
                onError(errorMessage)
            }
        ) {
            // Menambahkan header Authorization dengan Bearer token
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

    fun updateQuantity(
        context: Context,
        productId: Int,
        quantity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "${context.getString(R.string.api_cart)}/$productId/quantity"

        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            put("quantity", quantity)
        }

        val request = object : JsonObjectRequest(
            Request.Method.PATCH, url, requestBody,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        onSuccess()
                    } else {
                        onError(message)
                    }
                } catch (e: Exception) {
                    onError("Terjadi kesalahan saat memproses data: ${e.message}")
                }
            },
            { error ->
                val networkResponse = error.networkResponse
                val errorMessage = if (networkResponse?.data != null) {
                    val errorJson = JSONObject(String(networkResponse.data))
                    errorJson.optString("message", "Terjadi kesalahan")
                } else {
                    "Koneksi gagal. Periksa koneksi internet Anda."
                }
                Log.e("CartService", "Error updating quantity: $errorMessage")
                onError(errorMessage)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $authToken"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

    fun deleteCart(
        context: Context,
        productId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "${context.getString(R.string.api_cart)}/$productId"

        val authToken = PreferenceManager(context).getString("auth_token")

        val request = object : JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        onSuccess()
                    } else {
                        onError(message)
                    }
                } catch (e: Exception) {
                    onError("Terjadi kesalahan saat memproses data: ${e.message}")
                }
            },
            { error ->
                val networkResponse = error.networkResponse
                val errorMessage = if (networkResponse?.data != null) {
                    val errorJson = JSONObject(String(networkResponse.data))
                    errorJson.optString("message", "Terjadi kesalahan")
                } else {
                    "Koneksi gagal. Periksa koneksi internet Anda."
                }
                Log.e("CartService", "Error deleting cart: $errorMessage")
                onError(errorMessage)
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $authToken"
                return headers
            }
        }

        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }

}
