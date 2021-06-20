package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth

enum class ProviderType {
    BASIC
}

class HomeActivity : AppCompatActivity() {

    private var viewBinding : ActivityHomeBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        //TODO: CREAR OBSERVABLE PARA GUARDAR EN PREFS ID DEL DOCUMENTO

        //Setup
        val bundle: Bundle? = intent.extras
        val email: String? = bundle?.getString("email")
        val provider: String? = bundle?.getString("provider")
        setup(email?: "", provider?: "")

        //TODO: Crear SessionUtils con los métodos de guardar en sesión, cerrar sesión...
        //Persistencia de los datos de sesión
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString("email", email)
        prefs.putString("provider", provider.toString())
        prefs.apply()
    }

    private fun setup(email: String, provider: String) {
        title = "Inicio"
        viewBinding?.activityMainLabelEmailTextView?.text = email
        viewBinding?.activityAuthLabelProviderTextView?.text = provider

        viewBinding?.logOutButton?.setOnClickListener {
            val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
            prefs.clear()
            prefs.apply()

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }
    }
}