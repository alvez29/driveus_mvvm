package com.example.driveus_mvvm.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.driveus_mvvm.ui.*

class MyRidesViewPagerAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        var fragment: Fragment = MyComingRidesFragment()

        when(position){
            0 -> {
                fragment = MyComingRidesFragment()
            }
            1 -> {
                fragment = MyRidesRecordFragment()
            }
        }

        return fragment
    }
}