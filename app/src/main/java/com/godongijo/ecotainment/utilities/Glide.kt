package com.godongijo.ecotainment.utilities

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.godongijo.ecotainment.R

class Glide {

    fun loadImageFromUrl(
        imageView: ImageView,
        imageUrl: String,
        placeholder: ImageView? = null // Menggunakan default parameter sebagai null
    ) {
        Glide
            .with(imageView.context)
            .load(imageUrl)
            .centerCrop()
            .apply {
                // Jika placeholder tidak null, gunakan gambar dari parameter placeholder
                if (placeholder != null) {
                    placeholder(placeholder.drawable) // Gambar placeholder yang dipassing sebagai parameter
                } else {
                    placeholder(R.drawable.ic_tanaman) // Gambar default jika placeholder null
                }
            }
            .into(imageView)
    }


    fun loadImageFitCenter(
        imageView: ImageView,
        imageUrl: String,
        placeholder: ImageView? = null // Menggunakan default parameter sebagai null
    ) {
        Glide
            .with(imageView.context)
            .load(imageUrl)
            .fitCenter()
            .apply {
                // Jika placeholder tidak null, gunakan gambar dari parameter placeholder
                if (placeholder != null) {
                    placeholder(placeholder.drawable) // Gambar placeholder yang dipassing sebagai parameter
                } else {
                    placeholder(R.drawable.ic_tanaman) // Gambar default jika placeholder null
                }
            }
            .into(imageView)
    }
}