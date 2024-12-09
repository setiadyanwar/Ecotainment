package com.godongijo.ecotainment.ui.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.OrderStatusAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.ActivityOrderStatusBinding
import com.godongijo.ecotainment.models.SkeletonLayoutType
import com.godongijo.ecotainment.services.transaction.TransactionService
import com.google.android.material.tabs.TabLayout

class OrderStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderStatusBinding

    private lateinit var orderStatusAdapter: OrderStatusAdapter

    private val transactionService = TransactionService()

    private var selectedTab: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()
    }

    // Refresh data when the activity is resumed
    override fun onResume() {
        super.onResume()
        // Re-load data for the selected tab
        val tabTitle = binding.tabLayout.getTabAt(selectedTab)?.text.toString()
        loadDataForTab(tabTitle)
    }

    private fun init() {
        // Setup TabLayout
        setupTabs()

        // Setup RecyclerView
        setupRecyclerView()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupTabs() {
        // Retrieve selected tab index
        selectedTab = intent.getIntExtra("selectedTab", 0)

        // Adding tabs to TabLayout
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Menunggu"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Saat Ini"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Selesai"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Dibatalkan"))

        // Automatically select the tab based on selectedTab index
        binding.tabLayout.getTabAt(selectedTab)?.select()

        // Load data based on the selected tab
        val tabTitle = binding.tabLayout.getTabAt(selectedTab)?.text.toString()
        loadDataForTab(tabTitle)

        // Adding tab selected listener
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Get selected tab and load data accordingly
                tab?.let {
                    val selectedTabTitle = it.text.toString()
                    loadDataForTab(selectedTabTitle)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerView() {
        orderStatusAdapter = OrderStatusAdapter(
            this,
            transactionService,
            onFinishAction = { tabTitle ->
                loadDataForTab(tabTitle)
            }
        )
        binding.orderStatusRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadDataForTab(tabTitle: String) {
        val skeletonAdapter = SkeletonAdapter(5, SkeletonLayoutType.ORDER_STATUS)
        binding.orderStatusRecyclerView.adapter = skeletonAdapter

        // Fetch all transactions (replace this with your service call)
        transactionService.getTransactionList(
            context = this,
            onResult = { transactionList ->
                // Fetch data based on selected tab
                val filteredData = when (tabTitle) {
                    "Menunggu" -> transactionList.filter { it.status == "pending" || it.status == "waiting_for_confirmation" }
                    "Saat Ini" -> transactionList.filter { it.status == "processed" || it.status == "on_shipment"}
                    "Selesai" -> transactionList.filter { it.status == "completed" || it.status == "reviewed"}
                    "Dibatalkan" -> transactionList.filter { it.status == "canceled" }
                    else -> emptyList()
                }

                if (filteredData.isEmpty()) {
                    binding.orderStatusRecyclerView.visibility = View.GONE
                    binding.layoutEmptyCart.visibility = View.VISIBLE
                } else {
                    // Update the adapter with new data
                    orderStatusAdapter.updateData(filteredData)

                    binding.orderStatusRecyclerView.adapter = orderStatusAdapter

                    binding.orderStatusRecyclerView.visibility = View.VISIBLE
                    binding.layoutEmptyCart.visibility = View.GONE
                }
            },
            onError = {}
        )
    }
}
