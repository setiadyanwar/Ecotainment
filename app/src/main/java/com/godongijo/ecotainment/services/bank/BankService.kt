package com.godongijo.ecotainment.services.bank

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Bank
import com.godongijo.ecotainment.models.Product
import com.godongijo.ecotainment.models.Review
import com.godongijo.ecotainment.models.User
import com.godongijo.ecotainment.services.ApiResponse
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
import java.io.FileOutputStream

class BankService {
    fun getBankList(
        context: Context,
        onResult: (List<Bank>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/bank"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data bank jika success
                        val dataArray = response.getJSONArray("data")
                        val bankList = parseBankList(dataArray)
                        onResult(bankList)
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
                Log.e("BankService", "Error fetching banks: $errorMessage")
                onError(errorMessage)
            }
        ) {}

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }


    fun getSingleBank(
        context: Context,
        bankId: Int,
        onResult: (Bank) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/bank/$bankId"

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data produk jika success
                        val bankData = response.getJSONObject("data")
                        val bank = parseSingleBank(bankData)
                        onResult(bank)
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


    // Helper function to parse JSONArray to List<Bank>
    private fun parseBankList(dataArray: JSONArray): List<Bank> {
        val bankList = mutableListOf<Bank>()

        for (i in 0 until dataArray.length()) {
            val bankJson = dataArray.getJSONObject(i)

            val bank = Bank(
                id = bankJson.optInt("id", 0),
                name = bankJson.optString("name", ""),
                logo = bankJson.optString("logo", ""),
                accountNumber = bankJson.optString("account_number", ""),
                accountHolder = bankJson.optString("account_holder", ""),
                paymentInstructions = bankJson.optString("payment_instructions", ""),
                createdAt = bankJson.optString("created_at", ""),
                updatedAt = bankJson.optString("updated_at", "")
            )

            bankList.add(bank)
        }

        return bankList
    }

    private fun parseSingleBank(bankData: JSONObject): Bank {
        return Bank(
            id = bankData.optInt("id"),
            name = bankData.optString("name"),
            logo = bankData.optString("logo"),
            accountNumber = bankData.optString("account_number"),
            accountHolder = bankData.optString("account_holder"),
            paymentInstructions = bankData.optString("payment_instructions"),
            createdAt = bankData.optString("created_at"),
            updatedAt = bankData.optString("updated_at")
        )
    }

    fun addNewBank(
        context: Context,
        name: String,
        logoResId: Int?,
        qrImageUri: Uri? = null,
        accountNumber: String,
        accountHolder: String,
        paymentInstructions: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            // Convert parameter to RequestBody
            val namePart = name.toRequestBody(MultipartBody.FORM)
            val accountNumberPart = accountNumber.toRequestBody(MultipartBody.FORM)
            val accountHolderPart = accountHolder.toRequestBody(MultipartBody.FORM)
            val paymentInstructionsPart = paymentInstructions.toRequestBody(MultipartBody.FORM)

            val logoOrQrImagePart = if (name == "QRIS" && qrImageUri != null) {
                val file = File(getRealPathFromURI(context, qrImageUri))
                val requestBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("logo", file.name, requestBody)
            } else {
                logoResId?.let {
                    val drawable = context.resources.getDrawable(it, null)
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val file = File(context.cacheDir, "logo_${System.currentTimeMillis()}.png")
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    val requestBody = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("logo", file.name, requestBody)
                }
            }

            // Call the API
            RetrofitInstance.apiService.addBank(
                "Bearer $authToken",
                namePart,
                logoOrQrImagePart,
                accountNumberPart,
                accountHolderPart,
                paymentInstructionsPart
            ).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        onSuccess()
                    } else {
                        onError(response.body()?.message ?: "Error adding bank")
                        Log.d("Bank Service", "Error Adding Bank: ${response.body()?.message}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    onError(t.message ?: "An error occurred")
                }
            })
        } else {
            onError("Token is missing")
        }
    }


    fun updateBank(
        context: Context,
        id: Int, // ID bank yang akan diperbarui
        name: String? = null,
        logoResId: Int? = null, // ID dari drawable
        qrImageUri: Uri? = null,
        accountNumber: String? = null,
        accountHolder: String? = null,
        paymentInstructions: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            // Convert parameter to RequestBody
            val methodPart = "PUT".toRequestBody(MultipartBody.FORM)
            val namePart = name?.toRequestBody(MultipartBody.FORM)
            val accountNumberPart = accountNumber?.toRequestBody(MultipartBody.FORM)
            val accountHolderPart = accountHolder?.toRequestBody(MultipartBody.FORM)
            val paymentInstructionsPart = paymentInstructions?.toRequestBody(MultipartBody.FORM)

            val logoOrQrImagePart = if (name == "QRIS" && qrImageUri != null) {
                val file = File(getRealPathFromURI(context, qrImageUri))
                val requestBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("logo", file.name, requestBody)
            } else {
                logoResId?.let {
                    val drawable = context.resources.getDrawable(it, null)
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    val file = File(context.cacheDir, "logo_${System.currentTimeMillis()}.png")
                    val outputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()

                    val requestBody = file.asRequestBody("image/*".toMediaType())
                    MultipartBody.Part.createFormData("logo", file.name, requestBody)
                }
            }


            // Call the API
            RetrofitInstance.apiService.updateBank(
                "Bearer $authToken",
                id, // bankId di URL
                methodPart, // Menambahkan _method PUT di body
                namePart,
                logoOrQrImagePart,
                accountNumberPart,
                accountHolderPart,
                paymentInstructionsPart
            ).enqueue(object : Callback<ApiResponse> {
                override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        onSuccess()
                    } else {
                        onError(response.body()?.message ?: "Error updating bank")
                        Log.d("Bank Service", "Error Update Bank: ${response.body()?.message}")
                    }
                }

                override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                    Log.d("Bank Service", "Error Update Bank: ${t.message}")
                    onError(t.message ?: "An error occurred")
                }
            })
        } else {
            onError("Token is missing")
        }
    }


    fun deleteBank(
        context: Context,
        bankId: Int,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/admin/banks/$bankId"

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
                Log.e("BankService", "Error deleting bank: $errorMessage")
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