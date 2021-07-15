package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.DialogPassengersListBinding
import com.example.driveus_mvvm.view_model.RideViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

class PassengersDialogFragment(
        private val channelId: String,
        private val rideId: String,
        private val viewModel: RideViewModel) : DialogFragment() {

    private var viewBinding: DialogPassengersListBinding? = null

    private fun passengersObserver(adapter: ArrayAdapter<String>?) = Observer<List<DocumentSnapshot>> { docSnapList ->
        adapter?.clear()
        val usernamesList = docSnapList.map { it.getString("username").toString() }.toMutableList()
        adapter?.addAll(usernamesList)
        adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DialogPassengersListBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = parentFragment?.context?.let { ArrayAdapter<String>(it, R.layout.row_passenger, mutableListOf()) }
        viewBinding?.dialogPassengerListPassengers?.adapter = adapter

        viewModel.getPassengersList(channelId, rideId).observe(viewLifecycleOwner, passengersObserver(adapter))
    }

}