package com.example.driveus_mvvm.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentRideFormBinding
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.ui.enums.RideFormEnum
import com.example.driveus_mvvm.view_model.RideViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import java.util.*

class RideFormFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {
    private var viewBinding: FragmentRideFormBinding? = null
    private val channelId by lazy { arguments?.getString("channelId") }
    private val viewModel: RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private var vehicleDocRef: String? = null
    private var idAndVehicle: MutableMap<String, Vehicle>? = null

    private var minute = 0
    private var hour = 0
    private var day = 0
    private var month = 0
    private var year = 0

    private var savedMinute = 0
    private var savedHour = 0
    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0

    private val formErrorsObserver = Observer<Map<RideFormEnum, Int>> {
        it.forEach { (k, v)->
            when(k) {
                RideFormEnum.CAPACITY -> viewBinding?.fragmentRideFormLabelCapacityEditText?.error = getString(v)
                RideFormEnum.PRICE -> viewBinding?.fragmentRideFormLabelPriceEditText?.error = getString(v)
                RideFormEnum.DATETIME -> viewBinding?.fragmentRideFormLabelDateTimeEditText?.error = getString(v)
                RideFormEnum.MEETING_POINT -> viewBinding?.fragmentRideFormLabelMeetingPointEditText?.error = getString(v)
            }
        }
    }

    private fun getInputs(): Map<RideFormEnum, String> {
        return mutableMapOf(
            RideFormEnum.PRICE to viewBinding?.fragmentRideFormLabelPriceEditText?.text.toString(),
            RideFormEnum.CAPACITY to viewBinding?.fragmentRideFormLabelCapacityEditText?.text.toString(),
            RideFormEnum.DATETIME to viewBinding?.fragmentRideFormLabelDateTimeEditText?.text.toString(),
            RideFormEnum.MEETING_POINT to viewBinding?.fragmentRideFormLabelMeetingPointEditText?.text.toString()
        )
    }

    private val redirectObserver = Observer<Boolean> {
        if (it) {
            findNavController().popBackStack()
        }
    }

    private fun getDateTimeCalendar() {
        val cal: Calendar = Calendar.getInstance()
        minute = cal.get(Calendar.MINUTE)
        hour = cal.get(Calendar.HOUR)
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private val vehiclesObserver = Observer<Map<String, Vehicle>> { document ->
        idAndVehicle = document as MutableMap<String, Vehicle>?
        val vehicleListString: MutableList<String> = mutableListOf()
        for(v in document.values) {
            vehicleListString.add(v.brand + " " + v.model)
        }
        val adpt = context?.let { ArrayAdapter(it, android.R.layout.simple_spinner_dropdown_item, vehicleListString) }
        val spinner = viewBinding?.fragmentRideFormLabelVehicleSpinner
        spinner?.adapter = adpt
    }

    private fun selectDateTime() {
        viewBinding?.fragmentRideFormLabelDateTimeEditText?.setOnClickListener {
            viewBinding?.fragmentRideFormLabelDateTimeEditText?.error = null
            getDateTimeCalendar()
            context?.let { it1 -> DatePickerDialog(it1, this, year, month,day).show() }
        }
    }

    private fun submitNewRide() {
        viewBinding?.fragmentRideFormButtonCreate?.setOnClickListener {
            sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                ?.let { userId ->
                    channelId?.let { channelId ->
                        context?.let { context ->
                            vehicleDocRef?.let { it1 -> viewModel.addNewRide(getInputs(), userId, it1, channelId, context) }
                        }
                    }
                }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val idKeys: List<String>? = idAndVehicle?.keys?.toList()
        vehicleDocRef = idKeys?.get(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        val idKeys: List<String>? = idAndVehicle?.keys?.toList()
        vehicleDocRef = idKeys?.get(0)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month+1
        savedYear = year

        getDateTimeCalendar()

        TimePickerDialog(context, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        viewBinding?.fragmentRideFormLabelDateTimeEditText?.setText("$savedDay/$savedMonth/$savedYear $savedHour:$savedMinute")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentRideFormBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getRideFormErrors().observe(viewLifecycleOwner, formErrorsObserver)
        viewModel.getRedirectRide().observe(viewLifecycleOwner, redirectObserver)
        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { userViewModel.getVehiclesByUserId(it).
                observe(viewLifecycleOwner, vehiclesObserver)
            }

        selectDateTime()
        submitNewRide()
        viewBinding?.fragmentRideFormLabelVehicleSpinner?.onItemSelectedListener = this
    }
}