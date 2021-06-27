package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.driveus_mvvm.databinding.FragmentAddCarBinding
import com.example.driveus_mvvm.ui.enums.AddCarEnum
import com.example.driveus_mvvm.view_model.UserViewModel

class AddCarFragment : Fragment() {

    private var viewBinding: FragmentAddCarBinding? = null
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val formErrorsObserver = Observer<Map<AddCarEnum, Int>> {
        it.forEach { (k,v) ->
            when(k) {
                AddCarEnum.BRAND -> viewBinding?.fragmentAddCarLabelCarBrandEditText?.error = getString(v)
                AddCarEnum.MODEL -> viewBinding?.fragmentAddCarLabelCarModelEditText?.error = getString(v)
                AddCarEnum.COLOR -> viewBinding?.fragmentAddCarLabelCarColorEditText?.error = getString(v)
                AddCarEnum.SEAT -> viewBinding?.fragmentAddCarLabelCarSeats?.error = getString(v)
                AddCarEnum.DESCRIPTION -> viewBinding?.fragmentAddCarLabelCarDescriptionEditText?.error = getString(v)

            }
        }
    }

    private fun getInputs() : Map<AddCarEnum, String> {
        return mutableMapOf(
            AddCarEnum.BRAND to viewBinding?.fragmentAddCarLabelCarBrandEditText?.text.toString(),
            AddCarEnum.MODEL to viewBinding?.fragmentAddCarLabelCarModelEditText?.text.toString(),
            AddCarEnum.COLOR to viewBinding?.fragmentAddCarLabelCarColorEditText?.text.toString(),
            AddCarEnum.SEAT to viewBinding?.fragmentAddCarLabelCarSeatsEditText?.text.toString(),
            AddCarEnum.DESCRIPTION to viewBinding?.fragmentAddCarLabelCarDescriptionEditText?.text.toString()
        )
    }

    private fun setup(){
        viewModel.getFormVehicleErrors().observe(this, formErrorsObserver)

        viewBinding?.fragmentAddCarButtonAddCar?.setOnClickListener{
            viewModel.addNewVehicle(getInputs())
            findNavController().popBackStack()

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentAddCarBinding.inflate(inflater, container, false)

        setup()
        return viewBinding?.root
    }

}