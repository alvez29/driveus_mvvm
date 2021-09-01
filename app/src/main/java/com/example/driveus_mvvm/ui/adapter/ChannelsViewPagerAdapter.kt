package com.example.driveus_mvvm.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.driveus_mvvm.ui.AllChannelsFragment
import com.example.driveus_mvvm.ui.MyChannelsFragment

class ChannelsViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment = AllChannelsFragment()

        when(position){
            0 -> {
                fragment = MyChannelsFragment()
            }
            1 -> {
                fragment = AllChannelsFragment()
            }
        }

        return fragment
    }
}

