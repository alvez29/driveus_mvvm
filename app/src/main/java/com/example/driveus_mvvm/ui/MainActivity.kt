package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.ActivityMainBinding
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.view_model.UserViewModel

class MainActivity : AppCompatActivity() {

    private var viewBinding : ActivityMainBinding? = null
    private val viewModel : UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val userId = "0szOLEo3FeXgdefQLe07"

    private val userObserver = Observer<User> { user ->
        viewBinding?.mainActivityLabelSampleText?.text = user.name
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        viewModel.getUserById(userId).observe(this, userObserver)

        viewBinding?.mainActivityButtonSubmitNameButton?.setOnClickListener {
            viewModel.updateUserName(userId, viewBinding?.mainActivityInputNameInput?.text.toString())
        }
    }
}