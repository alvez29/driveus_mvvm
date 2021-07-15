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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyRidesRecordBinding
import com.example.driveus_mvvm.ui.adapter.MyRidesRecordListAdapter
import com.example.driveus_mvvm.view_model.RideViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage

class MyRidesRecordFragment : Fragment() {

    private var viewBinding: FragmentMyRidesRecordBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val myRidesRecordListAdapterListener = object : MyRidesRecordListAdapter.MyRidesRecordListAdapterListener {
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

        override fun navigateToRideDetail(rideId: String, channelId: String?) {
            val action = channelId?.let {
                MyRidesFragmentDirections
                        .actionMyRidesFragmentToRideDetailFragment()
                        .setRideId(rideId)
                        .setChannelId(it)
            }

            if (action != null) {
                findNavController().navigate(action)
            }
        }

    }

    private fun myRidesRecordAsPassengerObserver(adapter: MyRidesRecordListAdapter): Observer<in List<DocumentSnapshot>> = Observer<List<DocumentSnapshot>> { docSnap ->
        adapter.submitList(docSnap.sortedBy { it.getTimestamp("date") })
    }

    private fun myRidesRecordAsDriverObserver(adapter: MyRidesRecordListAdapter): Observer<in List<DocumentSnapshot>> = Observer<List<DocumentSnapshot>> { docSnap ->
        adapter.submitList(docSnap.sortedBy { it.getTimestamp("date") })
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
            if (viewBinding?.myRidesRecordListRecyclerAsDriver?.isShown == true) {
                viewBinding?.myRidesRecordListRecyclerAsDriver?.visibility = View.GONE
                viewBinding?.myRidesRecordListRecyclerAsPassenger?.visibility = View.VISIBLE
                viewBinding?.myRidesRecordListButtonFloatingButton?.setImageResource(R.drawable.ic_round_event_seat_24)
            } else {
                viewBinding?.myRidesRecordListRecyclerAsDriver?.visibility = View.VISIBLE
                viewBinding?.myRidesRecordListRecyclerAsPassenger?.visibility = View.GONE
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