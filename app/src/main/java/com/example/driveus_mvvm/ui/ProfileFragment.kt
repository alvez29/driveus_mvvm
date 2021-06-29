package com.example.driveus_mvvm.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentProfileBinding
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.ui.adapter.VehicleListAdapter
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot

class ProfileFragment : Fragment() {

    private var viewBinding: FragmentProfileBinding? = null
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val userObserver = Observer<DocumentSnapshot> { document ->
        val user: User? = document.toObject(User::class.java)
        val fullName = user?.name + " " + user?.surname
        viewBinding?.profileFragmentLabelUsername?.text = user?.username
        viewBinding?.profileFragmentLabelFullName?.text = fullName
        viewBinding?.profileFragmentLabelEmail?.text = user?.email

        val adapter = VehicleListAdapter()
        viewBinding?.fragmentProfileRecycleView?.adapter = adapter
        viewBinding?.fragmentProfileRecycleView?.layoutManager = LinearLayoutManager(context)
        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { viewModel.getVehiclesByUserId(it).observe(viewLifecycleOwner, vehiclesObserver(adapter)) }

        if (user?.isDriver == false) {
            viewBinding?.fragmentProfileLayoutCars?.visibility = View.GONE
            viewBinding?.fragmentProfileLayoutNoCars?.visibility = View.VISIBLE
        } else {
            viewBinding?.fragmentProfileLayoutCars?.visibility = View.VISIBLE
            viewBinding?.fragmentProfileLayoutNoCars?.visibility = View.GONE
        }
    }

    private fun vehiclesObserver(adapter: VehicleListAdapter) = Observer<Map<String, Vehicle>> { vehicles ->
        vehicles?.let {
            adapter.submitList(it.toList())
        }
    }

    private fun logOut() {
        viewBinding?.profileFragmentButtonLogOut?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val authIntent = Intent(activity, AuthActivity::class.java)
            startActivity(authIntent)
        }
    }

    private fun addNewCar() {
        viewBinding?.profileFragmentButtonAddCar?.setOnClickListener {
            val actionNoDriver = ProfileFragmentDirections.actionProfileFragmentToAddCarFragment2()
            findNavController().navigate(actionNoDriver)
        }

        viewBinding?.profileFragmentButtonAddCarDriver?.setOnClickListener {
            val actionDriver = ProfileFragmentDirections.actionProfileFragmentToAddCarFragment2()
            findNavController().navigate(actionDriver)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentProfileBinding.inflate(inflater, container, false)

        FirebaseAuth.getInstance().currentUser?.let {
            viewModel.getUserByUid(it.uid).observe(viewLifecycleOwner, userObserver)
        }

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logOut()
        addNewCar()
    }
}