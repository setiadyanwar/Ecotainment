package com.godongijo.ecotainment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.godongijo.ecotainment.databinding.FragmentHistoryBinding
import com.godongijo.ecotainment.ui.activities.NotificationActivity

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHistoryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        init()
        setListeners()
    }

    private fun init() {

    }

    private fun setListeners() {
        binding.notificationButton.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
    }
}
