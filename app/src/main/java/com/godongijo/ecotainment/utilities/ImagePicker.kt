package com.godongijo.ecotainment.utilities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ImagePicker(private val activity: ComponentActivity) {

    // Pilihan sumber gambar
    enum class ImageSource {
        GALLERY, CAMERA
    }

    // Callback untuk hasil pemilihan gambar
    private var onImagePickedCallback: ((Uri?) -> Unit)? = null
    private var currentPhotoPath: String? = null

    // Launcher untuk memilih gambar dari galeri
    private val galleryLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageUri = result.data?.data
                onImagePickedCallback?.invoke(imageUri)
            } else {
                onImagePickedCallback?.invoke(null)
            }
        }

    // Launcher untuk mengambil foto dari kamera
    private val cameraLauncher: ActivityResultLauncher<Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                currentPhotoPath?.let { path ->
                    val imageFile = File(path)
                    val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        FileProvider.getUriForFile(
                            activity,
                            "${activity.packageName}.fileprovider",
                            imageFile
                        )
                    } else {
                        Uri.fromFile(imageFile)
                    }
                    onImagePickedCallback?.invoke(imageUri)
                }
            } else {
                onImagePickedCallback?.invoke(null)
            }
        }

    // Launcher untuk permintaan izin
    private val permissionLauncher: ActivityResultLauncher<String> =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> openImageSource()
                else -> onImagePickedCallback?.invoke(null)
            }
        }

    /**
     * Memilih gambar dari sumber tertentu
     * @param source Sumber gambar (Galeri atau Kamera)
     * @param callback Fungsi callback untuk hasil pemilihan
     */
    fun pickImage(
        source: ImageSource = ImageSource.GALLERY,
        callback: (Uri?) -> Unit
    ) {
        onImagePickedCallback = callback

        // Periksa izin berdasarkan sumber dan versi Android
        val permission = when (source) {
            ImageSource.CAMERA -> Manifest.permission.CAMERA
            ImageSource.GALLERY -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                Manifest.permission.READ_MEDIA_IMAGES
            else
                Manifest.permission.READ_EXTERNAL_STORAGE
        }

        // Cek dan minta izin
        when {
            ContextCompat.checkSelfPermission(activity, permission) ==
                    PackageManager.PERMISSION_GRANTED -> {
                openImageSource(source)
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    /**
     * Membuka sumber gambar sesuai pilihan
     * @param source Sumber gambar yang dipilih
     */
    private fun openImageSource(source: ImageSource = ImageSource.GALLERY) {
        when (source) {
            ImageSource.GALLERY -> openGallery()
            ImageSource.CAMERA -> openCamera()
        }
    }

    /**
     * Membuka galeri untuk memilih gambar
     */
    private fun openGallery() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Gunakan Photo Picker untuk Android 13+
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "image/*"
            }
        } else {
            // Untuk versi sebelumnya
            Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                type = "image/*"
            }
        }
        galleryLauncher.launch(intent)
    }

    /**
     * Membuka kamera untuk mengambil foto
     */
    private fun openCamera() {
        val photoFile = createImageFile()
        currentPhotoPath = photoFile.absolutePath

        val photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                photoFile
            )
        } else {
            Uri.fromFile(photoFile)
        }

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        }
        cameraLauncher.launch(cameraIntent)
    }

    /**
     * Membuat file gambar sementara
     * @return File gambar yang baru dibuat
     */
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    /**
     * Mendapatkan path file dari URI
     * @param context Konteks aplikasi
     * @param uri URI gambar
     * @return Path file gambar
     */
    fun getPathFromUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.getString(columnIndex)
            } else null
        }
    }

    /**
     * Mendapatkan nama file dari URI
     * @param context Konteks aplikasi
     * @param uri URI gambar
     * @return Nama file gambar
     */
    fun getFileName(context: Context, uri: Uri): String? {
        // Coba metode pertama: query dari MediaStore
        val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                return cursor.getString(columnIndex)
            }
        }

        // Coba metode kedua: query dari OpenableColumns
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                cursor.moveToFirst()
                return cursor.getString(nameIndex)
            }
        }

        // Metode terakhir: ekstrak dari path
        return getPathFromUri(context, uri)?.let { path ->
            File(path).name
        }
    }

    /**
     * Mendapatkan ukuran file dari URI
     * @param context Konteks aplikasi
     * @param uri URI gambar
     * @return Ukuran file dalam bytes
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        // Coba metode pertama: query dari OpenableColumns
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex != -1) {
                cursor.moveToFirst()
                return cursor.getLong(sizeIndex)
            }
        }

        // Metode kedua: gunakan file dari path
        return getPathFromUri(context, uri)?.let { path ->
            File(path).length()
        } ?: -1L
    }

    fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input?.copyTo(output)
            }
        }
        return tempFile
    }

}