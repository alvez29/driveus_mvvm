package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyComingRidesBinding
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.ui.adapter.AllChannelsListAdapter
import com.example.driveus_mvvm.ui.adapter.RidesListAdapter
import com.example.driveus_mvvm.view_model.RideViewModel

class MyComingRidesFragment : Fragment() {

    private var viewBinding: FragmentMyComingRidesBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private fun myComingPassengerRidesObserver(adapter: RidesListAdapter) = Observer<Map<String, Ride>> {
        //TODO: Realizar el submist list del adapter
    }

//    private fun setupRecyclerAdapter() : RidesListAdapter {
//        val adapter = RidesListAdapter()
//
//        viewBinding?.myComingRidesListRecycler?.adapter = adapter
//        viewBinding?.myComingRidesListRecycler?.layoutManager = LinearLayoutManager(context)
//
//        return adapter
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyComingRidesBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val adapter = setupRecyclerAdapter()
        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")?.let { rideViewModel.getRidesAsPassenger(it).observe(viewLifecycleOwner, myComingPassengerRidesObserver) }
    }



}