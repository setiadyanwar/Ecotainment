package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivitySignInBinding
import com.godongijo.ecotainment.services.auth.AuthService
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding

    // Instance of AuthService for handling authentication
    private val authService = AuthService()

    // Boolean to keep track of whether progress bar is visible
    private var isProgressVisible = false

    // Drawable resources for input fields (normal state and error state)
    private lateinit var normalInput: Drawable
    private lateinit var errorInput: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    private fun init() {
        normalInput = ContextCompat.getDrawable(this, R.drawable.bg_outline_grey)!!
        errorInput = ContextCompat.getDrawable(this, R.drawable.bg_outline_red)!!
    }

    private fun setListeners() {
        binding.togglePassword.setOnClickListener{ togglePassword() }

        binding.buttonSignIn.setOnClickListener{ signIn() }

        binding.gotoSignUp.setOnClickListener{ signUp() }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        listenInput()
    }

    private fun signIn() {
        // Show progress bar
        setProgressBar()

        // Reset input fields and error messages
        binding.layoutInputEmail.background = normalInput
        binding.layoutInputPassword.background = normalInput
        binding.errorInputEmail.visibility = View.GONE
        binding.errorInputPassword.visibility = View.GONE

        val emailOrPhone = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()

        if(isValidData(emailOrPhone, password)) {
            // If data is valid, attempt to sign in using AuthService
            lifecycleScope.launch {
                try {
                    // Sign in using email and password
                    authService.signInWithEmailOrPhone(this@SignInActivity, emailOrPhone, password,
                        onResult = {
                            // Navigate to MainActivity after successful sign-in
                            val intent = Intent(this@SignInActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onError = { error ->
                            binding.errorInputEmail.text = error.toString()
                            binding.errorInputEmail.visibility = View.VISIBLE

                        }
                    )

                    // Hide progress bar
                    setProgressBar()
                } catch (e: Exception) {
                    // Display error message if sign-in fails
                    //binding.errorInputEmail.text = "Terjadi kesalahan saat masuk"
                    binding.errorInputEmail.text = e.message
                    binding.errorInputEmail.visibility = View.VISIBLE

                    // Hide progress bar
                    setProgressBar()
                }
            }
        } else {
            // Hide progress bar if validation fails
            setProgressBar()
        }
    }

    private fun signUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun togglePassword() {
        if (binding.inputPassword.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT) {
            // Jika saat ini dalam mode password (tidak terlihat), ubah menjadi terlihat
            binding.inputPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.togglePassword.setImageResource(R.drawable.ic_visibility_on) // Ubah ikon menjadi "terlihat"
        } else {
            // Jika saat ini dalam mode terlihat, ubah menjadi tidak terlihat
            binding.inputPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            binding.togglePassword.setImageResource(R.drawable.ic_visibility_off) // Ubah ikon menjadi "tidak terlihat"
        }
        // Pindahkan kursor ke akhir teks
        binding.inputPassword.setSelection(binding.inputPassword.text?.length ?: 0)
    }

    private fun listenInput() {
        // Set listener for email input field to reset error state when user starts typing
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.layoutInputEmail.background = normalInput
                binding.errorInputEmail.visibility = View.GONE
                binding.layoutInputPassword.background = normalInput
                binding.errorInputPassword.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Set listener for password input field to reset error state when user starts typing
        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.layoutInputEmail.background = normalInput
                binding.errorInputEmail.visibility = View.GONE
                binding.layoutInputPassword.background = normalInput
                binding.errorInputPassword.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Method to toggle the visibility of the progress bar and enable/disable input fields
    private fun setProgressBar() {
        if (isProgressVisible) {
            // Hide progress bar and enable inputs/buttons
            binding.progressBar.visibility = View.INVISIBLE
            binding.textButton.visibility = View.VISIBLE

            binding.inputEmail.isEnabled = true
            binding.inputPassword.isEnabled = true
            binding.forgetPassword.isEnabled = true
            binding.buttonSignIn.isEnabled = true
            binding.buttonSignInGoogle.isEnabled = true
            binding.gotoSignUp.isEnabled = true

            isProgressVisible = false
        } else {
            // Show progress bar and disable inputs/buttons
            binding.progressBar.visibility = View.VISIBLE
            binding.textButton.visibility = View.INVISIBLE

            binding.inputEmail.isEnabled = false
            binding.inputPassword.isEnabled = false
            binding.forgetPassword.isEnabled = false
            binding.buttonSignIn.isEnabled = false
            binding.buttonSignInGoogle.isEnabled = false
            binding.gotoSignUp.isEnabled = false

            isProgressVisible = true
        }
    }

    // Method to validate the data input
    private fun isValidData(emailOrPhone:String, password:String): Boolean {
        // Check if username is empty
        if (emailOrPhone.isEmpty()) {
            binding.layoutInputEmail.background = errorInput
            binding.errorInputEmail.text = "Harap isi email atau no handphone!"
            binding.errorInputEmail.visibility = View.VISIBLE
            return false
        } // Check if password is empty
        else if (password.isEmpty()) {
            binding.layoutInputPassword.background = errorInput
            binding.errorInputPassword.text = "Harap isi kata sandi!"
            binding.errorInputPassword.visibility = View.VISIBLE
            return false
        } // If all validations pass
        else {
            return true
        }
    }
}
