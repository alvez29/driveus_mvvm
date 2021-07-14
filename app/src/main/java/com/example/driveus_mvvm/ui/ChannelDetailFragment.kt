package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentChannelDetailBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.ui.adapter.MyComingRidesListAdapter
import com.example.driveus_mvvm.ui.adapter.RidesListAdapter
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.google.firebase.storage.FirebaseStorage

class ChannelDetailFragment : Fragment() {

    private var viewBinding: FragmentChannelDetailBinding? = null
    private val channelId by lazy { arguments?.getString("channelId") }
    private val viewModel : ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }

    private val channelObserver = Observer<Channel> {
        viewBinding?.channelDetailLabelChannelOriginZone?.text = it.originZone
        viewBinding?.channelDetailLabelChannelDestinationZone?.text = it.destinationZone
    }

    private fun ridesObserver(adapter: RidesListAdapter) = Observer<Map<String, Ride>> { map ->
        adapter.submitList(map.toList().sortedBy { it.second.date })

    }

    private val ridesListAdapterListener = object : RidesListAdapter.RideListAdapterListener {

        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            FirebaseStorage.getInstance().reference.child("users/$userId").downloadUrl.addOnSuccessListener {
                Glide.with(this@ChannelDetailFragment)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }.addOnFailureListener {
                Glide.with(this@ChannelDetailFragment)
                    .load(R.drawable.ic_action_name)
                    .circleCrop()
                    .into(imageView)

                Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
            }
        }

        override fun navigateToRideDetail(rideId: String) {
            val action = channelId?.let {
                ChannelDetailFragmentDirections
                    .actionChannelDetailFragmentToRideDetailFragment()
                    .setRideId(rideId)
                    .setChannelId(it)
            }

            if (action != null) {
                findNavController().navigate(action)
            }
        }

    }

    private fun setupRecyclerAdapter(): RidesListAdapter {
        val adapter = RidesListAdapter(ridesListAdapterListener)

        viewBinding?.channelDetailListRidesList?.adapter = adapter
        viewBinding?.channelDetailListRidesList?.layoutManager = LinearLayoutManager(context)

        return adapter
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChannelDetailBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = setupRecyclerAdapter()

        channelId?.let {
            viewModel.getRidesFromChannel(it).observe(viewLifecycleOwner, ridesObserver(adapter))
            viewModel.getChannelById(it).observe(viewLifecycleOwner, channelObserver)
        }

    }



}