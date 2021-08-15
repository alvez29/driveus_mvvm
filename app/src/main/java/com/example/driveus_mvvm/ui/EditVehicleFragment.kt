package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentEditVehicleBinding
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.ui.enums.VehicleFormEnum
import com.example.driveus_mvvm.view_model.UserViewModel

class EditVehicleFragment : Fragment() {

        private var viewBinding: FragmentEditVehicleBinding? = null
        private val userViewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
        private val vehicleId: String? by lazy { arguments?.getString("vehicleId") }
        private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

        private fun vehicleObserver(userId: String) = Observer<Vehicle> { vehicle ->
            if (vehicle != null) {
                viewBinding?.fragmentEditVehicleInputColorEdit?.setText(vehicle.color)
                viewBinding?.fragmentEditVehicleInputDescripcionEdit?.setText(vehicle.description)

                viewBinding?.fragmentEditVehicleButtonEditCar?.setOnClickListener {
                    val inputs = getInputs()
                    vehicleId?.let { vId ->
                        userViewModel.editVehicle(userId, vId, vehicle, inputs)
                    }
                }
            }

        }

        private val formErrorObserver = Observer<Map<VehicleFormEnum, Int>> {
            it.forEach { (k,v) ->
                when(k) {
                    VehicleFormEnum.COLOR -> viewBinding?.fragmentEditVehicleInputColorEdit?.error = getString(v)
                    VehicleFormEnum.DESCRIPTION -> viewBinding?.fragmentEditVehicleInputDescripcionEdit?.error = getString(v)
                }
            }
        }

        private val redirectObserver = Observer<Boolean> {
            if (it) {
                val action = EditVehicleFragmentDirections.actionEditVehicleFragmentToProfileFragment()
                findNavController().navigate(action)
            }
        }

        private fun getInputs(): MutableMap<VehicleFormEnum, String> {
            return mutableMapOf(
                    VehicleFormEnum.COLOR to viewBinding?.fragmentEditVehicleInputColorEdit?.text.toString(),
                    VehicleFormEnum.DESCRIPTION to viewBinding?.fragmentEditVehicleInputDescripcionEdit?.text.toString())
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            viewBinding = FragmentEditVehicleBinding.inflate(inflater, container, false)

            return viewBinding?.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")

            vehicleId?.let {
                if (userId != null) {
                    userViewModel.getVehicleById(it, userId).observe(viewLifecycleOwner, vehicleObserver(userId))
                }
            }

            userViewModel.getFormVehicleErrors().observe(viewLifecycleOwner, formErrorObserver)
            userViewModel.getRedirectVehicle().observe(viewLifecycleOwner, redirectObserver)

        }
}