package com.example.driveus_mvvm.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ca.antonious.materialdaypicker.MaterialDayPicker
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentRideFormBinding
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.ui.enums.RideFormEnum
import com.example.driveus_mvvm.ui.utils.DateTimeUtils
import com.example.driveus_mvvm.ui.utils.NetworkUtils
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
                RideFormEnum.TIME_REPEAT -> viewBinding?.fragmentRideFormLabelTimeRepeatEditText?.error = getString(v)
                RideFormEnum.DAY_LIMIT -> viewBinding?.fragmentRideFormLabelDateEndRepeatEditText?.error = getString(v)
                RideFormEnum.DAYS_OF_THE_WEEK -> {
                    viewBinding?.fragmentRideFormIconErrorDaysOfWeek?.error = getString(v)
                    viewBinding?.fragmentRideFormLabelErrorDaysOfWeek?.setText(getString(v))
                }
            }
        }
    }

    private fun getInputs(): Map<RideFormEnum, String> {
        return mutableMapOf(
            RideFormEnum.PRICE to viewBinding?.fragmentRideFormLabelPriceEditText?.text.toString(),
            RideFormEnum.CAPACITY to viewBinding?.fragmentRideFormLabelCapacityEditText?.text.toString(),
            RideFormEnum.DATETIME to viewBinding?.fragmentRideFormLabelDateTimeEditText?.text.toString(),
            RideFormEnum.MEETING_POINT to viewBinding?.fragmentRideFormLabelMeetingPointEditText?.text.toString(),
            RideFormEnum.DAY_LIMIT to viewBinding?.fragmentRideFormLabelDateEndRepeatEditText?.text.toString(),
            RideFormEnum.TIME_REPEAT to viewBinding?.fragmentRideFormLabelTimeRepeatEditText?.text.toString(),
            RideFormEnum.DAYS_OF_THE_WEEK to viewBinding?.fragmentRideFormInputDayWeek?.selectedDays.toString(),
            RideFormEnum.REPEAT to viewBinding?.fragmentRideFormCheckBoxRepeat?.isChecked.toString()
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

    private fun getDateLimitCalendar() {
        val cal: Calendar = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
    }

    private fun getTimeRepeat() {
        val cal: Calendar = Calendar.getInstance()
        minute = cal.get(Calendar.MINUTE)
        hour = cal.get(Calendar.HOUR)
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

    private fun selectDateLimit() {
        viewBinding?.fragmentRideFormLabelDateEndRepeatEditText?.setOnClickListener {
            viewBinding?.fragmentRideFormLabelDateEndRepeatEditText?.error = null
            getDateLimitCalendar()
            context?.let { it1 -> DatePickerDialog(it1, this, year, month, day).show() }
        }
    }

    private fun selectTimeRepeat() {
        viewBinding?.fragmentRideFormLabelTimeRepeatEditText?.setOnClickListener {
            viewBinding?.fragmentRideFormLabelTimeRepeatEditText?.error = null
            getTimeRepeat()
            TimePickerDialog(context, this, hour, minute, true).show()
        }
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
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()
            } else {
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
    }

    private fun setupRepeatable() {
        viewBinding?.fragmentRideFormCheckBoxRepeat?.setOnClickListener {
            if (viewBinding?.fragmentRideFormIconErrorDaysOfWeek?.error.isNullOrBlank().not()) {
                viewBinding?.fragmentRideFormLabelErrorDaysOfWeek?.setText("")
                viewBinding?.fragmentRideFormIconErrorDaysOfWeek?.error = null
            }
            if (viewBinding?.fragmentRideFormCheckBoxRepeat?.isChecked == true) {
                viewBinding?.fragmentRideFormContainerDateTimeNoRepeat?.visibility = View.GONE
                viewBinding?.fragmentRideFormContainerDateTimeRepeat?.visibility = View.VISIBLE
            } else {
                viewBinding?.fragmentRideFormContainerDateTimeNoRepeat?.visibility = View.VISIBLE
                viewBinding?.fragmentRideFormContainerDateTimeRepeat?.visibility = View.GONE
            }
        }
    }

    private fun goneErrorSelectDayOfWeek() {
        viewBinding?.fragmentRideFormInputDayWeek?.dayPressedListener = object: MaterialDayPicker.DayPressedListener {

            override fun onDayPressed(weekday: MaterialDayPicker.Weekday, isSelected: Boolean) {
                viewBinding?.fragmentRideFormLabelErrorDaysOfWeek?.setText("")
                viewBinding?.fragmentRideFormIconErrorDaysOfWeek?.error = null
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

        if (viewBinding?.fragmentRideFormCheckBoxRepeat?.isChecked == false) {
            getDateTimeCalendar()
            TimePickerDialog(context, this, hour, minute, true).show()
        } else {
            getDateLimitCalendar()
            val dateStr = "$savedDay/$savedMonth/$savedYear"
            viewBinding?.fragmentRideFormLabelDateEndRepeatEditText?.setText(dateStr)
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute
        val finalMinute = DateTimeUtils.fixMinuteString(savedMinute)
        if (viewBinding?.fragmentRideFormCheckBoxRepeat?.isChecked == false) {
            val datetimeStr = "$savedDay/$savedMonth/$savedYear $savedHour:$finalMinute"
            viewBinding?.fragmentRideFormLabelDateTimeEditText?.setText(datetimeStr)
        } else {
            val minuteStr = "$savedHour:$finalMinute"
            viewBinding?.fragmentRideFormLabelTimeRepeatEditText?.setText(minuteStr)
        }
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
        setupRepeatable()
        selectDateTime()
        selectDateLimit()
        selectTimeRepeat()
        submitNewRide()
        goneErrorSelectDayOfWeek()
        viewBinding?.fragmentRideFormLabelVehicleSpinner?.onItemSelectedListener = this
    }
}