package com.example.driveus_mvvm.ui

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentChangePasswordBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment: Fragment() {

    private var viewBinding: FragmentChangePasswordBinding? = null
    private val firebaseAuth: FirebaseAuth = FirestoreRepository.getFirebaseAuthInstance()

    private val buttonAnimatorListener = object : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            val email = firebaseAuth.currentUser?.email
            firebaseAuth.sendPasswordResetEmail(email.toString()).addOnSuccessListener {
                onSendPasswordResetEmailSuccess()
            }.addOnFailureListener {
                Toast.makeText(context, getString(R.string.change_password_send_email_failure), Toast.LENGTH_SHORT).show()
            }
        }

        override fun onAnimationEnd(animation: Animator?) {
            //no-op
        }

        override fun onAnimationCancel(animation: Animator?) {
            //no-op
        }

        override fun onAnimationRepeat(animation: Animator?) {
            //no-op
        }

    }

    private fun onSendPasswordResetEmailSuccess() {
        (activity as MainActivity).logOut()
    }


    private fun setupOnClickListeners() {
        viewBinding?.fragmentChangePasswordButtonSendEmail?.setOnClickListener {
            if (viewBinding?.fragmentChangePasswordButtonSendEmail?.isAnimating == false) {
                viewBinding?.fragmentChangePasswordButtonSendEmail?.playAnimation()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding?.fragmentChangePasswordButtonSendEmail?.addAnimatorListener(buttonAnimatorListener)
        setupOnClickListeners()

        super.onViewCreated(view, savedInstanceState)
    }
}