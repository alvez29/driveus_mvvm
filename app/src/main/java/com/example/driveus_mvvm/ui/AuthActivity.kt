package com.example.driveus_mvvm.ui

import android.content.Context
import android.content.Intent
import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private var viewBinding : ActivityAuthBinding? = null

    //TODO: Ordenar los métodos (privados arriba)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        // Setup
        setup()
        session()
    }

    override fun onStart() {
        super.onStart()
        viewBinding?.authLayout?.visibility = View.VISIBLE
    }

    private fun session() {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE)
        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            viewBinding?.authLayout?.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() {
        title = "Autenticación"

        viewBinding?.activityAuthButtonSignUpButton?.setOnClickListener{
            if (viewBinding?.activityAuthInputEmailEditText?.text?.isNotEmpty() == true && viewBinding?.activityAuthInputPasswordEditText?.text?.isNotEmpty() == true) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(viewBinding?.activityAuthInputEmailEditText?.text.toString(),
                viewBinding?.activityAuthInputPasswordEditText?.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        //TODO: Controlar la excepción del login
                        showAlert()
                    }
                }
            }
        }

        viewBinding?.activityAuthButtonLogInButton?.setOnClickListener{
            if (viewBinding?.activityAuthInputEmailEditText?.text?.isNotEmpty() == true && viewBinding?.activityAuthInputPasswordEditText?.text?.isNotEmpty() == true) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(viewBinding?.activityAuthInputEmailEditText?.text.toString(),
                    viewBinding?.activityAuthInputPasswordEditText?.text.toString()).addOnCompleteListener {
                        if (it.isSuccessful) {
                            showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                        } else {
                            showAlert()
                        }
                }
            }
        }
    }

    private fun showAlert() {

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Se ha producido un error autenticando al usuario")
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun  showHome(email: String, provider: ProviderType) {
        val homeIntent: Intent = Intent (this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.toString())
        }
        startActivity(homeIntent)
    }
}