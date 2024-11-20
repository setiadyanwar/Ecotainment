package com.godongijo.ecotainment.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.FragmentDescriptionBinding

class DescriptionFragment : Fragment() {

    private lateinit var binding: FragmentDescriptionBinding
    private var description: String? = null
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ambil data dari Bundle yang diteruskan dari Activity
        description = arguments?.getString("description")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDescriptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set deskripsi ke TextView
        binding.ProductDesc.text = description

        // Batasi jumlah baris awal menjadi 5
        binding.ProductDesc.maxLines = 5

        val arrowDown = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
        val arrowUp = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_up)

        // Observasi jumlah total baris teks
        binding.ProductDesc.viewTreeObserver.addOnGlobalLayoutListener {
            val totalLines = binding.ProductDesc.lineCount
            if (totalLines > 5) {
                binding.ivArrow.visibility = View.VISIBLE
            } else {
                binding.ivArrow.visibility = View.GONE
            }
        }

        // Atur klik listener untuk memperluas/menciutkan teks
        binding.ivArrow.setOnClickListener {
            if (isExpanded) {
                // Kembali ke 5 baris
                binding.ProductDesc.maxLines = 5
                binding.ivArrow.text = "See More"
                binding.ivArrow.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    arrowDown,
                    null
                )
                binding.gradientWhite.visibility = View.VISIBLE
            } else {
                // Perluas ke semua baris
                binding.ProductDesc.maxLines = Int.MAX_VALUE
                binding.ivArrow.text = "Show Less"
                binding.ivArrow.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    arrowUp,
                    null
                )
                binding.gradientWhite.visibility = View.INVISIBLE
            }
            isExpanded = !isExpanded
        }
    }
}
