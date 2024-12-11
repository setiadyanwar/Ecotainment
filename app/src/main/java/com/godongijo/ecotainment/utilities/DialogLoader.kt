package com.godongijo.ecotainment.utilities

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.DialogLoaderBinding

object DialogLoader {

    fun show(context: Context, message: String): Dialog? {
        if (context is Activity && (context.isFinishing || context.isDestroyed)) {
            return null // Jangan tampilkan dialog jika aktivitas tidak valid
        }
        // Inflate layout menggunakan ViewBinding
        val binding = DialogLoaderBinding.inflate(LayoutInflater.from(context))

        // Atur tampilan awal dialog
        binding.loadingIcon.visibility = View.VISIBLE
        binding.successIcon.visibility = View.GONE
        binding.errorIcon.visibility = View.GONE
        binding.message.text = message

        // Buat dialog dan tampilkan
        val dialog = Dialog(context).apply {
            setContentView(binding.root)
        }

        dialog.show()
        return dialog
    }

    fun success(dialog: Dialog, context: Context, message: String) {
        // Inflate layout untuk binding lokal
        val binding = DialogLoaderBinding.bind(dialog.findViewById(R.id.dialog_loader_root))

        // Update tampilan untuk state selesai
        binding.loadingIcon.visibility = View.GONE
        binding.successIcon.visibility = View.VISIBLE
        binding.errorIcon.visibility = View.GONE
        binding.message.text = message

        // Jalankan animasi pada successIcon
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_animation)
        binding.successIcon.startAnimation(animation)

        // Dismiss dialog setelah 1 detik
        binding.root.postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 1500L)
    }

    fun error(dialog: Dialog, context: Context, message: String) {
        // Inflate layout untuk binding lokal
        val binding = DialogLoaderBinding.bind(dialog.findViewById(R.id.dialog_loader_root))

        // Update tampilan untuk state selesai
        binding.loadingIcon.visibility = View.GONE
        binding.successIcon.visibility = View.GONE
        binding.errorIcon.visibility = View.VISIBLE
        binding.message.text = message
        binding.message.setTextColor(ContextCompat.getColor(context, R.color.warning_300))

        // Jalankan animasi pada successIcon
        val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.pop_up_animation)
        binding.errorIcon.startAnimation(animation)

        // Dismiss dialog setelah 1 detik
        binding.root.postDelayed({
            if (dialog.isShowing) {
                dialog.dismiss()
            }
        }, 2000L)
    }
}