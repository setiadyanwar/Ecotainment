package com.godongijo.ecotainment.services.auth

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.User
import com.godongijo.ecotainment.utilities.PreferenceManager
import org.json.JSONObject

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
                                val authToken = response.getJSONObject("data").getString("token")

                                // Save userId and authToken to SharedPreferences
                                val preferenceManager = PreferenceManager(context)
                                preferenceManager.putString("user_id", userId)
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
                                val authToken = response.getJSONObject("data").getString("token")

                                // Save userId and authToken to SharedPreferences
                                val preferenceManager = PreferenceManager(context)
                                preferenceManager.putString("user_id", userId)
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

                                    // Parsing JSON response menjadi objek User
                                    val user = User(
                                        id = data.getString("id"),
                                        email = data.optString("email", ""),
                                        username = data.optString("username", "Anonymous"),
                                        phoneNumber = data.optString("phone_number", ""),
                                        profilePicture = data.optString("profile_picture", ""),
                                        address = data.optString("address", ""),
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

}