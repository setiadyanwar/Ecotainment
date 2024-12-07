package com.godongijo.ecotainment.services.product

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.models.Review
import com.godongijo.ecotainment.models.User
import com.godongijo.ecotainment.services.AddProductResponse
import com.godongijo.ecotainment.services.UpdateProductResponse
import com.godongijo.ecotainment.utilities.PreferenceManager
import com.godongijo.ecotainment.utilities.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

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
                id = productJson.optInt("id", 0),
                name = productJson.optString("name", ""),
                category = productJson.optString("category", ""),
                price = productJson.optInt("price", 0),
                description = productJson.optString("description", ""),
                imageUrl = productJson.optString("image", ""),
                rating = productJson.optString("average_rating", "0.0").toDoubleOrNull() ?: 0.0, // Default rating
                totalSales = productJson.optInt("total_sales", 0),
                createdAt = productJson.optString("created_at", ""),
                updatedAt = productJson.optString("updated_at", ""),
                reviews = emptyList()
            )

            productList.add(product)
        }

        return productList
    }




    fun getSingleProduct(
        context: Context,
        productId: Int,
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
                    role = it.optString("role", ""),
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
            id = productJson.optInt("id", 0),
            name = productJson.optString("name", ""),
            category = productJson.optString("category", ""),
            price = productJson.optInt("price", 0),
            description = productJson.optString("description", ""),
            imageUrl = productJson.optString("image", ""),
            rating = productJson.optString("average_rating", "0.0").toDouble(),
            totalSales = productJson.optInt("total_sales", 0),
            createdAt = productJson.optString("created_at", ""),
            updatedAt = productJson.optString("updated_at", ""),
            reviews = reviews // Pass daftar reviews ke produk
        )
    }




    // Admin
    fun addNewProduct(
        context: Context,
        productName: String,
        productPrice: Int,
        productCategory: String,
        productDescription: String,
        productImageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            // Convert parameter to RequestBody
            val productNamePart = productName.toRequestBody(MultipartBody.FORM)
            val productPricePart = productPrice.toString().toRequestBody(MultipartBody.FORM)
            val productCategoryPart = productCategory.toRequestBody(MultipartBody.FORM)
            val productDescriptionPart = productDescription.toRequestBody(MultipartBody.FORM)

            // Menyiapkan file gambar produk
            val productImagePart = productImageUri?.let {
                val file = File(getRealPathFromURI(context, it))
                val requestBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("image", file.name, requestBody)
            }

            // Call the API
            RetrofitInstance.apiService.addProduct(
                "Bearer $authToken",
                productNamePart,
                productPricePart,
                productCategoryPart,
                productDescriptionPart,
                productImagePart
            ).enqueue(object : Callback<AddProductResponse> {
                override fun onResponse(call: Call<AddProductResponse>, response: Response<AddProductResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        onSuccess()
                    } else {
                        onError(response.body()?.message ?: "Error adding product")
                    }
                }

                override fun onFailure(call: Call<AddProductResponse>, t: Throwable) {
                    onError(t.message ?: "An error occurred")
                }
            })
        } else {
            onError("Token is missing")
        }
    }


    fun updateProduct(
        context: Context,
        productId: Int,
        productName: String? = null,
        productPrice: Int? = null,
        productCategory: String? = null,
        productDescription: String? = null,
        productImageUri: Uri? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            // Konversi parameter menjadi RequestBody jika tidak null
            val methodPart = "PUT".toRequestBody(MultipartBody.FORM)
            val productNamePart = productName?.toRequestBody(MultipartBody.FORM)
            val productPricePart = productPrice?.toString()?.toRequestBody(MultipartBody.FORM)
            val productCategoryPart = productCategory?.toRequestBody(MultipartBody.FORM)
            val productDescriptionPart = productDescription?.toRequestBody(MultipartBody.FORM)

            // Menyiapkan file gambar produk
            val productImagePart = productImageUri?.let {
                val file = File(getRealPathFromURI(context, it))
                val requestBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("image", file.name, requestBody)
            }

            // Panggil API
            RetrofitInstance.apiService.updateProduct(
                "Bearer $authToken",
                productId,
                methodPart,
                productNamePart,
                productPricePart,
                productCategoryPart,
                productDescriptionPart,
                productImagePart
            ).enqueue(object : Callback<UpdateProductResponse> {
                override fun onResponse(call: Call<UpdateProductResponse>, response: Response<UpdateProductResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        onSuccess()
                    } else {
                        onError(response.body()?.message ?: "Error updating product")
                    }
                }

                override fun onFailure(call: Call<UpdateProductResponse>, t: Throwable) {
                    onError(t.message ?: "An error occurred")
                }
            })
        } else {
            onError("Token is missing")
        }
    }

    fun deleteProduct(
        context: Context,
        productId: Int,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/admin/product/$productId"

        val authToken = PreferenceManager(context).getString("auth_token")

        val request = object : JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Kirim pesan sukses jika berhasil dihapus
                        onResult(message)
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
                Log.e("ProductService", "Error deleting product: $errorMessage")
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

    // Mendapatkan path file dari URI
    private fun getRealPathFromURI(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(MediaStore.Images.Media.DATA)
        val filePath = cursor?.getString(columnIndex ?: 0)
        cursor?.close()
        return filePath ?: ""
    }

}