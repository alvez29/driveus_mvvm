package com.example.driveus_mvvm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityAuthBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private var viewBinding : ActivityAuthBinding? = null
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val firebaseAuth: FirebaseAuth = FirestoreRepository.getFirebaseAuthInstance()
    private val gifURL = "https://i.imgur.com/N16kTqz.gif"

    private fun checkSession() {
        if (firebaseAuth.currentUser != null) {
            viewBinding?.authLayout?.visibility = View.INVISIBLE
            startMainActivity()
        }
    }

    private fun saveDocIdInSession(docId: String?) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString(getString(R.string.shared_pref_doc_id_key), docId)
        prefs.apply()
    }

    private fun setup() {
        title = "Autenticación"

        supportActionBar?.hide()

        viewBinding?.authActivityImgGifBackground?.let {
            Glide.with(this)
                .load(gifURL)
                .into(it)
        }

        viewBinding?.activityAuthButtonLogInButton?.setOnClickListener {
            showLoadingBar()
            if (viewBinding?.activityAuthInputEmailEditText?.text?.isNotEmpty() == true && viewBinding?.activityAuthInputPasswordEditText?.text?.isNotEmpty() == true) {
                firebaseAuth.signInWithEmailAndPassword(viewBinding?.activityAuthInputEmailEditText?.text.toString(),
                    viewBinding?.activityAuthInputPasswordEditText?.text.toString())
                        .addOnSuccessListener {
                            it.user?.uid?.let { uid ->
                                viewModel.getUserByUid(uid).get().addOnSuccessListener { querySnapshot ->
                                    saveDocIdInSession(querySnapshot?.documents?.first()?.id)
                                    startMainActivity()
                                }
                            }
                        }.addOnFailureListener{
                            showAlert()
                            hideLoadingBar()

                        }
            } else {
                hideLoadingBar()
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
        builder.setPositiveButton(getString(R.string.login_dialog__message__positive_message), null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showForm() {
        val formIntent = Intent(this, SignUpActivity::class.java)
        startActivity(formIntent)
    }

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(mainActivityIntent)
        this.finish()
    }

    private fun showLoadingBar() {
        viewBinding?.activityAuthImageLoadingBar?.apply {
            visibility = View.VISIBLE
            progress = 0F
        }
    }

    private fun hideLoadingBar() {
        viewBinding?.activityAuthImageLoadingBar?.visibility = View.GONE
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