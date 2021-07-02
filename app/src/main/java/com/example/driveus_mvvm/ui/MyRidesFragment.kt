package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyRidesBinding
import com.example.driveus_mvvm.ui.adapter.MyRidesViewPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MyRidesFragment : Fragment() {

    private var viewBinding: FragmentMyRidesBinding? = null

    private fun setupTabLayoutMediator() {
        viewBinding?.myRidesFragmentContainerTabLayout?.let { tabLayout ->
            viewBinding?.myRidesFragmentContainerChannelsViewPager2?.let { viewPager2 ->
                TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                    when(position) {
                        0 -> {
                            tab.text = getString(R.string.my_rides_fragment__label__tab_coming_rides)
                            tab.icon = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_round_timer_24) }
                        }
                        1 -> {
                            tab.text = getString(R.string.my_rides_fragment__label__tab_record)
                            tab.icon = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_round_feed_24) }
                        }
                    }
                }.attach()
            }
        }
    }

    private fun attachViewPagerAdapter() {
        val viewPagerAdapter = MyRidesViewPagerAdapter(this)
        viewBinding?.myRidesFragmentContainerChannelsViewPager2?.adapter = viewPagerAdapter
    }

    //TODO: Revisar los colores
    private fun setupTabColor() {
        val tabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                context?.let { ContextCompat.getColor(it, R.color.teal_300) }
                        ?.let { tab?.icon?.setTint(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                context?.let { ContextCompat.getColor(it, R.color.material_on_background_disabled) }
                        ?.let { tab?.icon?.setTint(it) }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        }

        viewBinding?.myRidesFragmentContainerTabLayout?.addOnTabSelectedListener(tabSelectedListener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyRidesBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachViewPagerAdapter()
        setupTabLayoutMediator()
        setupTabColor()

    }



}