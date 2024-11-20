package com.godongijo.ecotainment.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.godongijo.ecotainment.R
import com.godongijo.ecotainment.databinding.ActivityMainBinding
import com.godongijo.ecotainment.ui.fragment.*
import com.godongijo.ecotainment.ui.fragment.HomeFragment
import com.godongijo.ecotainment.utilities.PreferenceManager
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Instance of Auth
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var authToken: String

    // Variables for holding instances of fragments
    private var homeFragment: HomeFragment? = null
    private var wishlistFragment: WishlistFragment? = null
    private var historyFragment: HistoryFragment? = null
    private var profileFragment: ProfileFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferenceManager = PreferenceManager(this)
        authToken = preferenceManager.getString("auth_token") ?: ""

        binding.navShop.translationZ = 16f

        // Set the default fragment to HomeFragment when the app is launched
        replaceFragment(HomeFragment())

        // Set up click listeners for navigation
        setListeners()

    }

    // Function to set listeners for bottom navigation actions
    private fun setListeners() {
        binding.navShop.setOnClickListener {
            if (authToken == "") {
                // Jika belum login, arahkan ke halaman SignIn
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(this, CartActivity::class.java)
                startActivity(intent)
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_home -> {
                    if (homeFragment == null) homeFragment = HomeFragment()
                    replaceFragment(homeFragment!!)
                    true
                }
                R.id.nav_wishlist -> {
                    // Cek apakah pengguna sudah login
                    if (authToken == "") {
                        // Jika belum login, arahkan ke halaman SignIn
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Jika sudah login, buka wishlistFragment
                        if (wishlistFragment == null) wishlistFragment = WishlistFragment()
                        replaceFragment(wishlistFragment!!)
                    }
                    true
                }
                R.id.nav_history -> {
                    // Cek apakah pengguna sudah login
                    if (authToken == "") {
                        // Jika belum login, arahkan ke halaman SignIn
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        if (historyFragment == null) historyFragment = HistoryFragment()
                        replaceFragment(historyFragment!!)
                    }
                    true
                }
                R.id.nav_profile -> {
                    // Cek apakah pengguna sudah login
                    if (authToken == "") {
                        // Jika belum login, arahkan ke halaman SignIn
                        val intent = Intent(this, SignInActivity::class.java)
                        startActivity(intent)
                    } else {
                        if (profileFragment == null) profileFragment = ProfileFragment()
                        replaceFragment(profileFragment!!)
                    }
                    true
                }
                // Return false if the selected item does not match any case
                else -> false
            }
        }
    }

    // Function to replace the current fragment with the one passed as parameter
    private fun replaceFragment(fragment: Fragment) {
        // Prevent replacing fragments if the activity's state is already saved
        if (supportFragmentManager.isStateSaved) return

        // Get the FragmentManager to handle the transaction
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // Check if the fragment to be replaced is the same as the current one, to avoid unnecessary replacement
        val currentFragment = fragmentManager.findFragmentById(binding.fragmentContainer.id)
        if (currentFragment != null && currentFragment::class.java == fragment::class.java) {
            return
        }

        // Replace the current fragment and allow state loss in case of system destruction
        fragmentTransaction.replace(binding.fragmentContainer.id, fragment)
        fragmentTransaction.commitAllowingStateLoss()
    }

}
