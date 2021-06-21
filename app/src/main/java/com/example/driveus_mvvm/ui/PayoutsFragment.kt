package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.databinding.FragmentPayoutsBinding

class PayoutsFragment : Fragment() {

    private var viewBinding: FragmentPayoutsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentPayoutsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }


}