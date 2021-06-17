package com.example.driveus_mvvm.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.driveus_mvvm.databinding.ActivityGoogleSignInBinding

class GoogleSignInActivity : AppCompatActivity() {

    private var viewBinding : ActivityGoogleSignInBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityGoogleSignInBinding.inflate(layoutInflater)
        setContentView(viewBinding?.root)

    }


}