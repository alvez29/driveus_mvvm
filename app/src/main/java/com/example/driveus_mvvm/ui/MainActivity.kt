package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.databinding.ActivityMainBinding
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.view_model.UserViewModel

class MainActivity : AppCompatActivity() {

    private val viewBinding : ActivityMainBinding? = null
    private val viewModel : UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val userObserver = Observer<User> { user ->
        Log.w("///////////////", user.name)
        viewBinding?.mainActivityLabelSampleText?.text = user.name
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

        viewModel.getUserById("0szOLEo3FeXgdefQLe07").observe(this, userObserver)
    }
}