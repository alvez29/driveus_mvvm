package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentPayoutsDebtsBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.DebtListAdapter
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.ui.utils.NetworkUtils
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
        override fun loadProfilePicture(passengerId: String?, driverId: String?, imageView: ImageView) {
            var userId = ""
            if (sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "") == passengerId) {
                if (driverId != null) {
                    userId = driverId
                }
            } else {
                if (passengerId != null) {
                    userId = passengerId
                }
            }

            context?.let { ImageUtils.loadProfilePicture(userId, imageView, it, firebaseStorage ) }
        }

        override fun pressCheckbox(payoutDocSnap: DocumentSnapshot, checkBox: CheckBox) {
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()
                checkBox.isChecked = false

            } else {
                val channelId = payoutDocSnap.reference.parent.parent?.parent?.parent?.id
                val rideId = payoutDocSnap.reference.parent.parent?.id

                if (channelId != null && rideId != null) {
                    payoutViewModel.checkDebtAsPaid(channelId, rideId, payoutDocSnap.reference, payoutDocSnap.get("passenger") as DocumentReference)
                }
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

        override fun navigateToRideDetail(payoutDocSnap: DocumentSnapshot) {
            val channelId = payoutDocSnap.reference.parent.parent?.parent?.parent?.id
            val rideId = payoutDocSnap.reference.parent.parent?.id

            if (channelId != null && rideId != null) {
                val action = PayoutDebtsFragmentDirections.actionPayoutDebtsFragmentToRideDetailFragment()
                        .setChannelId(channelId)
                        .setRideId(rideId)

                findNavController().navigate(action)
            }
        }

        override fun getUsername(passengerId:String, passengerUsername: String?, driverUsername: String?): String {
            var res = ""
            val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            if (userId == passengerId) {
                if (driverUsername != null) {
                    res = driverUsername
                }
            } else {
                if (passengerUsername != null) {
                    res = passengerUsername
                }
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