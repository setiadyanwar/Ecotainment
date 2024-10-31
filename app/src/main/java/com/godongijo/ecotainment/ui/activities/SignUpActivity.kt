package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding

    // Drawable resources for input fields (normal state and error state)
    private lateinit var normalInput: Drawable
    private lateinit var errorInput: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
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

        binding.toggleConfirmPassword.setOnClickListener{ toggleConfirmPassword() }

        binding.buttonSignUp.setOnClickListener{ signUp() }

        binding.gotoSignIn.setOnClickListener{ signIn() }

        listenInput()
    }

    private fun signUp() {
        // Reset input fields and error messages
        binding.layoutInputEmail.background = normalInput
        binding.layoutInputPassword.background = normalInput
        binding.layoutInputConfirmPassword.background = normalInput
        binding.errorInputEmail.visibility = View.GONE
        binding.errorInputPassword.visibility = View.GONE
        binding.errorInputConfirmPassword.visibility = View.GONE

        val email = binding.inputEmail.text.toString().trim()
        val password = binding.inputPassword.text.toString().trim()
        val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

        if(isValidData(email, password, confirmPassword)) {
            val toast = Toast.makeText(this, "Berhasil Daftar", Toast.LENGTH_SHORT) // in Activity
            toast.show()
        }

    }

    private fun signIn() {
        val intent = Intent(this, SignInActivity::class.java)
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

    private fun toggleConfirmPassword() {
        if (binding.inputConfirmPassword.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT) {
            // Jika saat ini dalam mode password (tidak terlihat), ubah menjadi terlihat
            binding.inputConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.toggleConfirmPassword.setImageResource(R.drawable.ic_visibility_on) // Ubah ikon menjadi "terlihat"
        } else {
            // Jika saat ini dalam mode terlihat, ubah menjadi tidak terlihat
            binding.inputConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
            binding.toggleConfirmPassword.setImageResource(R.drawable.ic_visibility_off) // Ubah ikon menjadi "tidak terlihat"
        }
        // Pindahkan kursor ke akhir teks
        binding.inputConfirmPassword.setSelection(binding.inputConfirmPassword.text?.length ?: 0)
    }

    private fun listenInput() {
        // Set listener for email input field to reset error state when user starts typing
        binding.inputEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.layoutInputEmail.background = normalInput
                binding.errorInputEmail.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Set listener for password input field to reset error state when user starts typing
        binding.inputPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.layoutInputPassword.background = normalInput
                binding.errorInputPassword.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Set listener for confirmPassword input field to reset error state when user starts typing
        binding.inputConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.layoutInputConfirmPassword.background = normalInput
                binding.errorInputConfirmPassword.visibility = View.GONE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // Method to validate the data input
    private fun isValidData(email:String, password:String, confirmPassword:String): Boolean {
        // Check if username is empty
        if (email.isEmpty()) {
            binding.layoutInputEmail.background = errorInput
            binding.errorInputEmail.text = "Harap isi email anda!"
            binding.errorInputEmail.visibility = View.VISIBLE
            return false
        } // Check if email format is valid
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.layoutInputEmail.background = errorInput
            binding.errorInputEmail.text = "Harap masukkan email valid!"
            binding.errorInputEmail.visibility = View.VISIBLE
            return false
        } // Check if password is empty
        else if (password.isEmpty()) {
            binding.layoutInputPassword.background = errorInput
            binding.errorInputPassword.text = "Harap isi kata sandi!"
            binding.errorInputPassword.visibility = View.VISIBLE
            return false
        } // Check if confirmPassword is empty
        else if (confirmPassword.isEmpty()) {
            binding.layoutInputConfirmPassword.background = errorInput
            binding.errorInputConfirmPassword.text = "Harap konfirmasi kata sandi!"
            binding.errorInputConfirmPassword.visibility = View.VISIBLE
            return false
        } // Check if password and confirmPassword match
        else if (confirmPassword != password) {
            binding.layoutInputConfirmPassword.background = errorInput
            binding.errorInputConfirmPassword.text = "Kata sandi dan konfirmasi tidak sesuai"
            binding.errorInputConfirmPassword.visibility = View.VISIBLE
            return false
        } // If all validations pass
        else {
            return true
        }
    }
}