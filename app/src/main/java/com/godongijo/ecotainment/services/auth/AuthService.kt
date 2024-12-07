package com.godongijo.ecotainment.services.auth

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Address
import com.godongijo.ecotainment.models.User
import com.godongijo.ecotainment.services.UpdateResponse
import com.godongijo.ecotainment.utilities.PreferenceManager
import com.godongijo.ecotainment.utilities.RetrofitInstance
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AuthService {

    // Function to detect if input is an email or phone number
    private fun isEmail(input: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(input).matches()
    }

    fun signInWithEmailOrPhone(
        context: Context,
        input: String,
        password: String,
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_sign_in)

        val requestBody = if (isEmail(input)) {
            JSONObject().apply {
                put("email", input)
                put("password", password)
            }
        } else {
            JSONObject().apply {
                put("phone_number", input)
                put("password", password)
            }
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    // Ambil status code dari response
                    val statusCode = response.optInt("status_code", 200)

                    // Sesuaikan dengan status code
                    when (statusCode) {
                        200 -> {
                            val success = response.getBoolean("success")
                            val message = response.getString("message")
                            if (success) {
                                val userId = response.getJSONObject("data").getJSONObject("user").getString("id")
                                val role = response.getJSONObject("data").getJSONObject("user").getString("role")
                                val authToken = response.getJSONObject("data").getString("token")

                                // Save userId and authToken to SharedPreferences
                                val preferenceManager = PreferenceManager(context)
                                preferenceManager.putString("user_id", userId)
                                preferenceManager.putString("role", role)
                                preferenceManager.putString("auth_token", authToken)

                                onResult(userId)
                            } else {
                                onError(message)
                            }
                        }
                        422 -> {
                            // Untuk status code 422, mungkin validasi gagal
                            val message = response.optString("message", "Validasi gagal")
                            onError(message)
                        }
                        401 -> {
                            // Untuk status code 401, kesalahan otentikasi
                            val message = response.optString("message", "Email/No Telepon atau password salah")
                            onError(message)
                        }
                        500 -> {
                            // Untuk status code 500, error server
                            val message = response.optString("message", "Terjadi kesalahan pada server")
                            onError(message)
                        }
                        else -> {
                            // Untuk status code lainnya
                            val message = response.optString("message", "Terjadi kesalahan tak terduga")
                            onError(message)
                        }
                    }
                } catch (e: Exception) {
                    onError(e.message.toString())
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
                Log.e("AuthService", "Sign-in error: $errorMessage")
                onError(errorMessage)
            }
        ) {}

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }


    // Create new user
    fun signUpWithEmailAndPassword(
        context: Context,
        email: String,
        password: String,
        onResult: (String?) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_sign_up)
        val requestBody = JSONObject().apply {
            put("email", email)
            put("password", password)
        }

        val request = object : JsonObjectRequest(
            Request.Method.POST, url, requestBody,
            { response ->
                try {
                    // Ambil status code dari response
                    val statusCode = response.optInt("status_code", 201)

                    // Sesuaikan dengan status code
                    when (statusCode) {
                        201 -> {
                            val success = response.getBoolean("success")
                            val message = response.getString("message")
                            if (success) {
                                val userId = response.getJSONObject("data").getJSONObject("user").getString("id")
                                val role = response.getJSONObject("data").getJSONObject("user").getString("role")
                                val authToken = response.getJSONObject("data").getString("token")

                                // Save userId and authToken to SharedPreferences
                                val preferenceManager = PreferenceManager(context)
                                preferenceManager.putString("user_id", userId)
                                preferenceManager.putString("role", role)
                                preferenceManager.putString("auth_token", authToken)

                                onResult(userId)
                            } else {
                                onError(message)
                            }
                        }
                        422 -> {
                            // Untuk status code 422, validasi gagal
                            val message = response.optString("message", "Validasi gagal")
                            onError(message)
                        }
                        500 -> {
                            // Untuk status code 500, error server
                            val message = response.optString("message", "Terjadi kesalahan pada server")
                            onError(message)
                        }
                        else -> {
                            // Untuk status code lainnya
                            val message = response.optString("message", "Terjadi kesalahan tak terduga")
                            onError(message)
                        }
                    }
                } catch (e: Exception) {
                    onError(e.message.toString())
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
                Log.e("AuthService", "Sign-up error: $errorMessage")
                onError(errorMessage)
            }
        ) {}

        // Add request to the request queue
        val requestQueue = Volley.newRequestQueue(context)
        requestQueue.add(request)
    }


    // Sign out user
    fun signOut(
        context: Context,
        onResult: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            val url = context.getString(R.string.api_logout)

            // Request kosong untuk logout
            val requestBody = JSONObject()

            val request = object : JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                { response ->
                    try {
                        // Ambil status code dari response
                        val statusCode = response.optInt("status_code", 200)

                        // Sesuaikan dengan status code
                        when (statusCode) {
                            200 -> {
                                val success = response.getBoolean("success")
                                val message = response.getString("message")
                                if (success) {
                                    // Clear preferences if logout is successful
                                    val preferenceManager = PreferenceManager(context)
                                    preferenceManager.clear()
                                    onResult()
                                } else {
                                    onError(message)
                                }
                            }
                            401 -> {
                                // Token tidak valid atau sudah tidak aktif
                                val message = response.optString("message", "Token tidak valid atau sudah tidak aktif")
                                onError(message)
                            }
                            500 -> {
                                // Error server
                                val message = response.optString("message", "Terjadi kesalahan saat proses logout")
                                onError(message)
                            }
                            else -> {
                                // Status code lainnya
                                val message = response.optString("message", "Terjadi kesalahan tak terduga")
                                onError(message)
                            }
                        }
                    } catch (e: Exception) {
                        onError(e.message.toString())
                    }
                },
                { error ->
                    Log.e("AuthService", "Logout error: ${error.message}")
                    onError(error.message.toString())
                }
            ) {
                // Add Bearer token in header
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $authToken"
                    return headers
                }
            }

            // Add request to the request queue
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(request)
        } else {
            onError("Token is missing")
        }
    }

    fun getUserProfile(
        context: Context,
        onResult: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            val url = context.getString(R.string.api_user)

            val request = object : JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        val statusCode = response.optInt("status_code", 200)

                        when (statusCode) {
                            200 -> {
                                val success = response.getBoolean("success")
                                val message = response.getString("message")
                                if (success) {
                                    val data = response.getJSONObject("data")

                                    // Parsing `addresses` menjadi List<Address>
                                    val addresses = mutableListOf<Address>()
                                    val addressArray = data.optJSONArray("addresses")
                                    if (addressArray != null) {
                                        for (i in 0 until addressArray.length()) {
                                            val addressObject = addressArray.getJSONObject(i)
                                            val address = Address(
                                                id = addressObject.getInt("id"),
                                                recipientName = addressObject.optString("recipient_name", ""),
                                                phoneNumber = addressObject.optString("phone_number", ""),
                                                province = addressObject.optString("province", ""),
                                                cityOrDistrict = addressObject.optString("city_or_district", ""),
                                                detailAddress = addressObject.optString("detailed_address"),
                                                createdAt = addressObject.optString("created_at", ""),
                                                updatedAt = addressObject.optString("updated_at", "")
                                            )
                                            addresses.add(address)
                                        }
                                    }

                                    // Parsing JSON response menjadi objek User
                                    val user = User(
                                        id = data.getString("id"),
                                        email = data.optString("email", ""),
                                        username = data.optString("username", "Anonymous"),
                                        phoneNumber = data.optString("phone_number", ""),
                                        profilePicture = data.optString("profile_picture", ""),
                                        role = data.optString("role", ""),
                                        address = addresses,
                                        createdAt = data.optString("created_at", ""),
                                        updatedAt = data.optString("updated_at", "")
                                    )

                                    onResult(user) // Mengembalikan objek User
                                } else {
                                    onError(message)
                                }
                            }
                            500 -> {
                                // Error server
                                val message = response.optString("message", "Terjadi kesalahan pada server")
                                onError(message)
                            }
                            else -> {
                                val message = response.optString("message", "Terjadi kesalahan tak terduga")
                                onError(message)
                            }
                        }
                    } catch (e: Exception) {
                        onError(e.message.toString())
                    }
                },
                { error ->
                    Log.e("AuthService", "Get user profile error: ${error.message}")
                    onError(error.message.toString())
                }
            ) {
                // Menambahkan token authorization di header
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $authToken"
                    return headers
                }
            }

            // Menambahkan request ke dalam request queue
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(request)
        } else {
            onError("Token is missing")
        }
    }

    fun editUserProfile(
        context: Context,
        email: String? = null,
        password: String? = null,
        username: String? = null,
        phoneNumber: String? = null,
        profilePictureUri: Uri? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            val methodPart = "PUT".toRequestBody(MultipartBody.FORM)
            val emailPart = email?.toRequestBody(MultipartBody.FORM)
            val passwordPart = password?.toRequestBody(MultipartBody.FORM)
            val usernamePart = username?.toRequestBody(MultipartBody.FORM)
            val phoneNumberPart = phoneNumber?.toRequestBody(MultipartBody.FORM)

            // Menyiapkan file profile picture jika ada
            val profilePicturePart = profilePictureUri?.let {
                val file = File(getRealPathFromURI(context, it))
                val requestBody = file.asRequestBody("image/*".toMediaType())
                MultipartBody.Part.createFormData("profile_picture", file.name, requestBody)
            }

            RetrofitInstance.apiService.updateProfile(
                "Bearer $authToken",
                methodPart,
                emailPart,
                passwordPart,
                usernamePart,
                phoneNumberPart,
                profilePicturePart
            ).enqueue(object : Callback<UpdateResponse> {
                override fun onResponse(call: Call<UpdateResponse>, response: Response<UpdateResponse>) {
                    Log.d("AuthService", response.body().toString())
                    if (response.isSuccessful && response.body()?.success == true) {
                        onSuccess()
                    } else {
                        onError(response.body()?.message ?: "Error updating profile")
                    }
                }

                override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                    onError(t.message ?: "An error occurred")
                }
            })
        } else {
            onError("Token is missing")
        }
    }



    fun getUserAddress(
        context: Context,
        onResult: (List<Address>) -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            val url = context.getString(R.string.base_url) + "/api/auth/address"

            val request = object : JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    try {
                        val statusCode = response.optInt("status_code", 200)

                        when (statusCode) {
                            200 -> {
                                val success = response.getBoolean("success")
                                val message = response.getString("message")
                                if (success) {
                                    val addressList = mutableListOf<Address>()

                                    // Parsing JSON array data
                                    val addresses = response.getJSONArray("data")
                                    for (i in 0 until addresses.length()) {
                                        val addressObject = addresses.getJSONObject(i)
                                        val address = Address(
                                            id = addressObject.getInt("id"),
                                            recipientName = addressObject.optString("recipient_name", ""),
                                            phoneNumber = addressObject.optString("phone_number", ""),
                                            province = addressObject.optString("province", ""),
                                            cityOrDistrict = addressObject.optString("city_or_district", ""),
                                            detailAddress = addressObject.optString("detailed_address"),
                                            createdAt = addressObject.optString("created_at", ""),
                                            updatedAt = addressObject.optString("updated_at", "")
                                        )
                                        addressList.add(address)
                                    }

                                    onResult(addressList) // Mengembalikan daftar alamat
                                } else {
                                    onError(message)
                                }
                            }
                            500 -> {
                                // Error server
                                val message = response.optString("message", "Terjadi kesalahan pada server")
                                onError(message)
                            }
                            else -> {
                                val message = response.optString("message", "Terjadi kesalahan tak terduga")
                                onError(message)
                            }
                        }
                    } catch (e: Exception) {
                        onError(e.message.toString())
                    }
                },
                { error ->
                    Log.e("AuthService", "Get user address error: ${error.message}")
                    onError(error.message.toString())
                }
            ) {
                // Menambahkan token authorization di header
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $authToken"
                    return headers
                }
            }

            // Menambahkan request ke dalam request queue
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(request)
        } else {
            onError("Token is missing")
        }
    }

    fun addAddress(
        context: Context,
        recipientName: String,
        phoneNumber: String,
        province: String,
        city: String,
        fullAddress: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val authToken = PreferenceManager(context).getString("auth_token")

        if (!authToken.isNullOrEmpty()) {
            val url = context.getString(R.string.base_url) + "/api/auth/address"

            val requestBody = JSONObject().apply {
                put("recipient_name", recipientName)
                put("phone_number", phoneNumber)
                put("province", province)
                put("city_or_district", city)
                put("detailed_address", fullAddress)
            }

            val request = object : JsonObjectRequest(
                Request.Method.POST, url, requestBody,
                { response ->
                    if (response.getBoolean("success")) {
                        onSuccess()
                    } else {
                        onError(response.getString("message"))
                    }
                },
                { error ->
                    Log.e("AuthService", "Get user address error: ${error.message}")
                    onError(error.message.toString())
                }
            ) {
                // Menambahkan token authorization di header
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $authToken"
                    return headers
                }
            }

            // Menambahkan request ke dalam request queue
            val requestQueue = Volley.newRequestQueue(context)
            requestQueue.add(request)
        } else {
            onError("Token is missing")
        }
    }

    fun editAddress(
        context: Context,
        addressId: Int,
        recipientName: String? = null,
        phoneNumber: String? = null,
        province: String? = null,
        city: String? = null,
        fullAddress: String? = null,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/auth/address/$addressId"

        val authToken = PreferenceManager(context).getString("auth_token")

        // Body request
        val requestBody = JSONObject().apply {
            recipientName?.let { put("recipient_name", it) }
            phoneNumber?.let { put("phone_number", it) }
            province?.let { put("province", it) }
            city?.let { put("city_or_district", it) }
            fullAddress?.let { put("detailed_address", it) }
        }

        val request = object : JsonObjectRequest(
            Request.Method.PUT, url, requestBody,
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
                Log.e("Auth Service", "Error updating address: $errorMessage")
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




    fun deleteAddress(
        context: Context,
        addressId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.base_url) + "/api/auth/address/$addressId"

        val authToken = PreferenceManager(context).getString("auth_token")

        val request = object : JsonObjectRequest(
            Request.Method.DELETE, url, null,
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
                Log.e("Auth Service", "Error updating address: $errorMessage")
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