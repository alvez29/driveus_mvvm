package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentChannelsBinding
import com.example.driveus_mvvm.ui.adapter.ChannelsViewPagerAdapter
import com.example.driveus_mvvm.ui.utils.HelpMenu
import com.google.android.material.tabs.TabLayoutMediator

class ChannelsFragment : Fragment() {

    private var viewBinding: FragmentChannelsBinding? = null

    private fun setupTabLayoutMediator() {
        viewBinding?.channelsFragmentContainerTabLayout?.let { tabLayout ->
            viewBinding?.channelsFragmentContainerChannelsViewPager2?.let { viewPager2 ->
                TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
                    when(position) {
                        0 -> {
                            tab.text = getString(R.string.channels_fragment__label__tab_my_channels)
                            tab.icon = context?.let { ContextCompat.getDrawable(it,R.drawable.ic_round_bookmarks_24) }
                        }
                        1 -> {
                            tab.text = getString(R.string.channels_fragment__label__tab_explore)
                            tab.icon = context?.let { ContextCompat.getDrawable(it,R.drawable.ic_round_search_24) }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_bar_channels, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_top_bar_channel__item__help -> {
                val text1 = R.string.dialog_help_channels1
                val text2 = R.string.dialog_help_channels2
                val text3 = R.string.dialog_help_channels3
                val textList: List<Int> = listOf(text1, text2, text3)
                context?.let { HelpMenu.displayHelpMenu(it, textList) }
            }
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChannelsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachViewPagerAdapter()
        setupTabLayoutMediator()

    }

}