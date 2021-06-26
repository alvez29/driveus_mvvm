package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.databinding.FragmentChannelDetailBinding
import com.example.driveus_mvvm.databinding.FragmentMyRidesBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.view_model.ChannelViewModel

class ChannelDetailFragment : Fragment() {

    private var viewBinding: FragmentChannelDetailBinding? = null
    private val channelId by lazy { arguments?.getString("channelId") }
    private val viewModel : ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }

    private val channelObserver = Observer<Channel> {
        //TODO: Bind del canal en la vista
    }

    private val ridesObserver = Observer<Map<String, Ride>> {
        //TODO: Bind de los canales en la vista
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChannelDetailBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelId?.let {
            viewModel.getRidesFromChannel(it).observe(viewLifecycleOwner, ridesObserver)
            viewModel.getChannelById(it).observe(viewLifecycleOwner, channelObserver)
        }

    }


}