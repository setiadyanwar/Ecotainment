package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityEditProfileBinding
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.utilities.Glide
import java.text.SimpleDateFormat
import java.util.Locale

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding

    private val authService = AuthService()

    private var selectedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
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

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedImageUri ->
            selectedUri = selectedImageUri
            // Menampilkan gambar yang dipilih
            binding.profilePicture.setImageURI(selectedImageUri)
        }
    }

    // Fungsi untuk membuka galeri
    private fun openGallery() {
        // Memilih tipe konten gambar
        galleryLauncher.launch("image/*")
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

        authService.editUserProfile(
            this,
            username = username,
            email = email,
            phoneNumber = phoneNumber,
            profilePictureUri = selectedUri,
            onSuccess = {
                Toast.makeText(this, "Berhasil update Profile", Toast.LENGTH_SHORT).show()
                onBackPressedDispatcher.onBackPressed()
            },
            onError = { error ->
                Log.d("ERROR LOAD ACCOUNT", error)
            }
        )
    }
}