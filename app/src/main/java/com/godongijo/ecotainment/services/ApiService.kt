package com.godongijo.ecotainment.services

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @Multipart
    @POST("api/auth/profile")
    fun updateProfile(
        @Header("Authorization") authToken: String,
        @Part("_method") method: RequestBody?,
        @Part("email") email: RequestBody?,
        @Part("password") password: RequestBody?,
        @Part("username") username: RequestBody?,
        @Part("phone_number") phoneNumber: RequestBody?,
        @Part profilePicture: MultipartBody.Part?
    ): Call<UpdateResponse>

    @Multipart
    @POST("api/transactions/{transactionId}/proof")
    fun uploadPaymentProof(
        @Header("Authorization") authToken: String,
        @Path("transactionId") transactionId: Int,
        @Part paymentProof: MultipartBody.Part
    ): Call<UploadProofResponse>

    @Multipart
    @POST("api/admin/product")
    fun addProduct(
        @Header("Authorization") authToken: String,
        @Part("name") productName: RequestBody,
        @Part("price") productPrice: RequestBody,
        @Part("category") productCategory: RequestBody,
        @Part("description") productDescription: RequestBody,
        @Part productImage: MultipartBody.Part?
    ): Call<AddProductResponse>

    @Multipart
    @POST("api/admin/product/{id}")
    fun updateProduct(
        @Header("Authorization") authToken: String,
        @Path("id") productId: Int,
        @Part("_method") method: RequestBody,
        @Part("name") productName: RequestBody?,
        @Part("price") productPrice: RequestBody?,
        @Part("category") productCategory: RequestBody?,
        @Part("description") productDescription: RequestBody?,
        @Part productImage: MultipartBody.Part?
    ): Call<UpdateProductResponse>


}

// UpdateResponse.kt
data class UpdateResponse(
    val success: Boolean,
    val message: String
)

data class UploadProofResponse(
    val success: Boolean,
    val message: String
)

data class AddProductResponse(
    val success: Boolean,
    val message: String
)

data class UpdateProductResponse(
    val success: Boolean,
    val message: String
)

