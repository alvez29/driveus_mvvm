package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentPayoutsBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.PayoutListAdapter
import com.example.driveus_mvvm.view_model.PayoutViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage

class PayoutsFragment : Fragment() {

    private var viewBinding: FragmentPayoutsBinding? = null
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()
    private val payoutViewModel : PayoutViewModel by lazy { ViewModelProvider(this)[PayoutViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val adapterListener = object : PayoutListAdapter.PayoutListAdapterListener {
        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            firebaseStorage.reference.child("users/$userId").downloadUrl.addOnSuccessListener {
                Glide.with(this@PayoutsFragment)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }.addOnFailureListener {
                Glide.with(this@PayoutsFragment)
                    .load(R.drawable.ic_action_name)
                    .circleCrop()
                    .into(imageView)

                Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
            }
        }

        override fun pressCheckbox(payoutDocSnap: DocumentSnapshot, checkBox: CheckBox) {
            val channelId = payoutDocSnap.reference.parent.parent?.parent?.parent?.id
            val rideId = payoutDocSnap.reference.parent.parent?.id

            if (channelId != null && rideId != null) {
                payoutViewModel.checkPayoutAsPaid(channelId, rideId, payoutDocSnap.reference, payoutDocSnap.get("passenger") as DocumentReference)
            }
        }

        override fun amITheDriver(passengerId: String): Boolean {
            var res = false
            val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            if (userId != passengerId) {
                res = true
            }
            return res
        }
    }

    private fun payoutsAsPassengerObserver(adapter: PayoutListAdapter) = Observer<List<Pair<String, DocumentSnapshot>>> { docSnapList ->
        adapter.submitList(docSnapList)
    }

    private fun payoutsAsDriverObserver(adapter: PayoutListAdapter) = Observer<List<Pair<String, DocumentSnapshot>>> { docSnapList ->
        adapter.submitList(docSnapList.toList())
    }

    private fun setupRecyclerAdapterPassenger() : PayoutListAdapter {
        val adapter = PayoutListAdapter(adapterListener)

        viewBinding?.fragmentPayoutListPayoutsListPassenger?.adapter = adapter
        viewBinding?.fragmentPayoutListPayoutsListPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupRecyclerAdapterDriver() : PayoutListAdapter {
        val adapter = PayoutListAdapter(adapterListener)

        viewBinding?.fragmentPayoutListPayoutsListDriver?.adapter = adapter
        viewBinding?.fragmentPayoutListPayoutsListDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupButtonRole() {
        val drawableWheel = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_steering_wheel_24) }
        val drawableSeat = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_round_event_seat_24) }

        viewBinding?.fragmentPayoutButtonRoleButton?.setOnClickListener {
            if (viewBinding?.fragmentPayoutListPayoutsListPassenger?.visibility == View.VISIBLE) {
                viewBinding?.fragmentPayoutButtonRoleButton?.text = getString(R.string.payouts_fragment_button__label__driver)
                viewBinding?.fragmentPayoutButtonRoleButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableWheel, null, null, null)
                viewBinding?.fragmentPayoutListPayoutsListPassenger?.visibility = View.GONE
                viewBinding?.fragmentPayoutListPayoutsListDriver?.visibility = View.VISIBLE
            } else {
                viewBinding?.fragmentPayoutButtonRoleButton?.text = getString(R.string.my_rides_record_list__label__role_button_passenger)
                viewBinding?.fragmentPayoutButtonRoleButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableSeat, null, null, null)

                viewBinding?.fragmentPayoutListPayoutsListPassenger?.visibility = View.VISIBLE
                viewBinding?.fragmentPayoutListPayoutsListDriver?.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentPayoutsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterPassenger = setupRecyclerAdapterPassenger()
        val adapterDriver = setupRecyclerAdapterDriver()

        setupButtonRole()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key),"")
            ?.let {
                payoutViewModel.getPendingPayoutsAsPassengerFromUser(it).observe(viewLifecycleOwner, payoutsAsPassengerObserver(adapterPassenger))
                payoutViewModel.getPendingPayoutsAsDriverFromUser(it).observe(viewLifecycleOwner, payoutsAsDriverObserver(adapterDriver))
            }
    }

}