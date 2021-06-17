package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var viewBinding: FragmentProfileBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentProfileBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }
}