package com.example.driveus_mvvm.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    companion object {
        const val BACKGROUND_COLOR_KEY = "backgroundColor"
    }

    private var viewBinding : ActivityMainBinding? = null

    private val backgroundColorListener =
        NavController.OnDestinationChangedListener { _, _, arguments ->
            applyBackgroundColor(arguments)
        }

    private fun applyBackgroundColor(arguments: Bundle?) {
        when(arguments?.getString(BACKGROUND_COLOR_KEY)) {
            "yellow" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.yellow_500)))
            }
            "blue" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_300)))
            }
            "pink" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.pink_200)))
            }
            else -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.black)))
            }
        }

    }

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
        navController.addOnDestinationChangedListener(backgroundColorListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        configureBottomBarNavigation()
        saveSession()
    }

}