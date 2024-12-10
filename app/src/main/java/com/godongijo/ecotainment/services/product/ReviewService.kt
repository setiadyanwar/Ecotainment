package com.godongijo.ecotainment.services.product

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.Review
import com.godongijo.ecotainment.utilities.PreferenceManager
import org.json.JSONObject

class ReviewService {
    fun addProductReviews(
        context: Context,
        reviews: List<Map<String, Any>>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_reviews)

        val authToken = PreferenceManager(context).getString("auth_token")

        // Process each review in the list
        reviews.forEach { reviewData ->
            val productId = reviewData["productId"] as? Int ?: 0
            val rating = reviewData["rating"] as? Int ?: 0
            val comment = reviewData["comment"] as? String

            if (rating > 0) {
                // Prepare the request body
                val requestBody = JSONObject().apply {
                    put("rating", rating)
                    put("comment", comment ?: JSONObject.NULL)
                }

                val request = object : JsonObjectRequest(
                    Request.Method.POST, "$url/$productId", requestBody,
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
                        Log.e("Review Service", "Error storing review: $errorMessage")
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
        }
    }

}