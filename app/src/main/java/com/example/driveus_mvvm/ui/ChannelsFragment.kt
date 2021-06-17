package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.databinding.FragmentChannelsBinding

class ChannelsFragment : Fragment() {

    private var viewBinding: FragmentChannelsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChannelsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

}