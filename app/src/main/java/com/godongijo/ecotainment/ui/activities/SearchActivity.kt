package com.godongijo.ecotainment.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.godongijo.ecotainment.adapters.ProductAdapter
import com.godongijo.ecotainment.adapters.SearchHistoryAdapter
import com.godongijo.ecotainment.adapters.SkeletonAdapter
import com.godongijo.ecotainment.databinding.ActivitySearchBinding
import com.godongijo.ecotainment.services.product.ProductService
import com.godongijo.ecotainment.services.product.SearchService
import com.godongijo.ecotainment.utilities.PreferenceManager

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private val searchService = SearchService()
    private val productService = ProductService()

    // Adapters
    private lateinit var searchHistoryAdapter: SearchHistoryAdapter
    private lateinit var productAdapter: ProductAdapter

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var authToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        setListeners()

    }

    private fun init() {
        preferenceManager = PreferenceManager(this)
        authToken = preferenceManager.getString("auth_token") ?: ""

        binding.layoutSearchResult.visibility = View.GONE
        binding.popularSearches.visibility = View.GONE
        binding.popularSearchesRecycler.visibility = View.GONE

        binding.inputSearch.requestFocus()

        // Menampilkan keyboard secara otomatis
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.inputSearch, InputMethodManager.SHOW_IMPLICIT)

        if(authToken != "") {
            initRecentSearch()
        }

//        initPopularSearch()
    }

    private fun setListeners() {
        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.buttonCart.setOnClickListener {
            if(authToken == "") {
                startActivity(Intent(this, SignInActivity::class.java))
            } else {
                startActivity(Intent(this, CartActivity::class.java))
            }
        }

        // Listener untuk mendeteksi perubahan teks
        binding.inputSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                binding.layoutSearchResult.visibility = View.GONE
                binding.layoutSearchItem.visibility = View.VISIBLE

                // Tampilkan atau sembunyikan tombol clearSearch
                if (s.isNullOrEmpty()) {
                    binding.clearSearch.visibility = View.GONE
                } else {
                    binding.clearSearch.visibility = View.VISIBLE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Tambahkan klik listener pada tombol clearSearch
        binding.clearSearch.setOnClickListener {
            binding.inputSearch.text?.clear() // Hapus teks
        }

        // Listener untuk mendeteksi tombol Enter pada keyboard
        binding.inputSearch.setOnEditorActionListener { _, actionId, event ->
            // Cek apakah tombol "search" pada keyboard ditekan
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {

                val query = binding.inputSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    searchProduct(query) // Jalankan fungsi searchProduct dengan query
                }

                // Sembunyikan keyboard setelah pencarian
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(binding.inputSearch.windowToken, 0)

                true // Menyatakan aksi telah ditangani
            } else {
                false // Tidak menangani aksi lainnya
            }
        }


        binding.iconSearch.setOnClickListener{
            val query = binding.inputSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchProduct(query) // Jalankan fungsi searchProduct dengan query
            }

            // Sembunyikan keyboard setelah pencarian
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.inputSearch.windowToken, 0)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            val query = binding.inputSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchProduct(query) // Jalankan fungsi searchProduct dengan query
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun searchProduct(searchQuery: String) {
        binding.layoutSearchResult.visibility = View.VISIBLE
        binding.layoutSearchItem.visibility = View.GONE
        binding.layoutEmptySearch.visibility = View.GONE

        binding.searchResultRecycler.layoutManager = GridLayoutManager(this, 2) // Menampilkan 2 kolom per baris
        productAdapter = ProductAdapter(this, emptyList(), productService)

        val skeletonAdapter = SkeletonAdapter(10, 1)
        binding.searchResultRecycler.adapter = skeletonAdapter

        productService.getProductList(
            context = this,
            searchQuery = searchQuery,
            onResult = { productList ->
                // Update UI based on whether there are items in the list
                if (productList.isEmpty()) {
                    binding.layoutSearchResult.visibility = View.GONE
                    binding.layoutSearchItem.visibility = View.GONE

                    binding.searchResultRecycler.visibility = View.GONE
                    binding.layoutEmptySearch.visibility = View.VISIBLE
                } else {
                    binding.layoutSearchResult.visibility = View.VISIBLE
                    binding.layoutSearchItem.visibility = View.GONE

                    binding.searchResultRecycler.visibility = View.VISIBLE
                    binding.layoutEmptySearch.visibility = View.GONE

                    binding.searchResultRecycler.adapter = productAdapter
                    productAdapter.updateList(productList)
                }

                binding.inputSearch.clearFocus()
            },
            onError = {
                binding.layoutSearchResult.visibility = View.GONE
                binding.layoutSearchItem.visibility = View.GONE

                binding.searchResultRecycler.visibility = View.GONE
                binding.layoutEmptySearch.visibility = View.VISIBLE

            }
        )
    }

    private fun initRecentSearch() {
        binding.recentSearchesRecycler.layoutManager = LinearLayoutManager(this) // Menampilkan 2 kolom per baris
        searchHistoryAdapter = SearchHistoryAdapter(this, emptyList(), searchService)
        binding.recentSearchesRecycler.adapter = searchHistoryAdapter
        searchService.getSearchHistory(
            context = this,
            onResult = { searchHistoryList ->
                // Update UI based on whether there are items in the list
                if (searchHistoryList.isEmpty()) {
                    binding.recentSearchesRecycler.visibility = View.GONE
                } else {
                    binding.recentSearchesRecycler.visibility = View.VISIBLE
                    searchHistoryAdapter.updateList(searchHistoryList)
                }
            },
            onError = {
                binding.recentSearchesRecycler.visibility = View.GONE
            }
        )
    }

    private fun initPopularSearch() {

    }

}