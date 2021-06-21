package com.example.driveus_mvvm.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivitySignUpBinding
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth



class SignUpActivity : AppCompatActivity() {

    private var viewBinding : ActivitySignUpBinding? = null
    private val viewModel : UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val formErrorsObserver = Observer<Map<SignUpFormEnum, Int>> {
        it.forEach { (k,v) ->
            when(k) {
                SignUpFormEnum.NAME -> viewBinding?.activityAuthInputNameEditText?.error = getString(v)
                SignUpFormEnum.SURNAME -> viewBinding?.activitySignInputSurnameEditText?.error = getString(v)
                SignUpFormEnum.USERNAME -> viewBinding?.activitySignUpInputUsernameEditText?.error = getString(v)
                SignUpFormEnum.EMAIL -> viewBinding?.activitySignUpInputEmailEditText?.error = getString(v)
                SignUpFormEnum.PASSWORD -> viewBinding?.activitySignUpInputPasswordEditText?.error = getString(v)
                SignUpFormEnum.CONFIRM_PASSWORD -> viewBinding?.activitySignUpInputPasswordRepeatEditText?.error = getString(v)
            }
        }
    }

    private val redirectObserver = Observer<Boolean> {
        if(it) {
            showHome(viewBinding?.activitySignUpInputEmailEditText?.text.toString(), ProviderType.BASIC)
        }
    }

    private fun getInputs() : Map<SignUpFormEnum, String>{
        return mutableMapOf(
            SignUpFormEnum.NAME to viewBinding?.activityAuthInputNameEditText?.text.toString(),
            SignUpFormEnum.SURNAME to viewBinding?.activitySignInputSurnameEditText?.text.toString(),
            SignUpFormEnum.USERNAME to viewBinding?.activitySignUpInputUsernameEditText?.text.toString(),
            SignUpFormEnum.EMAIL to viewBinding?.activitySignUpInputEmailEditText?.text.toString(),
            SignUpFormEnum.PASSWORD to viewBinding?.activitySignUpInputPasswordEditText?.text.toString(),
            SignUpFormEnum.CONFIRM_PASSWORD to viewBinding?.activitySignUpInputPasswordRepeatEditText?.text.toString()
        )
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
        title = getString(R.string.sign_up_title)

        viewModel.getFormErrors().observe(this, formErrorsObserver)
        viewModel.getRedirect().observe(this, redirectObserver)

        viewBinding?.activitySignUpButtonSignUp?.setOnClickListener {
               viewModel.createNewUser(getInputs())
        }
    }
}