package com.example.driveus_mvvm.ui


import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityMainBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    companion object {
        const val BACKGROUND_COLOR_KEY = "backgroundColor"
    }

    private val firebaseAuth: FirebaseAuth = FirestoreRepository.getFirebaseAuthInstance()
    private val sharedPref by lazy { this.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }
    private var viewBinding : ActivityMainBinding? = null

    private val backgroundLogOutAnimation = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            //no-op
        }

        override fun onAnimationEnd(animation: Animator?) {
            firebaseAuth.signOut()
            sharedPref?.edit()?.clear()?.apply()
            val authIntent = Intent(this@MainActivity, AuthActivity::class.java)
            authIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(authIntent)
            finish()
        }

        override fun onAnimationCancel(animation: Animator?) {
            //no-op
        }

        override fun onAnimationRepeat(animation: Animator?) {
            //no-op
        }

    }

    private val backgroundColorListener =
        NavController.OnDestinationChangedListener { _, _, arguments ->
            applyBackgroundColor(arguments)
        }

    fun logOut() {
        viewBinding?.mainActivityToolbarBottomNavView?.visibility = View.GONE
        supportActionBar?.hide()
        viewBinding?.mainActivityAnimationLogout?.visibility = View.VISIBLE
        viewBinding?.mainActivityAnimationLogout?.playAnimation()
    }

    private fun applyBackgroundColor(arguments: Bundle?) {
        when(arguments?.getString(BACKGROUND_COLOR_KEY)) {
            "yellow" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.yellow_300)))
                supportActionBar?.setTitle(Html.fromHtml(getString(R.string.black_message_bar)))
            }
            "blue" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.teal_300)))
                supportActionBar?.setTitle(Html.fromHtml(getString(R.string.white_message_bar)))
            }
            "pink" -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.pink_200)))
                supportActionBar?.setTitle(Html.fromHtml(getString(R.string.white_message_bar)))
            }
            else -> {
                supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.black)))
                supportActionBar?.setTitle(Html.fromHtml(getString(R.string.white_message_bar)))

            }
        }

    }

    private fun configureBackgroundAnimation() {
        viewBinding?.mainActivityAnimationLogout?.addAnimatorListener(backgroundLogOutAnimation)
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

        configureBackgroundAnimation()
        configureBottomBarNavigation()

    }

}