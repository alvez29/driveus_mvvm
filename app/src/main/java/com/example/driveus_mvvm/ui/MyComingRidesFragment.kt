package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyComingRidesBinding
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.ui.adapter.MyComingRidesListAdapter
import com.example.driveus_mvvm.view_model.RideViewModel
import com.google.firebase.storage.FirebaseStorage

class MyComingRidesFragment : Fragment() {

    private var viewBinding: FragmentMyComingRidesBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val myComingRidesListAdapterListener = object : MyComingRidesListAdapter.MyComingRideListAdapterListener {

        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            FirebaseStorage.getInstance().reference.child("users/$userId").downloadUrl.addOnSuccessListener {
                Glide.with(this@MyComingRidesFragment)
                        .load(it)
                        .circleCrop()
                        .into(imageView)
            }.addOnFailureListener {
                Glide.with(this@MyComingRidesFragment)
                        .load(R.drawable.ic_action_name)
                        .circleCrop()
                        .into(imageView)

                Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
            }
        }

    }

    private fun myComingRidesAsPassengerObserver(adapter: MyComingRidesListAdapter) = Observer<Map<String, Ride>> {
        adapter.submitList(it.toList().sortedBy { pair -> pair.second.date })

    }

    private fun myComingRidesAsDriverObserver(adapter: MyComingRidesListAdapter) = Observer<Map<String, Ride>> {
        adapter.submitList(it.toList().sortedBy { pair -> pair.second.date })

    }

    private fun setupRecyclerAsDriverAdapter() : MyComingRidesListAdapter {
        val adapter = MyComingRidesListAdapter(myComingRidesListAdapterListener)

        viewBinding?.myComingRidesListRecyclerAsDriver?.adapter = adapter
        viewBinding?.myComingRidesListRecyclerAsDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupRecyclerAsPassengerAdapter() : MyComingRidesListAdapter {
        val adapter = MyComingRidesListAdapter(myComingRidesListAdapterListener)

        viewBinding?.myComingRidesListRecyclerAsPassenger?.adapter = adapter
        viewBinding?.myComingRidesListRecyclerAsPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupFloatingButton() {
        viewBinding?.myComingRidesListButtonFloatingButton?.setOnClickListener {
            if (viewBinding?.myComingRidesListRecyclerAsDriver?.isShown == true) {
                viewBinding?.myComingRidesListRecyclerAsDriver?.visibility = View.GONE
                viewBinding?.myComingRidesListRecyclerAsPassenger?.visibility = View.VISIBLE
                viewBinding?.myComingRidesListButtonFloatingButton?.setImageResource(R.drawable.ic_round_event_seat_24)
            } else {
                viewBinding?.myComingRidesListRecyclerAsDriver?.visibility = View.VISIBLE
                viewBinding?.myComingRidesListRecyclerAsPassenger?.visibility = View.GONE
                viewBinding?.myComingRidesListButtonFloatingButton?.setImageResource(R.drawable.ic_steering_wheel_24)
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyComingRidesBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterAsPassenger = setupRecyclerAsPassengerAdapter()
        val adapterAsDriver = setupRecyclerAsDriverAdapter()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                ?.let { rideViewModel.getComingRidesAsPassenger(it).observe(viewLifecycleOwner, myComingRidesAsPassengerObserver(adapterAsPassenger)) }

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                ?.let { rideViewModel.getComingRidesAsDriver(it).observe(viewLifecycleOwner, myComingRidesAsDriverObserver(adapterAsDriver)) }

        setupFloatingButton()

    }

}