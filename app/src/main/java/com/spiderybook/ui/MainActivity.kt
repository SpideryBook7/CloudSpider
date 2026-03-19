package com.spiderybook.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.spiderybook.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: com.spiderybook.databinding.ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable Edge-to-Edge
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        binding = com.spiderybook.databinding.ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hide System Bars
        val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        if (navHostFragment != null) {
            navController = navHostFragment.navController
            androidx.navigation.ui.NavigationUI.setupWithNavController(binding.bottomNav, navController)
            
            // Hide BottomNav on non-top-level destinations
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.nav_home, R.id.nav_search, R.id.nav_favorites, R.id.nav_downloads, R.id.nav_settings -> {
                        binding.bottomNav.visibility = android.view.View.VISIBLE
                    }
                    else -> {
                        binding.bottomNav.visibility = android.view.View.GONE
                    }
                }
            }
        }
    }
}
