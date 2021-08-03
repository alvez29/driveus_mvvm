package com.example.driveus_mvvm.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentMyRidesRecordBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.MyRidesRecordListAdapter
import com.example.driveus_mvvm.ui.utils.DateTimeUtils
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.view_model.RideViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class MyRidesRecordFragment : Fragment() {

    private var viewBinding: FragmentMyRidesRecordBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()

    private val myRidesRecordListAdapterListener = object : MyRidesRecordListAdapter.MyRidesRecordListAdapterListener {
        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            context?.let { ImageUtils.loadProfilePicture(userId, imageView, it, firebaseStorage ) }
        }

        override fun navigateToRideDetail(rideId: String, channelId: String?) {
            val action = channelId?.let {
                MyRidesFragmentDirections
                        .actionMyRidesFragmentToRideDetailFragment()
                        .setRideId(rideId)
                        .setChannelId(it)
            }

            if (action != null) {
                findNavController().navigate(action)
            }
        }

    }

    private fun onEndDateSet(startDateStr: String) = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        val endDateStr = "$dayOfMonth/${month+1}/$year"
        val dateStr = "De $startDateStr a $endDateStr"
        val endDate = DateTimeUtils.dateStringToDate(endDateStr)
        val startDate = DateTimeUtils.dateStringToDate(startDateStr)

        if (endDate.before(startDate)) {
            Toast.makeText(context, getString(R.string.my_rides_record_filter_dialog_toast_validation_error),Toast.LENGTH_SHORT ).show()
        } else {
            viewBinding?.myRidesRecordInputFilterText?.text = dateStr
        }

    }

    private val onStartDateSet = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        val dateStr = "$dayOfMonth/${month+1}/$year"
        val datePicker = context?.let { it1 -> DatePickerDialog(it1, onEndDateSet(dateStr), year, month, dayOfMonth) }
        datePicker?.setTitle(getString(R.string.my_rides_record_filter_dialog_end_title))
        datePicker?.show()
    }

    private fun configureRideFilter(adapter: MyRidesRecordListAdapter, list: List<DocumentSnapshot>?) {
        viewBinding?.myRidesRecordContainerFilter?.setOnClickListener {
            val cal: Calendar = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            val datePicker = context?.let { it1 -> DatePickerDialog(it1, onStartDateSet, year, month,day)}
            datePicker?.setTitle(getString(R.string.my_rides_record_filter_dialog_start_title))
            datePicker?.show()
        }

        viewBinding?.myRidesRecordImageClearFilter?.setOnClickListener {
            viewBinding?.myRidesRecordInputFilterText?.text = ""
            it.visibility = View.INVISIBLE
        }

        viewBinding?.myRidesRecordInputFilterText?.addTextChangedListener { editableText ->
            if (editableText.toString().isBlank()) {
                viewBinding?.myRidesRecordImageClearFilter?.visibility = View.VISIBLE
                adapter.submitList(list?.sortedBy { it.getTimestamp("date")})

            } else {
                viewBinding?.myRidesRecordImageClearFilter?.visibility = View.VISIBLE
                val splitStringDate = editableText?.removeRange(0, 2)?.split("a")
                val originDate = DateTimeUtils.dateTimeStringToTimestamp("${splitStringDate?.get(0)?.trim()} 00:00").toDate()
                val endDate = DateTimeUtils.dateTimeStringToTimestamp("${splitStringDate?.get(1)?.trim()} 23:59").toDate()

                val listFiltered = list?.filter {
                    val date = it.getTimestamp("date")?.toDate()
                    date?.after(originDate) == true && date.before(endDate)
                }

                adapter.submitList(listFiltered)
            }
        }

    }

    private fun myRidesRecordAsPassengerObserver(adapter: MyRidesRecordListAdapter) = Observer<List<DocumentSnapshot>> { docSnap ->
        configureRideFilter(adapter, docSnap)

        if(docSnap.isEmpty()) {
            viewBinding?.myRidesRecordListRecyclerAsPassenger?.visibility = View.GONE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutPassenger?.visibility = View.VISIBLE
        } else {
            viewBinding?.myRidesRecordListRecyclerAsPassenger?.visibility = View.VISIBLE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutPassenger?.visibility = View.GONE
        }
        adapter.submitList(docSnap.toList().sortedBy { it.getTimestamp("date") })
    }

    private fun myRidesRecordAsDriverObserver(adapter: MyRidesRecordListAdapter) = Observer<List<DocumentSnapshot>> { docSnap ->
        configureRideFilter(adapter, docSnap)

        if(docSnap.isEmpty()) {
            viewBinding?.myRidesRecordListRecyclerAsDriver?.visibility = View.GONE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutDriver?.visibility = View.VISIBLE
        } else {
            viewBinding?.myRidesRecordListRecyclerAsDriver?.visibility = View.VISIBLE
            viewBinding?.myRidesRecordFragmentContainerNoRidesLinearLayoutDriver?.visibility = View.GONE
        }
        adapter.submitList(docSnap.toList().sortedBy { it.getTimestamp("date") })
    }

    private fun setUpRecyclerAsPassengerAdapter(): MyRidesRecordListAdapter {
        val adapter = MyRidesRecordListAdapter(myRidesRecordListAdapterListener)

        viewBinding?.myRidesRecordListRecyclerAsPassenger?.adapter = adapter
        viewBinding?.myRidesRecordListRecyclerAsPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setUpRecyclerAsDriverAdapter(): MyRidesRecordListAdapter {
        val adapter = MyRidesRecordListAdapter(myRidesRecordListAdapterListener)

        viewBinding?.myRidesRecordListRecyclerAsDriver?.adapter = adapter
        viewBinding?.myRidesRecordListRecyclerAsDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }


    private fun setupFloatingButton() {
        viewBinding?.myRidesRecordButtonListButtonRole?.setOnClickListener {
            viewBinding?.myRidesRecordButtonFabChangeRole?.playAnimation()

            val drawableWheel = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_steering_wheel_24) }
            val drawableSeat = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_round_event_seat_24) }

            if (viewBinding?.myRidesRecordContainerDriver?.isShown == true) {
                viewBinding?.myRidesRecordContainerDriver?.visibility = View.GONE
                viewBinding?.myRidesRecordContainerPassenger?.visibility = View.VISIBLE
                viewBinding?.myRidesRecordButtonListButtonRole
                        ?.setCompoundDrawablesWithIntrinsicBounds(drawableSeat, null, null, null)
                viewBinding?.myRidesRecordButtonListButtonRole?.text = getString(R.string.my_rides_record_list__label__role_button_passenger)
            } else {
                viewBinding?.myRidesRecordContainerDriver?.visibility = View.VISIBLE
                viewBinding?.myRidesRecordContainerPassenger?.visibility = View.GONE
                viewBinding?.myRidesRecordButtonListButtonRole
                        ?.setCompoundDrawablesWithIntrinsicBounds(drawableWheel, null, null, null)
                viewBinding?.myRidesRecordButtonListButtonRole?.text = getString(R.string.my_rides_record_list__label__role_button_driver)
            }
        }

        viewBinding?.myRidesRecordButtonFabChangeRole?.setOnClickListener {
            viewBinding?.myRidesRecordButtonListButtonRole?.callOnClick()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentMyRidesRecordBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterAsPassenger = setUpRecyclerAsPassengerAdapter()
        val adapterAsDriver = setUpRecyclerAsDriverAdapter()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { rideViewModel.getRidesAsPassenger(it).observe(viewLifecycleOwner, myRidesRecordAsPassengerObserver(adapterAsPassenger)) }

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { rideViewModel.getRidesAsDriver(it).observe(viewLifecycleOwner, myRidesRecordAsDriverObserver(adapterAsDriver)) }

        setupFloatingButton()
    }

}