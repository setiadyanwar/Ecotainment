package com.godongijo.ecotainment.services.product

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.models.SearchHistory
import com.godongijo.ecotainment.utilities.PreferenceManager
import org.json.JSONArray
import org.json.JSONObject

class SearchService {

    fun getSearchHistory(
        context: Context,
        onResult: (List<SearchHistory>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = context.getString(R.string.api_search_history)

        val authToken = PreferenceManager(context).getString("auth_token")

        val request = object : JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    // Ambil status dan pesan dari response
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        // Ambil data search history jika success
                        val dataArray = response.getJSONArray("data")
                        val searchHistoryList = parseSearchHistoryList(dataArray)
                        onResult(searchHistoryList)
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
                Log.e("SearchService", "Error fetching search history: $errorMessage")
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

    // Helper function to parse JSONArray to List<SearchHistory>
    private fun parseSearchHistoryList(dataArray: JSONArray): List<SearchHistory> {
        val searchHistoryList = mutableListOf<SearchHistory>()

        for (i in 0 until dataArray.length()) {
            val searchHistoryJson = dataArray.getJSONObject(i)

            val searchHistory = SearchHistory(
                id = searchHistoryJson.optInt("id", 0),
                userId = searchHistoryJson.optString("user_id", ""),
                searchQuery = searchHistoryJson.optString("search_query", ""),
                createdAt = searchHistoryJson.optString("created_at", ""),
                updatedAt = searchHistoryJson.optString("updated_at", "")
            )

            searchHistoryList.add(searchHistory)
        }

        return searchHistoryList
    }
}
