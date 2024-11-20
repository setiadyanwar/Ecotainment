package com.godongijo.ecotainment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.godongijo.ecotainment.databinding.SingleViewSearchBinding
import com.godongijo.ecotainment.models.SearchHistory
import com.godongijo.ecotainment.services.product.SearchService

class SearchHistoryAdapter(
    private var context: Context,
    private var searchHistoryList: List<SearchHistory>,
    private val searchService: SearchService
) : RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder>() {

    // ViewHolder with binding
    inner class SearchHistoryViewHolder(val binding: SingleViewSearchBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchHistoryViewHolder {
        val binding = SingleViewSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchHistoryViewHolder, position: Int) {
        val searchHistory = searchHistoryList[position]

        holder.binding.apply {
            searchText.text = searchHistory.searchQuery
        }

    }

    override fun getItemCount(): Int = searchHistoryList.size

    fun updateList(newSearchHistoryList: List<SearchHistory>) {
        // Batasi list ke 10 item
        searchHistoryList = if (newSearchHistoryList.size > 10) {
            newSearchHistoryList.take(10)
        } else {
            newSearchHistoryList
        }
        notifyDataSetChanged()
    }
}
