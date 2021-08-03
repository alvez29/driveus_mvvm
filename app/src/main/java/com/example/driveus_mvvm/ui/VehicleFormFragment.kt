package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentVehicleFormBinding
import com.example.driveus_mvvm.ui.enums.VehicleFormEnum
import com.example.driveus_mvvm.ui.utils.NetworkUtils
import com.example.driveus_mvvm.view_model.UserViewModel

class VehicleFormFragment : Fragment() {

    private var viewBinding: FragmentVehicleFormBinding? = null
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val formErrorsObserver = Observer<Map<VehicleFormEnum, Int>> {
        it.forEach { (k,v) ->
            when(k) {
                VehicleFormEnum.BRAND -> viewBinding?.fragmentAddCarLabelCarBrandEditText?.error = getString(v)
                VehicleFormEnum.MODEL -> viewBinding?.fragmentAddCarLabelCarModelEditText?.error = getString(v)
                VehicleFormEnum.COLOR -> viewBinding?.fragmentAddCarLabelCarColorEditText?.error = getString(v)
                VehicleFormEnum.SEAT -> viewBinding?.fragmentAddCarLabelCarSeats?.error = getString(v)
                VehicleFormEnum.DESCRIPTION -> viewBinding?.fragmentAddCarLabelCarDescriptionEditText?.error = getString(v)

            }
        }
    }

    private val redirectObserver = Observer<Boolean> {
        if (it) {
            findNavController().popBackStack()
        }
    }


    private fun getInputs() : Map<VehicleFormEnum, String> {
        return mutableMapOf(
            VehicleFormEnum.BRAND to viewBinding?.fragmentAddCarLabelCarBrandEditText?.text.toString(),
            VehicleFormEnum.MODEL to viewBinding?.fragmentAddCarLabelCarModelEditText?.text.toString(),
            VehicleFormEnum.COLOR to viewBinding?.fragmentAddCarLabelCarColorEditText?.text.toString(),
            VehicleFormEnum.SEAT to viewBinding?.fragmentAddCarLabelCarSeatsEditText?.text.toString(),
            VehicleFormEnum.DESCRIPTION to viewBinding?.fragmentAddCarLabelCarDescriptionEditText?.text.toString()
        )
    }

    private fun setup(){
        viewModel.getFormVehicleErrors().observe(viewLifecycleOwner, formErrorsObserver)
        viewModel.getRedirectVehicle().observe(viewLifecycleOwner, redirectObserver)

        viewBinding?.fragmentAddCarButtonAddCar?.setOnClickListener{
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()

            } else {
                sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                        ?.let { it1 -> viewModel.addNewVehicle(getInputs(), it1) }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentVehicleFormBinding.inflate(inflater, container, false)

        setup()
        return viewBinding?.root
    }

}