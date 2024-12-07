package com.godongijo.ecotainment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.adapters.ProductAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.FragmentHomeBinding
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.ui.activities.GotoSchoolActivity
import com.godongijo.ecotainment.ui.activities.NotificationActivity
import com.godongijo.ecotainment.ui.activities.OfflineFieldTripActivity
import com.godongijo.ecotainment.ui.activities.ReservationActivity
import com.godongijo.ecotainment.ui.activities.SearchActivity
import com.godongijo.ecotainment.ui.activities.SignInActivity
import com.godongijo.ecotainment.ui.activities.VirtualTripActivity
import com.godongijo.ecotainment.utilities.Glide
import com.godongijo.ecotainment.utilities.PreferenceManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    // Adapters for different lists
    private lateinit var productAdapter: ProductAdapter

    // Preference Manager
    private lateinit var preferenceManager: PreferenceManager

    // Services for handling data operations
    private val productService = ProductService()

    private val authService = AuthService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        initProfileInfo() // Panggil ulang setiap kali fragment aktif kembali
        initProductList()
    }


    private fun init() {
        preferenceManager = PreferenceManager(requireContext())
        val authToken = preferenceManager.getString("auth_token") ?: ""

        // Init Profile
        if (authToken == "") {
            binding.buttonSignIn.visibility = View.VISIBLE
            binding.profilePicture.visibility = View.GONE
            binding.textDaftar.text = "Ayo Daftar\nSekarang Juga!!"
        } else {
            binding.buttonSignIn.visibility = View.GONE
            binding.profilePicture.visibility = View.VISIBLE
            binding.textDaftar.text = "Selamat Datang di\nEcotainment"

            initProfileInfo()
        }

        // Init Product List
        initProductList()

        // Set up the image slider with slide models
        val slideModels = listOf(
            SlideModel(R.drawable.banner_1, null, ScaleTypes.FIT),
            SlideModel(R.drawable.banner_2, null, ScaleTypes.FIT)
        )
        binding.imageSlider.setImageList(slideModels)

        val underlinedText = SpannableString("Tambahkan Alamatmu")
        underlinedText.setSpan(UnderlineSpan(), 0, underlinedText.length, 0)
        binding.tambahAlamat.text = underlinedText
    }

    private fun setListeners() {
        binding.buttonSignIn.setOnClickListener { signIn() }

        binding.searchBar.setOnClickListener { search() }
        binding.searchInput.setOnClickListener { search() }
        binding.searchIcon.setOnClickListener { search() }

        binding.notificationButton.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            initProductList()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        binding.virtualFieldtrip.setOnClickListener {
            val intent =
                Intent(activity, VirtualTripActivity::class.java)
            startActivity(intent)
        }

        binding.goesToSchool.setOnClickListener {
            val intent =
                Intent(activity, GotoSchoolActivity::class.java)
            startActivity(intent)
        }

        binding.offlieFieldtrip.setOnClickListener {
            val intent =
                Intent(activity, OfflineFieldTripActivity::class.java)
            startActivity(intent)
        }

        binding.reservation.setOnClickListener {
            val intent =
                Intent(activity, ReservationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initProfileInfo() {
        authService.getUserProfile(
            context = requireContext(),
            onResult = { user ->
                if(user.profilePicture != null) {
                    val imageProfile = requireContext().getString(R.string.base_url) + user.profilePicture
                    Glide().loadImageFromUrl(binding.profilePicture, imageProfile)
                }
            },
            onError = {

            }
        )

        authService.getUserAddress(
            requireContext(),
            onResult = { addressList ->
                if (addressList.isNotEmpty()) {
                    val selectedAddress = addressList[0] // Ambil alamat pertama sebagai default
                    val fullAddress = "${selectedAddress.detailAddress}, ${selectedAddress.cityOrDistrict}, ${selectedAddress.province}"

                    val underlinedText = SpannableString(fullAddress)
                    binding.tambahAlamat.text = underlinedText

                }
            },
            onError = {

            }
        )
    }

    private fun initProductList() {
        binding.recyclerViewProduct.layoutManager = GridLayoutManager(requireContext(), 2) // Menampilkan 2 kolom per baris
        binding.recyclerViewProduct.isNestedScrollingEnabled = false
        productAdapter = ProductAdapter(requireContext(), emptyList(), productService)

        val skeletonAdapter = SkeletonAdapter(10, 1)
        binding.recyclerViewProduct.adapter = skeletonAdapter

        lifecycleScope.launch {
            productService.getProductList(
                context = requireContext(),
                onResult = { productList ->
                    // Update UI based on whether there are items in the list
                    if (productList.isEmpty()) {
                        binding.recyclerViewProduct.visibility = View.GONE
                        binding.emptyProduct.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewProduct.visibility = View.VISIBLE
                        binding.emptyProduct.visibility = View.GONE

                        binding.recyclerViewProduct.adapter = productAdapter
                        productAdapter.updateList(productList)
                    }
                },
                onError = {
                    binding.recyclerViewProduct.visibility = View.GONE
                    binding.emptyProduct.text = "Gagal memuat data"
                    binding.emptyProduct.visibility = View.VISIBLE
                }
            )
        }
    }

    private fun signIn() {
        val intent = Intent(requireContext(), SignInActivity::class.java)
        startActivity(intent)
    }

    private fun search() {
        val intent = Intent(requireContext(), SearchActivity::class.java)
        startActivity(intent)
    }

}
