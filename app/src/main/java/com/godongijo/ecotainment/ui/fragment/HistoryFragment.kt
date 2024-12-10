package com.godongijo.ecotainment.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.HistoryAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.FragmentHistoryBinding
import com.godongijo.ecotainment.services.transaction.TransactionService
import com.godongijo.ecotainment.ui.activities.NotificationActivity

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    private lateinit var historyAdapter: HistoryAdapter

    private val transactionService = TransactionService()

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
        initHistoryPurchase()
    }

    private fun setListeners() {
        binding.notificationButton.setOnClickListener {
            startActivity(Intent(requireContext(), NotificationActivity::class.java))
        }
    }

    private fun initHistoryPurchase() {
        binding.historyRecycler.visibility = View.VISIBLE
        binding.emptyLayout.visibility = View.GONE

        historyAdapter = HistoryAdapter(requireContext())
        binding.historyRecycler.layoutManager = LinearLayoutManager(requireContext())

        val skeletonAdapter = SkeletonAdapter(2, 4)
        binding.historyRecycler.adapter = skeletonAdapter


        transactionService.getTransactionList(
            context = requireContext(),
            onResult = { transactionList ->
                // Fetch data based on selected tab
                val filteredData = transactionList.filter { it.status == "completed" || it.status == "reviewed" }

                if (filteredData.isEmpty()) {
                    binding.historyRecycler.visibility = View.GONE
                    binding.emptyLayout.visibility = View.VISIBLE
                } else {
                    // Update the adapter with new data
                    historyAdapter.updateData(filteredData)

                    binding.historyRecycler.adapter = historyAdapter

                    binding.historyRecycler.visibility = View.VISIBLE
                    binding.emptyLayout.visibility = View.GONE
                }
            },
            onError = {}
        )
    }
}
