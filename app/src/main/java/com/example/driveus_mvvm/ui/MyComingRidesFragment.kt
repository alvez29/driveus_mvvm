package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyComingRidesBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.MyComingRidesListAdapter
import com.example.driveus_mvvm.view_model.RideViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyComingRidesFragment : Fragment() {

    private var viewBinding: FragmentMyComingRidesBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()

    private val myComingRidesListAdapterListener = object : MyComingRidesListAdapter.MyComingRideListAdapterListener {

        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            firebaseStorage.reference.child("users/$userId").downloadUrl.addOnSuccessListener {
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

    private fun myComingRidesAsPassengerObserver(adapter: MyComingRidesListAdapter) = Observer<List<DocumentSnapshot>> {
        if (it.isEmpty()){
            viewBinding?.myComingRidesFragmentListRecyclerAsPassenger?.visibility = View.GONE
            viewBinding?.myComingRidesFragmentContainerNoRidesLinearLayoutPassenger?.visibility = View.VISIBLE
        } else {
            viewBinding?.myComingRidesFragmentListRecyclerAsPassenger?.visibility = View.VISIBLE
            viewBinding?.myComingRidesFragmentContainerNoRidesLinearLayoutPassenger?.visibility = View.GONE
        }
        adapter.submitList(it.toList().sortedBy { it.getTimestamp("date") })
    }

    private fun myComingRidesAsDriverObserver(adapter: MyComingRidesListAdapter) = Observer<List<DocumentSnapshot>> {
        if (it.isEmpty()){
            viewBinding?.myComingRidesFragmentListRecyclerAsDriver?.visibility = View.GONE
            viewBinding?.myComingRidesFragmentContainerNoRidesLinearLayoutDriver?.visibility = View.VISIBLE
        } else {
            viewBinding?.myComingRidesFragmentListRecyclerAsPassenger?.visibility = View.VISIBLE
            viewBinding?.myComingRidesFragmentContainerNoRidesLinearLayoutDriver?.visibility = View.GONE
        }
        adapter.submitList(it.toList().sortedBy { it.getTimestamp("date") })

    }

    private fun setupRecyclerAsDriverAdapter() : MyComingRidesListAdapter {
        val adapter = MyComingRidesListAdapter(myComingRidesListAdapterListener)

        viewBinding?.myComingRidesFragmentListRecyclerAsDriver?.adapter = adapter
        viewBinding?.myComingRidesFragmentListRecyclerAsDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupRecyclerAsPassengerAdapter() : MyComingRidesListAdapter {
        val adapter = MyComingRidesListAdapter(myComingRidesListAdapterListener)

        viewBinding?.myComingRidesFragmentListRecyclerAsPassenger?.adapter = adapter
        viewBinding?.myComingRidesFragmentListRecyclerAsPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }


    private fun setupFloatingButton() {
        viewBinding?.myComingRidesFragmentButtonRoleButton?.setOnClickListener {
            viewBinding?.myComingRidesFragmentButtonFabChangeRole?.playAnimation()

            val drawableWheel = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_steering_wheel_24) }
            val drawableSeat = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_round_event_seat_24) }

            if (viewBinding?.myComingRidesFragmentContainerDriver?.isShown == true) {
                viewBinding?.myComingRidesFragmentContainerPassenger?.visibility = View.VISIBLE
                viewBinding?.myComingRidesFragmentContainerDriver?.visibility = View.GONE
                viewBinding?.myComingRidesFragmentButtonRoleButton
                        ?.setCompoundDrawablesWithIntrinsicBounds(drawableSeat, null, null, null)
                viewBinding?.myComingRidesFragmentButtonRoleButton?.text = getString(R.string.my_rides_record_list__label__role_button_passenger)

            } else {
                viewBinding?.myComingRidesFragmentContainerPassenger?.visibility = View.GONE
                viewBinding?.myComingRidesFragmentContainerDriver?.visibility = View.VISIBLE
                viewBinding?.myComingRidesFragmentButtonRoleButton
                        ?.setCompoundDrawablesWithIntrinsicBounds(drawableWheel, null, null, null)
                viewBinding?.myComingRidesFragmentButtonRoleButton?.text = getString(R.string.my_rides_record_list__label__role_button_driver)
            }
        }

        viewBinding?.myComingRidesFragmentButtonFabChangeRole?.setOnClickListener {
            viewBinding?.myComingRidesFragmentButtonRoleButton?.callOnClick()
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
                ?.let {
                    rideViewModel.getComingRidesAsPassenger(it).observe(viewLifecycleOwner, myComingRidesAsPassengerObserver(adapterAsPassenger))
                }

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                ?.let {
                    rideViewModel.getComingRidesAsDriver(it).observe(viewLifecycleOwner, myComingRidesAsDriverObserver(adapterAsDriver))
                }

        setupFloatingButton()
    }

}