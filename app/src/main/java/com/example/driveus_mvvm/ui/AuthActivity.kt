package com.example.driveus_mvvm.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityAuthBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private var viewBinding : ActivityAuthBinding? = null

    private fun checkSession() {
        if (FirebaseAuth.getInstance().currentUser != null) {
        viewBinding?.authLayout?.visibility = View.INVISIBLE
            startMainActivity()
        }
    }

    private fun setup() {
        title = "Autenticación"

        viewBinding?.activityAuthButtonLogInButton?.setOnClickListener{
            if (viewBinding?.activityAuthInputEmailEditText?.text?.isNotEmpty() == true && viewBinding?.activityAuthInputPasswordEditText?.text?.isNotEmpty() == true) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(viewBinding?.activityAuthInputEmailEditText?.text.toString(),
                    viewBinding?.activityAuthInputPasswordEditText?.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        startMainActivity()
                    } else {
                        showAlert()
                    }
                }
            }
        }

      viewBinding?.activityAuthButtonSignUpButton?.setOnClickListener {
            showForm()
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.login_dialog__message__title))
        builder.setMessage(getString(R.string.login_dialog__message__error))
        builder.setPositiveButton(getString(R.string.login_dialog__mesage__positive_message), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showForm() {
        val formIntent = Intent(this, SignUpActivity::class.java)
        startActivity(formIntent)
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(mainActivityIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        // Setup
        setup()
        checkSession()
    }

    override fun onStart() {
        super.onStart()
        viewBinding?.authLayout?.visibility = View.VISIBLE
    }
}