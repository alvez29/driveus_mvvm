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
import com.example.driveus_mvvm.databinding.FragmentMyRidesRecordBinding
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.ui.adapter.MyRidesRecordListAdapter
import com.example.driveus_mvvm.view_model.RideViewModel
import com.google.firebase.storage.FirebaseStorage

class MyRidesRecordFragment : Fragment() {

    private var viewBinding: FragmentMyRidesRecordBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val myRidesRecordListAdapterListener = object:MyRidesRecordListAdapter.MyRidesRecordListAdapterListener {
        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            FirebaseStorage.getInstance().reference.child("users/$userId").downloadUrl.addOnSuccessListener {
                Glide.with(this@MyRidesRecordFragment)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }.addOnFailureListener {
                Glide.with(this@MyRidesRecordFragment)
                    .load(R.drawable.ic_action_name)
                    .circleCrop()
                    .into(imageView)

                Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
            }
        }
    }

    private fun myRidesRecordAsPassengerObserver(adapter: MyRidesRecordListAdapter) = Observer<Map<String, Ride>> {
        if(it.isEmpty()) {
            viewBinding?.myRidesRecordListRecyclerAsPassenger?.visibility = View.GONE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutPassenger?.visibility = View.VISIBLE
        } else {
            viewBinding?.myRidesRecordListRecyclerAsPassenger?.visibility = View.VISIBLE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutPassenger?.visibility = View.GONE
        }
        adapter.submitList(it.toList().sortedBy { pair -> pair.second.date })
    }

    private fun myRidesRecordAsDriverObserver(adapter: MyRidesRecordListAdapter) = Observer<Map<String, Ride>> {
        if(it.isEmpty()) {
            viewBinding?.myRidesRecordListRecyclerAsDriver?.visibility = View.GONE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutDriver?.visibility = View.VISIBLE
        } else {
            viewBinding?.myRidesRecordListRecyclerAsDriver?.visibility = View.VISIBLE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutDriver?.visibility = View.GONE
        }
        adapter.submitList(it.toList().sortedBy { pair -> pair.second.date })
    }

    private fun setUpRecyclerAsPassengerAdapter(): MyRidesRecordListAdapter {
        val adapter = MyRidesRecordListAdapter(myRidesRecordListAdapterListener)

        viewBinding?.myRidesRecordListRecyclerAsPassenger?.adapter = adapter
        viewBinding?.myRidesRecordListRecyclerAsPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setUpRecyclerAsDriverAdapter(): MyRidesRecordListAdapter {
        val adapter = MyRidesRecordListAdapter(myRidesRecordListAdapterListener)

        viewBinding?.myRidesRecordListRecyclerAsDriver?.adapter = adapter
        viewBinding?.myRidesRecordListRecyclerAsDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }


    private fun setupFloatingButton() {
        viewBinding?.myRidesRecordListButtonFloatingButton?.setOnClickListener {
            if (viewBinding?.myRidesRecordContainerDriver?.isShown == true) {
                viewBinding?.myRidesRecordContainerDriver?.visibility = View.GONE
                viewBinding?.myRidesRecordContainerPassenger?.visibility = View.VISIBLE
                viewBinding?.myRidesRecordListButtonFloatingButton?.setImageResource(R.drawable.ic_round_event_seat_24)
            } else {
                viewBinding?.myRidesRecordContainerDriver?.visibility = View.VISIBLE
                viewBinding?.myRidesRecordContainerPassenger?.visibility = View.GONE
                viewBinding?.myRidesRecordListButtonFloatingButton?.setImageResource(R.drawable.ic_steering_wheel_24)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyRidesRecordBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterAsPassenger = setUpRecyclerAsPassengerAdapter()
        val adapterAsDriver = setUpRecyclerAsDriverAdapter()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { rideViewModel.getRidesAsPassenger(it).observe(viewLifecycleOwner, myRidesRecordAsPassengerObserver(adapterAsPassenger)) }

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { rideViewModel.getRidesAsDriver(it).observe(viewLifecycleOwner, myRidesRecordAsDriverObserver(adapterAsDriver)) }

        setupFloatingButton()
    }

}