package com.godongijo.ecotainment.services.transaction

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.models.Transaction
import com.godongijo.ecotainment.models.TransactionItem
import com.godongijo.ecotainment.services.UploadProofResponse
import com.godongijo.ecotainment.utilities.PreferenceManager
import com.godongijo.ecotainment.utilities.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class TransactionService {

    fun getTransactionList(
        context: Context,
        onResult: (List<Transaction>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_transactions)

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
                        val transactionList = parseTransactionList(dataArray)
                        onResult(transactionList)
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
                Log.e("TransactionService", "Error fetching data: $errorMessage")
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
    private fun parseTransactionList(dataArray: JSONArray): List<Transaction> {
        val transactionList = mutableListOf<Transaction>()

        for (i in 0 until dataArray.length()) {
            val transactionJson = dataArray.getJSONObject(i)

            // Mengambil array "items" dan memproses setiap item
            val itemsArray = transactionJson.optJSONArray("items")
            val itemList = mutableListOf<TransactionItem>()

            // Memastikan itemsArray tidak null dan melakukan iterasi
            itemsArray?.let {
                for (j in 0 until it.length()) {
                    val itemJson = it.getJSONObject(j)

                    // Parsing product
                    val productJson = itemJson.optJSONObject("product")
                    val product = productJson?.let { prod ->
                        Product(
                            id = prod.optInt("id", 0),
                            name = prod.optString("name", ""),
                            price = prod.optInt("price", 0),
                            category = prod.optString("category", ""),
                            description = prod.optString("description", ""),
                            imageUrl = prod.optString("image", ""),
                            totalSales = prod.optInt("total_sales", 0),
                            createdAt = prod.optString("created_at", ""),
                            updatedAt = prod.optString("updated_at", "")
                        )
                    }

                    // Parsing item
                    val item = TransactionItem(
                        id = itemJson.optInt("id", 0),
                        productId = itemJson.optInt("product_id", 0),
                        quantity = itemJson.optInt("quantity", 0),
                        product = product
                    )

                    itemList.add(item)
                }
            }

            // Membuat objek Transaction dan menambahkan itemList ke dalamnya
            val transaction = Transaction(
                id = transactionJson.optInt("id", 0),
                userId = transactionJson.optString("user_id", ""),
                totalAmount = transactionJson.optInt("total_amount", 0),
                status = transactionJson.optString("status", ""),
                recipientName = transactionJson.optString("recipient_name", ""),
                phoneNumber = transactionJson.optString("phone_number", ""),
                address = transactionJson.optString("address", ""),
                paymentProof = transactionJson.optString("payment_proof", ""),
                items = itemList, // Assign list of items
                createdAt = transactionJson.optString("created_at", ""),
                updatedAt = transactionJson.optString("updated_at", "")
            )

            transactionList.add(transaction)
        }

        return transactionList
    }



    fun addNewTransaction(
        context: Context,
        totalAmount: Int,
        recipientNames: String,
        phoneNumber: String,
        address: String,
        items: List<*>,
        onResult: (Transaction) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_transactions)

        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            put("total_amount", totalAmount)
            // Ubah setiap elemen dalam `items` menjadi JSONObject
            val formattedItems = JSONArray()
            items.forEach { item ->
                if (item is Pair<*, *>) {
                    val productId = item.first as? Int ?: 0
                    val quantity = item.second as? Int ?: 0
                    val jsonObject = JSONObject().apply {
                        put("product_id", productId)
                        put("quantity", quantity)
                    }
                    formattedItems.put(jsonObject)
                }
            }
            put("recipient_name", recipientNames)
            put("phone_number", phoneNumber)
            put("address", address)
            put("items", formattedItems)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        val dataJson = response.getJSONObject("data")
                        val transaction = parseSingleTransaction(dataJson)
                        onResult(transaction)
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
                Log.e("Transaction Service", "Error storing transaction: $errorMessage")
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

    private fun parseSingleTransaction(dataJson: JSONObject): Transaction {
        val itemsArray = dataJson.optJSONArray("items")
        val itemList = mutableListOf<TransactionItem>()

        itemsArray?.let {
            for (i in 0 until it.length()) {
                val itemJson = it.getJSONObject(i)

                val productJson = itemJson.optJSONObject("product")
                val product = productJson?.let { prod ->
                    Product(
                        id = prod.optInt("id", 0),
                        name = prod.optString("name", ""),
                        price = prod.optInt("price", 0),
                        category = prod.optString("category", ""),
                        description = prod.optString("description", ""),
                        imageUrl = prod.optString("image", ""),
                        totalSales = prod.optInt("total_sales", 0),
                        createdAt = prod.optString("created_at", ""),
                        updatedAt = prod.optString("updated_at", "")
                    )
                }

                val item = TransactionItem(
                    id = itemJson.optInt("id", 0),
                    productId = itemJson.optInt("product_id", 0),
                    quantity = itemJson.optInt("quantity", 0),
                    product = product
                )
                itemList.add(item)
            }
        }

        return Transaction(
            id = dataJson.optInt("id", 0),
            userId = dataJson.optString("user_id", ""),
            totalAmount = dataJson.optInt("total_amount", 0),
            status = dataJson.optString("status", ""),
            recipientName = dataJson.optString("recipient_name", ""),
            phoneNumber = dataJson.optString("phone_number", ""),
            address = dataJson.optString("address", ""),
            paymentProof = dataJson.optString("payment_proof", ""),
            items = itemList,
            createdAt = dataJson.optString("created_at", ""),
            updatedAt = dataJson.optString("updated_at", "")
        )
    }



    fun updateTransactionStatus(
        context: Context,
        transactionId: Int,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_transactions) + "/$transactionId/status"
        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            put("status", status)
        }

        val request = object : JsonObjectRequest(
            Request.Method.PATCH, url, requestBody,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        Log.d("TransactionService", "SUCCESS UPDATE TRANSACTION STATUS")
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
                Log.e("Transaction Service", "Error updating transaction status: $errorMessage")
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

    fun uploadProof(
        context: Context,
        transactionId: Int,
        paymentProofImagePath: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            // Konversi URI gambar menjadi file dan request body
            val file = File(paymentProofImagePath)
            val requestBody = file.asRequestBody("image/*".toMediaType())
            val paymentProofPart = MultipartBody.Part.createFormData("payment_proof", file.name, requestBody)

            // Memanggil API upload proof dengan Retrofit
            RetrofitInstance.apiService.uploadPaymentProof(
                authToken = "Bearer $authToken",
                transactionId = transactionId,
                paymentProof = paymentProofPart
            ).enqueue(object : Callback<UploadProofResponse> {
                override fun onResponse(call: Call<UploadProofResponse>, response: Response<UploadProofResponse>) {
                    Log.d("PaymentService", response.body().toString())
                    if (response.isSuccessful && response.body()?.success == true) {
                        onSuccess()
                    } else {
                        onError(response.body()?.message ?: "Error uploading payment proof")
                    }
                }

                override fun onFailure(call: Call<UploadProofResponse>, t: Throwable) {
                    onError(t.message ?: "An error occurred")
                }
            })
        } else {
            onError("Token is missing")
        }
    }

    fun getAllTransactionList(
        context: Context,
        onResult: (List<Transaction>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/admin/transactions"

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
                        val transactionList = parseTransactionList(dataArray)
                        onResult(transactionList)
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
                Log.e("TransactionService", "Error fetching data: $errorMessage")
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

    fun adminUpdateTransactionStatus(
        context: Context,
        transactionId: Int,
        status: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/admin/transactions/$transactionId/status"
        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            put("status", status)
        }

        Log.d("Req Body", requestBody.toString())

        val request = object : JsonObjectRequest(
            Request.Method.PUT, url, requestBody,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        Log.d("TransactionService", "SUCCESS UPDATE TRANSACTION STATUS")
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
                Log.e("Transaction Service", "Error updating transaction status: $errorMessage")
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
        // Check different URI schemes and handle accordingly
        return when {
            // Content URI (most common for gallery and other content providers)
            uri.scheme == "content" -> {
                try {
                    // Try to get the real path using content resolver
                    val projection = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor = context.contentResolver.query(uri, projection, null, null, null)
                    cursor?.use {
                        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        it.moveToFirst()
                        it.getString(columnIndex)
                    } ?: copyFileToAppStorage(context, uri)
                } catch (e: Exception) {
                    // If query fails, copy the file to app's storage
                    copyFileToAppStorage(context, uri)
                }
            }

            // File URI
            uri.scheme == "file" -> uri.path ?: ""

            // Other schemes - try to copy to app storage
            else -> copyFileToAppStorage(context, uri)
        }
    }

    private fun copyFileToAppStorage(context: Context, uri: Uri): String {
        return try {
            // Create a temporary file in app's cache directory
            val file = File(context.cacheDir, "temp_upload_${System.currentTimeMillis()}.jpg")

            // Copy input stream to the new file
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            file.absolutePath
        } catch (e: Exception) {
            Log.e("FileUpload", "Error copying file: ${e.message}")
            ""
        }
    }

}
