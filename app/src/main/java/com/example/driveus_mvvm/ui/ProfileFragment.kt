package com.example.driveus_mvvm.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private var viewBinding: FragmentProfileBinding? = null

    private fun logOut() {
        viewBinding?.profileFragmentButtonLogOut?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val authIntent = Intent(activity, AuthActivity::class.java)
            startActivity(authIntent)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentProfileBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logOut()
    }
}