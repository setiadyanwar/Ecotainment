package com.godongijo.ecotainment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.godongijo.ecotainment.databinding.FragmentWishlistBinding
import com.godongijo.ecotainment.ui.activities.MainActivity
import com.godongijo.ecotainment.ui.activities.SignInActivity
import com.google.firebase.auth.FirebaseAuth

class WishlistFragment : Fragment() {

    private lateinit var binding: FragmentWishlistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWishlistBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init()
    }

    private fun init() {

    }
}
