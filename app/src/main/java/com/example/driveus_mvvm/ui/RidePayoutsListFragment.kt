package com.example.driveus_mvvm.ui

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
import com.example.driveus_mvvm.databinding.FragmentRidePayoutsDetailListBinding
import com.example.driveus_mvvm.ui.adapter.PayoutsListAdapter
import com.example.driveus_mvvm.view_model.PayoutViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage

class RidePayoutsListFragment: Fragment() {

    private var viewBinding: FragmentRidePayoutsDetailListBinding? = null
    private val payoutViewModel : PayoutViewModel by lazy { ViewModelProvider(this)[PayoutViewModel::class.java] }
    private val rideId by lazy { arguments?.getString("rideId") }
    private val channelId by lazy { arguments?.getString("channelId") }

    private val adapterListener = object : PayoutsListAdapter.RideListAdapterListener {

        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            FirebaseStorage.getInstance().reference.child("users/$userId").downloadUrl.addOnSuccessListener {
                Glide.with(this@RidePayoutsListFragment)
                        .load(it)
                        .circleCrop()
                        .into(imageView)
            }.addOnFailureListener {
                Glide.with(this@RidePayoutsListFragment)
                        .load(R.drawable.ic_action_name)
                        .circleCrop()
                        .into(imageView)

                Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
            }
        }
    }

    private fun payoutsObserver(adapter: PayoutsListAdapter) = Observer<Map<String, QueryDocumentSnapshot>> { docSnapList ->
        adapter.submitList(docSnapList.toList())

        val totalPrice = docSnapList.map { it.component2() }.sumOf { it.getDouble("price") as Double }
        val totalPriceStr = "$totalPrice €"
        viewBinding?.fragmentRidePayoutsDetailListLabelTotalPrice?.text = totalPriceStr

    }

    private fun setupRecyclerAdapter() : PayoutsListAdapter {
        val adapter = PayoutsListAdapter(adapterListener)

        viewBinding?.ridePayoutsDetailListPayoutsList?.adapter = adapter
        viewBinding?.ridePayoutsDetailListPayoutsList?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentRidePayoutsDetailListBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = setupRecyclerAdapter()

        rideId?.let { rdId ->
            channelId?.let { chnId ->
                payoutViewModel.getPayoutsListFromRide(chnId, rdId).observe(viewLifecycleOwner, payoutsObserver(adapter))
            }
        }

    }
}