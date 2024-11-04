package com.godongijo.ecotainment.ui.fragments

import ProductAdapter
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.FragmentHomeBinding
import com.godongijo.ecotainment.models.Product

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productList = listOf(
            Product(R.drawable.fs8, "Hiasan Tanaman Lidah Mertua", "Rp 200.000", "15% off", 4.9, 20),
            Product(R.drawable.fs8, "Tanaman Hias Monstera", "Rp 150.000", "10% off", 4.8, 18),
            Product(R.drawable.fs8, "Kaktus Mini", "Rp 50.000", "5% off", 4.7, 25),
            Product(R.drawable.fs8, "Bonsai Cemara", "Rp 350.000", "20% off", 4.5, 12),
            Product(R.drawable.fs8, "Tanaman Puring", "Rp 100.000", "8% off", 4.6, 30),
            Product(R.drawable.fs8, "Tanaman Hias Lavender", "Rp 80.000", "12% off", 4.9, 22),
            Product(R.drawable.fs8, "Bambu Rejeki", "Rp 120.000", "10% off", 4.4, 15),
            Product(R.drawable.fs8, "Tanaman Sirih Gading", "Rp 70.000", "15% off", 4.8, 28),
            Product(R.drawable.fs8, "Kaktus Hias", "Rp 60.000", "5% off", 4.6, 20),
            Product(R.drawable.fs8, "Aglonema Merah", "Rp 90.000", "7% off", 4.7, 19)
        )

        // Initialize the adapter with the product list
        productAdapter = ProductAdapter(productList)

        // Set up the RecyclerView with a grid layout
        binding.recyclerViewProduct.apply {
            layoutManager = GridLayoutManager(requireContext(), 2) // Menampilkan 2 kolom per baris
            adapter = productAdapter
            isNestedScrollingEnabled = false // Menonaktifkan nested scrolling
        }

        // Set up the image slider with slide models
        val slideModels = listOf(
            SlideModel(R.drawable.banner_1, null, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(slideModels)

        val underlinedText = SpannableString("Tambahkan Alamatmu")
        underlinedText.setSpan(UnderlineSpan(), 0, underlinedText.length, 0)

        binding.tambahAlamat.text = underlinedText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
