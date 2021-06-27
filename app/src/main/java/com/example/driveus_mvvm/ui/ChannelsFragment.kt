package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentChannelsBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.ChannelsViewPagerAdapter
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class ChannelsFragment : Fragment() {

    private var viewBinding: FragmentChannelsBinding? = null

    private fun setupTabLayoutMediator() {
        viewBinding?.channelsFragmentContainerTabLayout?.let { tabLayout ->
            viewBinding?.channelsFragmentContainerChannelsViewPager2?.let { viewPager2 ->
                TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                    when(position) {
                        0 -> {
                            tab.text = getString(R.string.channels_fragment__label__tab_explore)
                            tab.icon = context?.let { ContextCompat.getDrawable(it,R.drawable.ic_round_search_24) }
                        }
                        1 -> {
                            tab.text = getString(R.string.channels_fragment__label__tab_my_channels)
                            tab.icon = context?.let { ContextCompat.getDrawable(it,R.drawable.ic_round_bookmarks_24) }
                        }
                    }
                }.attach()
            }
        }

    }

    private fun attachViewPagerAdapter() {
        val viewPagerAdapter = ChannelsViewPagerAdapter(this)
        viewBinding?.channelsFragmentContainerChannelsViewPager2?.adapter = viewPagerAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChannelsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachViewPagerAdapter()
        setupTabLayoutMediator()

    }

}