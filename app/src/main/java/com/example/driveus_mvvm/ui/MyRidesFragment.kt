package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyRidesBinding
import com.example.driveus_mvvm.ui.adapter.MyRidesViewPagerAdapter
import com.example.driveus_mvvm.ui.utils.HelpMenu
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_bar_my_rides, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_top_bar_my_rides__item__help -> {
                val text1 = R.string.dialog_help_my_rides1
                val text2 = R.string.dialog_help_my_rides2
                val text3 = R.string.dialog_help_my_rides3
                val textList: List<Int> = listOf(text1, text2, text3)
                context?.let { HelpMenu.displayHelpMenu(it, textList) }
            }
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyRidesBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachViewPagerAdapter()
        setupTabLayoutMediator()

    }



}