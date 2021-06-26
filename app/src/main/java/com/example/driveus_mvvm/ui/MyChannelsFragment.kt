package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.databinding.FragmentMyChannelsBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.ui.adapter.AllChannelsListAdapter
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference

class MyChannelsFragment : Fragment() {

    private var viewBinding: FragmentMyChannelsBinding? = null
    private val channelViewModel : ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }
    private val userUID : String? by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    private fun myChannelsObserver(adapter: AllChannelsListAdapter) = Observer<Map<String, Channel>> {
        adapter.submitList(it.toList())
    }

    private val allChannelsListAdapterListener = object : AllChannelsListAdapter.AllChannelsListAdapterListener {

        override fun onItemClick(channelDocId: String) {
            val action = ChannelsFragmentDirections
                    .actionChannelsFragmentToChannelDetailFragment()
                    .setChannelId(channelDocId)

            NavHostFragment.findNavController(this@MyChannelsFragment)
                    .navigate(action)
        }

        override fun onSubscribeClick(channelDocId: String) {
            userUID?.let {
                channelViewModel.suscribeToChannel(channelDocId, it)
            }
        }

        override fun onUnsubscribeClick(channelDocId: String) {
            userUID?.let {
                channelViewModel.unsuscribeToChannel(channelDocId, it)
            }
        }

    }

    private fun setupRecyclerAdapter() : AllChannelsListAdapter {
        val adapter = AllChannelsListAdapter(allChannelsListAdapterListener)

        viewBinding?.myChannelsFragmentContainerChannelsRecycler?.adapter = adapter
        viewBinding?.myChannelsFragmentContainerChannelsRecycler?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyChannelsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = setupRecyclerAdapter()

        FirebaseAuth.getInstance().currentUser?.let {
            channelViewModel.getUserChannels(it.uid).observe(viewLifecycleOwner, myChannelsObserver(adapter))
        }
    }
}