package com.godongijo.ecotainment.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityUpdatePasswordBinding
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.utilities.DialogLoader
import com.godongijo.ecotainment.utilities.ImagePicker

class UpdatePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdatePasswordBinding

    private val authService = AuthService()

    private var dialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdatePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
    }

    private fun setListeners(){
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toggleCurrentPassword.setOnClickListener{ toggleCurrentPassword() }

        binding.toggleNewPassword.setOnClickListener{ toggleNewPassword() }

        binding.buttonSave.setOnClickListener {
            savePassword()
        }
    }

    private fun toggleCurrentPassword() {
        if (binding.inputCurrentPassword.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT) {
            // Jika saat ini dalam mode password (tidak terlihat), ubah menjadi terlihat
            binding.inputCurrentPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.toggleCurrentPassword.setImageResource(R.drawable.ic_visibility_on) // Ubah ikon menjadi "terlihat"
        } else {
            // Jika saat ini dalam mode terlihat, ubah menjadi tidak terlihat
            binding.inputCurrentPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            binding.toggleCurrentPassword.setImageResource(R.drawable.ic_visibility_off) // Ubah ikon menjadi "tidak terlihat"
        }
        // Pindahkan kursor ke akhir teks
        binding.inputCurrentPassword.setSelection(binding.inputCurrentPassword.text?.length ?: 0)
    }

    private fun toggleNewPassword() {
        if (binding.inputNewPassword.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT) {
            // Jika saat ini dalam mode password (tidak terlihat), ubah menjadi terlihat
            binding.inputNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.toggleNewPassword.setImageResource(R.drawable.ic_visibility_on) // Ubah ikon menjadi "terlihat"
        } else {
            // Jika saat ini dalam mode terlihat, ubah menjadi tidak terlihat
            binding.inputNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            binding.toggleNewPassword.setImageResource(R.drawable.ic_visibility_off) // Ubah ikon menjadi "tidak terlihat"
        }
        // Pindahkan kursor ke akhir teks
        binding.inputNewPassword.setSelection(binding.inputNewPassword.text?.length ?: 0)
    }

    private fun savePassword() {
        val currentPassword = binding.inputCurrentPassword.text.toString().trim()
        val newPassword = binding.inputNewPassword.text.toString().trim()

        // Validasi data input
        when {
            currentPassword.isEmpty() -> {
                binding.inputCurrentPassword.error = "Password lama tidak boleh kosong"
                binding.inputCurrentPassword.requestFocus()
                return
            }

            currentPassword.length < 6 -> {
                binding.inputCurrentPassword.error = "Password minimal 6 karakter"
                binding.inputCurrentPassword.requestFocus()
                return
            }

            newPassword.isEmpty() -> {
                binding.inputNewPassword.error = "Password baru tidak boleh kosong"
                binding.inputNewPassword.requestFocus()
                return
            }

            newPassword.length < 6 -> {
                binding.inputNewPassword.error = "Password minimal 6 karakter"
                binding.inputNewPassword.requestFocus()
                return
            }

            else -> {
                dialog = DialogLoader.show(context = this, message = "Mohon tunggu, proses sedang berjalan...") ?: return

                authService.updatePassword(
                    context = this,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    onSuccess = {
                        if (!isFinishing && !isDestroyed) {
                            dialog?.let {
                                DialogLoader.success(it, context = this, message = "Berhasil Mengubah Password")

                                binding.root.postDelayed({
                                    finish()
                                }, 1500L)
                            }
                        }
                    },
                    onError = {errorMessage ->
                        if (!isFinishing && !isDestroyed) {
                            dialog?.let {
                                DialogLoader.error(it, context = this, message = errorMessage)
                            }
                        }
                    }
                )

            }
        }
    }
}