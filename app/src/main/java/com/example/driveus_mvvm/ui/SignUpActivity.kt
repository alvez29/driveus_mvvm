package com.example.driveus_mvvm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivitySignUpBinding
import com.example.driveus_mvvm.ui.enums.SignUpFormEnum
import com.example.driveus_mvvm.view_model.UserViewModel

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

    private val redirectObserver = Observer<Pair<Boolean, String>> {
        if (it.first) {
            try {
                saveDocIdInSession(it.second)
                startMainActivity()
            } catch (e: Exception) {
                Log.d(getString(R.string.sign_up_error_save_session_tag), e.message.toString())
            }
        }
    }

    private fun saveDocIdInSession(docId: String?) {
        val prefs = getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE).edit()
        prefs.putString(getString(R.string.shared_pref_doc_id_key), docId)
        prefs.apply()
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

    private fun startMainActivity() {
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(mainActivityIntent)
        this.finish()
    }


    private fun setup() {
        title = getString(R.string.sign_up_title)

        viewModel.getFormErrors().observe(this, formErrorsObserver)
        viewModel.getRedirect().observe(this, redirectObserver)

        viewBinding?.activitySignUpButtonSignUp?.setOnClickListener {
            viewModel.createNewUser(getInputs())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        setup()
    }


}