package com.example.driveus_mvvm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var viewBinding : ActivityMainBinding? = null

    private fun configureBottomBarNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_activity__container__fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        viewBinding?.mainActivityToolbarBottomNavView?.setupWithNavController(navController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        configureBottomBarNavigation()
    }

}