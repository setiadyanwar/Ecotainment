package com.godongijo.ecotainment.ui.fragment

import android.os.Bundle
import android.util.EventLogTags.Description
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.FragmentDescriptionBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


class DescriptionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentDescriptionBinding? = null
    private val binding get() = _binding!!
    private val description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDescriptionBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ProductDesc.setText(description)
        binding.ProductDesc.setMaxLines(5)
        val arrowDown = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)
        val arrowUp = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_down)

        binding.ivArrow.setOnClickListener { v ->
            if (binding.ProductDesc.getMaxLines() === 5) {
                binding.ProductDesc.setMaxLines(Int.MAX_VALUE)
                binding.ivArrow.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    arrowUp,
                    null
                )
                binding.gradientWhite.visibility = View.INVISIBLE
            } else {
                binding.ProductDesc.setMaxLines(5)
                binding.ivArrow.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    null,
                    null,
                    arrowDown,
                    null
                )
                binding.gradientWhite.visibility = View.VISIBLE
            }
        }
    }


}