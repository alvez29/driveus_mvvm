package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentAllChannelsBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.ui.adapter.AllChannelsListAdapter
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import java.util.*

class AllChannelsFragment : Fragment() {

    private var viewBinding: FragmentAllChannelsBinding? = null
    private val channelViewModel : ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }
    private val userUID : String? by lazy { FirebaseAuth.getInstance().currentUser?.uid }

    private val allChannelsListAdapterListener = object : AllChannelsListAdapter.AllChannelsListAdapterListener {

        override fun onItemClick(channelDocId: String) {
            val action = ChannelsFragmentDirections
                    .actionChannelsFragmentToChannelDetailFragment()
                    .setChannelId(channelDocId)

            NavHostFragment.findNavController(this@AllChannelsFragment)
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

        //TODO: Añadir la forma de mostrar es favorito o no

    }

    private fun queryTextListener(channels: Map<String, Channel>, adapter: AllChannelsListAdapter): SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            var filteredMap: Map<String, Channel> = emptyMap()

            query?.let {
                if (viewBinding?.allChannelsSwitchFilter?.isChecked == true) {
                    filteredMap = channels.filter {
                        it.value.destinationZone?.contains(query, true) == true
                    }

                } else if (viewBinding?.allChannelsSwitchFilter?.isChecked == false) {
                    filteredMap = channels.filter {
                        it.value.originZone?.contains(query, true) == true
                    }
                } else {
                    filteredMap = channels
                }
            }

            adapter.submitList(filteredMap.toList())

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            newText?.let {
                if (newText.trim().isBlank()) {
                    adapter.submitList(channels.toList())
                }
            }

            return true
        }

    }

    private fun configureChannelSearch(channels: Map<String, Channel>, adapter: AllChannelsListAdapter) {
        
        viewBinding?.allChannelsSearchSearchView?.setOnQueryTextListener(queryTextListener(channels, adapter))
        
        viewBinding?.allChannelsSwitchFilter?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewBinding?.allChannelsImageFilterImage?.setImageResource(R.drawable.ic_round_location_on_24)
            } else {
                viewBinding?.allChannelsImageFilterImage?.setImageResource(R.drawable.ic_origin_zone_24)
            }
            
        }

    }

    private fun allChannelsObserver(adapter: AllChannelsListAdapter) = Observer<Map<String, Channel>> {
        //Este método se puede poner más bonito (aún)
        FirebaseAuth.getInstance().currentUser?.let { user ->

            if (it.isEmpty().not()) {
                viewBinding?.allChannelsSearchSearchView?.visibility = View.VISIBLE
                viewBinding?.allChannelsSwitchFilter?.visibility = View.VISIBLE
                viewBinding?.allChannelsImageFilterImage?.visibility = View.VISIBLE
                configureChannelSearch(it, adapter)

            } else {
                viewBinding?.allChannelsSearchSearchView?.visibility = View.GONE
                viewBinding?.allChannelsSwitchFilter?.visibility = View.GONE
                viewBinding?.allChannelsImageFilterImage?.visibility = View.GONE

            }

            adapter.submitList(it.toList())

        }

    }

    private fun setupRecyclerAdapter() : AllChannelsListAdapter {
        val adapter = AllChannelsListAdapter(allChannelsListAdapterListener)

        viewBinding?.allChannelsContainerChannelsRecycler?.adapter = adapter
        viewBinding?.allChannelsContainerChannelsRecycler?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentAllChannelsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = setupRecyclerAdapter()
        channelViewModel.getAllChannels().observe(viewLifecycleOwner, allChannelsObserver(adapter))
    }

}