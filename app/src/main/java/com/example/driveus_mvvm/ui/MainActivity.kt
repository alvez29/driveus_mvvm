package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var viewBinding : ActivityMainBinding? = null

    private fun saveSession() {
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")

        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider.toString())
        prefs.apply()
    }

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
        saveSession()
    }

}