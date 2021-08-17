package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentAllChannelsBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.ui.adapter.AllChannelsListAdapter
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.google.firebase.firestore.DocumentReference

class AllChannelsFragment : Fragment() {

    private var viewBinding: FragmentAllChannelsBinding? = null
    private val channelViewModel: ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }
    private var filterMode = false

    private val allChannelsListAdapterListener = object : AllChannelsListAdapter.AllChannelsListAdapterListener {

        override fun onItemClick(channelDocId: String) {
            val action = ChannelsFragmentDirections
                    .actionChannelsFragmentToChannelDetailFragment()
                    .setChannelId(channelDocId)

            NavHostFragment.findNavController(this@AllChannelsFragment)
                    .navigate(action)
        }

        override fun onSubscribeClick(channelDocId: String) {
            sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")?.let {
                channelViewModel.subscribeToChannel(it, channelDocId)
            }
        }

        override fun onUnsubscribeClick(channelDocId: String) {
            sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")?.let {
                channelViewModel.unsubscribeToChannel(it, channelDocId)
            }
        }

        override fun isSubscribed(usersList: List<DocumentReference?>): Boolean {
            val docId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key),"")

            return usersList.map { it?.id }.contains(docId)
        }

    }

    private fun queryTextListener(channels: Map<String, Channel>, adapter: AllChannelsListAdapter): SearchView.OnQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            var filteredMap: Map<String, Channel> = emptyMap()

            query?.let {
                when (viewBinding?.allChannelsSwitchFilter?.isChecked) {
                    true -> {
                        filteredMap = channels.filter {
                            it.value.destinationZone?.contains(query, true) == true
                        }

                    }
                    false -> {
                        filteredMap = channels.filter {
                            it.value.originZone?.contains(query, true) == true
                        }
                    }
                    else -> {
                        filteredMap = channels
                    }
                }
            }
            adapter.submitList(filteredMap.toList())

            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            newText?.let {
                if (newText.trim().isBlank()) {
                    filterMode = false
                    adapter.submitList(channels.toList())
                } else {
                    filterMode = true
                }
            }

            return true
        }

    }

    private fun checkEditMode() {
        //Si la lista cambia mientras estamos en el filtro se ejecuta una nueva búsqueda
        if (filterMode) {
            val queryString = viewBinding?.allChannelsSearchSearchView?.query.toString()
            viewBinding?.allChannelsSearchSearchView?.setQuery(queryString, true)
        }
    }

    private fun configureChannelSearch(channels: Map<String, Channel>, adapter: AllChannelsListAdapter) {
        
        viewBinding?.allChannelsSearchSearchView?.setOnQueryTextListener(queryTextListener(channels, adapter))
        
        viewBinding?.allChannelsSwitchFilter?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewBinding?.allChannelsImageFilterImage?.setImageResource(R.drawable.ic_pink_round_location_on_24)
            } else {
                viewBinding?.allChannelsImageFilterImage?.setImageResource(R.drawable.ic_origin_zone_24)
            }

            checkEditMode()
        }

    }

    private fun allChannelsObserver(adapter: AllChannelsListAdapter) = Observer<Map<String, Channel>> {
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
        checkEditMode()
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