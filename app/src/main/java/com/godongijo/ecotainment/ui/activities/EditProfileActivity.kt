package com.godongijo.ecotainment.ui.activities

import android.app.Dialog
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityEditProfileBinding
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.utilities.DialogLoader
import com.godongijo.ecotainment.utilities.Glide
import com.godongijo.ecotainment.utilities.ImagePicker

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    private val authService = AuthService()

    private var selectedImagePath: String? = null

    private lateinit var imagePicker: ImagePicker

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
        imagePicker = ImagePicker(this)

        initProfileInfo()
    }

    private fun setListeners(){
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonSave.setOnClickListener {
            saveProfile()
        }

        binding.editPicture.setOnClickListener {
            openGallery()
        }
    }


    // Fungsi untuk membuka galeri
    private fun openGallery() {
        imagePicker.pickImage(ImagePicker.ImageSource.GALLERY) { uri ->
            uri?.let { selectedUri ->
                // Dapatkan path
                selectedImagePath = imagePicker.getPathFromUri(this, selectedUri)
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(selectedUri))
                binding.profilePicture.setImageBitmap(bitmap)

            } ?: run {
                Toast.makeText(this, "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initProfileInfo() {
        binding.mainLayout.visibility = View.GONE
        binding.shimmerLayout.visibility = View.VISIBLE

        authService.getUserProfile(
            context = this,
            onResult = { user ->
                if(user.profilePicture != null) {
                    val imageProfile = applicationContext.getString(R.string.base_url) + user.profilePicture
                    Glide().loadImageFromUrl(binding.profilePicture,imageProfile)
                }

                if(user.username != "null") {
                    binding.inputUsername.setText(user.username)
                }

                if(user.email != "null") {
                    binding.inputEmail.setText(user.email)
                }

                if(user.phoneNumber != "null") {
                    binding.inputPhone.setText(user.phoneNumber)
                }

                binding.mainLayout.visibility = View.VISIBLE
                binding.shimmerLayout.visibility = View.GONE
            },
            onError = { error ->
                Log.d("ERROR LOAD ACCOUNT", error)
            }
        )
    }

    private fun saveProfile() {
        val username = binding.inputUsername.text.toString().trim()
        val email = binding.inputEmail.text.toString().trim()
        val phoneNumber = binding.inputPhone.text.toString().trim()

        when {
            username.isEmpty() -> {
                binding.inputUsername.error = "Nama pengguna tidak boleh kosong"
                binding.inputUsername.requestFocus()
                return
            }

            email.isEmpty() -> {
                binding.inputEmail.error = "Email tidak boleh kosong"
                binding.inputEmail.requestFocus()
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.inputEmail.error = "Masukkan email valid"
                binding.inputEmail.requestFocus()
                return
            }
            else -> {
                dialog = DialogLoader.show(context = this, message = "Mohon tunggu, proses sedang berjalan...") ?: return

                authService.editUserProfile(
                    this,
                    username = username,
                    email = email,
                    phoneNumber = phoneNumber,
                    profilePicturePath = selectedImagePath,
                    onSuccess = {
                        if (!isFinishing && !isDestroyed) {
                            dialog?.let {
                                DialogLoader.success(it, context = this, message = "Berhasil Mengedit Profile")

                                binding.root.postDelayed({
                                    onBackPressedDispatcher.onBackPressed()
                                }, 1500L)
                            }
                        }
                    },
                    onError = {
                        if (!isFinishing && !isDestroyed) {
                            dialog?.let {
                                DialogLoader.success(it, context = this, message = "Gagal Mengedit Profile")

                                binding.root.postDelayed({
                                    onBackPressedDispatcher.onBackPressed()
                                }, 1500L)
                            }
                        }
                    }
                )

            }
        }

    }
}