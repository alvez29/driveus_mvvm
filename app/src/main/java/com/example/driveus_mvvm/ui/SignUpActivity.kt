package com.example.driveus_mvvm.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.databinding.ActivitySignUpBinding
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private var viewBinding : ActivitySignUpBinding? = null
    private val viewModel : UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }


    private fun showAlert(er: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(er)
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        setup()
    }

    private fun setup() {
        title = "Registro"


        viewBinding?.activitySignUpButtonSignUp?.setOnClickListener {
            if (viewBinding?.activityAuthInputNameEditText?.text?.isNotEmpty() == true
                && viewBinding?.activitySignInputSurnameEditText?.text?.isNotEmpty() == true
                && viewBinding?.activitySignUpInputEmailEditText?.text?.isNotEmpty() == true
                && viewBinding?.activitySignUpInputPasswordEditText?.text?.isNotEmpty() == true
                && viewBinding?.activitySignUpInputPasswordRepeatEditText?.text?.isNotEmpty() == true) {

                val name = viewBinding?.activityAuthInputNameEditText?.text.toString()
                val surname = viewBinding?.activitySignInputSurnameEditText?.text.toString()
                val username = viewBinding?.activitySignUpInputUsernameEditText?.text.toString()
                val email= viewBinding?.activitySignUpInputEmailEditText?.text.toString()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                    viewBinding?.activitySignUpInputEmailEditText?.text.toString(),
                    viewBinding?.activitySignUpInputPasswordEditText?.text.toString()
                ).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val uid = it.result?.user?.uid

                        val newUser = User(uid, username, surname, name, emptyList(), emptyList(), false, email)
                        viewModel.createNewUser(newUser)
                        showHome(it.result?.user?.email ?: "", ProviderType.BASIC)
                    } else {
                        //TODO: Controlar la excepción del login
                        showAlert("Se ha producido un error autenticando al usuario")
                    }
                }
            } else {
                showAlert("Debes completar todos los campos")
            }
        }
    }
}