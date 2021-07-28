package com.example.driveus_mvvm.ui


import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
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
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.yellow_300)))
                supportActionBar?.setTitle(Html.fromHtml("<font color='#000000'>DriveUs</font>"))
            }
            "blue" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_300)))
                supportActionBar?.setTitle(Html.fromHtml("<font color='#FFFFFF'>DriveUs </font>"))
            }
            "pink" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.pink_200)))
                supportActionBar?.setTitle(Html.fromHtml("<font color='#FFFFFF'>DriveUs </font>"))
            }
            else -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.black)))
                supportActionBar?.setTitle(Html.fromHtml("<font color='#FFFFFF'>DriveUs </font>"))

            }
        }

    }

    private fun configureBottomBarNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.main_activity__container__fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        //Vinculamos la barra de navegación con el controller de Navigation
        viewBinding?.mainActivityToolbarBottomNavView?.setupWithNavController(navController)

        //Código para que no se refresque la pantalla si pulsamos en el mismo fragmento en el que estamos
        viewBinding?.mainActivityToolbarBottomNavView?.setOnNavigationItemSelectedListener {
            if(it.itemId != viewBinding?.mainActivityToolbarBottomNavView?.selectedItemId)
                NavigationUI.onNavDestinationSelected(it, navController)
            true
        }

        //Definimos el color de fondo al cambiar
        navController.addOnDestinationChangedListener(backgroundColorListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)


        configureBottomBarNavigation()

    }

}