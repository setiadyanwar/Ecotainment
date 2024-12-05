package com.godongijo.ecotainment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.FragmentProfileBinding
import com.godongijo.ecotainment.services.auth.AuthService
import com.godongijo.ecotainment.services.transaction.TransactionService
import com.godongijo.ecotainment.ui.activities.AddressActivity
import com.godongijo.ecotainment.ui.activities.CustomerServiceActivity
import com.godongijo.ecotainment.ui.activities.EditProfileActivity
import com.godongijo.ecotainment.ui.activities.HelpCenterActivity
import com.godongijo.ecotainment.ui.activities.NotificationActivity
import com.godongijo.ecotainment.ui.activities.OrderStatusActivity
import com.godongijo.ecotainment.ui.activities.SignInActivity
import com.godongijo.ecotainment.utilities.Glide
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    // Instance of AuthService for handling authentication
    private val authService = AuthService()

    private val transactionService = TransactionService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init()
        setListeners()
    }

    override fun onResume() {
        super.onResume()
        initProfileInfo()
    }


    private fun init() {
        initProfileInfo()
    }

    private fun setListeners() {
        binding.notificationButton.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }

        binding.accountLogout.setOnClickListener { logout() }

        binding.orderWaiting.setOnClickListener {
            openOrderStatusActivity(0)
        }

        binding.orderCurrent.setOnClickListener {
            openOrderStatusActivity(1)
        }

        binding.orderFinish.setOnClickListener {
            openOrderStatusActivity(2)
        }

        binding.cancelOrder.setOnClickListener {
            openOrderStatusActivity(3)
        }

        binding.editProfileButton.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.myAddress.setOnClickListener {
            val intent = Intent(requireContext(), AddressActivity::class.java)
            startActivity(intent)
        }

        binding.helpCenter.setOnClickListener {
            startActivity(Intent(requireContext(), HelpCenterActivity::class.java))
        }

        binding.customerService.setOnClickListener {
            startActivity(Intent(requireContext(), CustomerServiceActivity::class.java))
        }
    }

    private fun initProfileInfo() {
        binding.layoutProfile.visibility = View.GONE
        binding.layoutShimmer.visibility = View.VISIBLE

        authService.getUserProfile(
            context = requireContext(),
            onResult = { user ->
                if(user.profilePicture != null) {
                    val imageProfile = requireContext().getString(R.string.base_url) + user.profilePicture
                    Glide().loadImageFromUrl(binding.profilePicture, imageProfile)
                }

                binding.username.text = user.username

                val timestamp = user.createdAt // Misalnya "2024-11-16T06:05:35.000000Z"

                // Format asli yang diterima dari MySQL (ISO 8601)
                val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())

                // Format yang diinginkan
                val targetFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                try {
                    // Parse tanggal dari format yang diterima
                    val date = timestamp?.let { originalFormat.parse(it) }

                    // Format tanggal menjadi format yang diinginkan
                    val formattedDate = date?.let { targetFormat.format(it) }

                    // Set ke TextView
                    binding.accountDate.text = "Sejak $formattedDate"
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.accountDate.text = "Invalid date" // Jika terjadi error parsing
                }

                binding.layoutProfile.visibility = View.VISIBLE
                binding.layoutShimmer.visibility = View.GONE
            },
            onError = { error ->
                Log.d("ERROR LOAD ACCOUNT", error)
            }
        )
    }

    private fun logout() {
        lifecycleScope.launch {
            if (isAdded) { // Memastikan fragment masih terhubung
                authService.signOut(
                    context = requireContext(),
                    onResult = {
                        if (isAdded) { // Memastikan fragment masih terhubung sebelum melanjutkan
                            val intent = Intent(requireContext(), SignInActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        }
                    },
                    onError = { error ->
                        if (isAdded) { // Memastikan fragment masih terhubung sebelum menampilkan Toast
                            Toast.makeText(requireContext(), "Logout failed: $error", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }

    // Fungsi untuk membuka OrderStatusActivity dengan parameter tab yang dipilih
    private fun openOrderStatusActivity(selectedTab: Int) {
        val intent = Intent(activity, OrderStatusActivity::class.java).apply {
            putExtra("selectedTab", selectedTab)
        }
        startActivity(intent)
    }

}
