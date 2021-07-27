package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentPayoutsDebtsBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.DebtListAdapter
import com.example.driveus_mvvm.view_model.PayoutViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage

class PayoutDebtsFragment: Fragment() {

    private var viewBinding: FragmentPayoutsDebtsBinding? = null
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()
    private val payoutViewModel : PayoutViewModel by lazy { ViewModelProvider(this)[PayoutViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val adapterListener = object : DebtListAdapter.DebtListAdapterListener {
        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            firebaseStorage.reference.child("users/$userId").downloadUrl.addOnSuccessListener {
                Glide.with(this@PayoutDebtsFragment)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }.addOnFailureListener {
                Glide.with(this@PayoutDebtsFragment)
                    .load(R.drawable.ic_action_name)
                    .circleCrop()
                    .into(imageView)

                Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
            }        }

        override fun pressCheckbox(payoutDocSnap: DocumentSnapshot, checkBox: CheckBox) {
            val channelId = payoutDocSnap.reference.parent.parent?.parent?.parent?.id
            val rideId = payoutDocSnap.reference.parent.parent?.id

            if (channelId != null && rideId != null) {
                payoutViewModel.checkDebtAsPaid(channelId, rideId, payoutDocSnap.reference, payoutDocSnap.get("passenger") as DocumentReference)
            }
        }

        override fun amIThePassenger(passengerId: String): Boolean {
            var res = false
            val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            if (userId == passengerId) {
                res = true
            }
            return res
        }
    }

    private fun debtsAsPassengerObserver(adapter: DebtListAdapter) = Observer<List<Pair<String, DocumentSnapshot>>> { docSnapList ->
        adapter.submitList(docSnapList)
    }

    private fun debtsAsDriverObserver (adapter: DebtListAdapter) = Observer<List<Pair<String, DocumentSnapshot>>> { docSnapList ->
        adapter.submitList(docSnapList)
    }


    private fun setupRecyclerAdapterPassenger(): DebtListAdapter{
        val adapter = DebtListAdapter(adapterListener)

        viewBinding?.fragmentPayoutDebtListPayoutsListPassenger?.adapter = adapter
        viewBinding?.fragmentPayoutDebtListPayoutsListPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupRecyclerAdapterDriver(): DebtListAdapter{
        val adapter = DebtListAdapter(adapterListener)

        viewBinding?.fragmentPayoutDebtListPayoutsListDriver?.adapter = adapter
        viewBinding?.fragmentPayoutDebtListPayoutsListDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentPayoutsDebtsBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterPassenger = setupRecyclerAdapterPassenger()
        val adapterDriver = setupRecyclerAdapterDriver()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let {
                payoutViewModel.getDebtsAsPassenger(it).observe(viewLifecycleOwner, debtsAsPassengerObserver(adapterPassenger))
                payoutViewModel.getDebtsAsDriver(it).observe(viewLifecycleOwner, debtsAsDriverObserver(adapterDriver))
            }
    }
}