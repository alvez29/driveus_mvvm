package com.example.driveus_mvvm.ui

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentChannelDetailBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.RidesListAdapter
import com.example.driveus_mvvm.ui.utils.DateTimeUtils
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import zion830.com.range_picker_dialog.TimeRangePickerDialog
import java.util.*

class ChannelDetailFragment : Fragment(), DatePickerDialog.OnDateSetListener {

    private var viewBinding: FragmentChannelDetailBinding? = null
    private val channelId by lazy { arguments?.getString("channelId") }
    private val channelViewModel : ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()

    private val channelObserver = Observer<Channel> {
        viewBinding?.channelDetailLabelChannelOriginZone?.text = it.originZone
        viewBinding?.channelDetailLabelChannelDestinationZone?.text = it.destinationZone
    }

    private val isDriverObserver = Observer<Boolean> {
        if (it){
            viewBinding?.channelDetailButtonFloatingButton?.visibility = View.VISIBLE
        } else {
            viewBinding?.channelDetailButtonFloatingButton?.visibility = View.GONE
        }
    }

    private fun checkListIsEmpty(map: List<Pair<String, Ride>>?) {
        if (map?.isEmpty() == true) {
            viewBinding?.channelDetailListRidesList?.visibility = View.GONE
            viewBinding?.channelDetailFragmentContainerNoRidesLinearLayout?.visibility = View.VISIBLE
        } else {
            viewBinding?.channelDetailListRidesList?.visibility = View.VISIBLE
            viewBinding?.channelDetailFragmentContainerNoRidesLinearLayout?.visibility = View.GONE
        }
    }

    private fun configureRideFilter(map: List<Pair<String, Ride>>, adapter: RidesListAdapter) {
        viewBinding?.channelDetailInputFilterText?.addTextChangedListener { editableText ->
            if (editableText.toString().isBlank()) {
                viewBinding?.channelDetailImageClearFilter?.visibility = View.GONE
                checkListIsEmpty(map)
                adapter.submitList(map)

            } else {
                viewBinding?.channelDetailImageClearFilter?.visibility = View.VISIBLE
                val startAndEndTimestamp: Pair<Timestamp, Timestamp> = DateTimeUtils.getStartAndEndDateFromFilterString(editableText.toString())
                val filteredMap = map
                    .filter {
                        it.second.date?.let { timestamp ->
                            timestamp >= startAndEndTimestamp.first && timestamp <= startAndEndTimestamp.second
                        } == true
                    }.sortedBy {
                        it.second.date
                    }

                checkListIsEmpty(filteredMap)
                adapter.submitList(filteredMap)
            }
        }

        viewBinding?.channelDetailImageClearFilter?.setOnClickListener {
            viewBinding?.channelDetailInputFilterText?.text = ""
        }
    }

    private fun ridesObserver(adapter: RidesListAdapter) = Observer<Map<String, Ride>> { map ->
        val mapList = map.toList().sortedBy { it.second.date }

        configureRideFilter(mapList, adapter)
        checkListIsEmpty(mapList)
        adapter.submitList(mapList)

    }

    private val ridesListAdapterListener = object : RidesListAdapter.RideListAdapterListener {

        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            context?.let { ImageUtils.loadProfilePicture(userId, imageView, it, firebaseStorage ) }
        }

        override fun navigateToRideDetail(rideId: String) {
            val action = channelId?.let {
                ChannelDetailFragmentDirections
                    .actionChannelDetailFragmentToRideDetailFragment()
                    .setRideId(rideId)
                    .setChannelId(it)
            }

            if (action != null) {
                findNavController().navigate(action)
            }
        }

    }

    private fun setupRecyclerAdapter(): RidesListAdapter {
        val adapter = RidesListAdapter(ridesListAdapterListener)

        viewBinding?.channelDetailListRidesList?.adapter = adapter
        viewBinding?.channelDetailListRidesList?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupDialog() {
        viewBinding?.channelDetailInputFilterText?.setOnClickListener {
            val cal: Calendar = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            context?.let { it1 ->
                DatePickerDialog(it1, this, year, month,day).show()
            }
        }
    }

    private fun setupToRideForm() {
        viewBinding?.channelDetailButtonFloatingButton?.setOnClickListener {
            val action = channelId?.let { it1 ->
                ChannelDetailFragmentDirections
                    .actionChannelDetailFragmentToRideFormFragment()
                    .setChannelId(it1)
            }
            if (action != null) {
                findNavController().navigate(action)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentChannelDetailBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val dateStr = "$dayOfMonth/${month+1}/$year"

        TimeRangePickerDialog.Builder()
                .setOnTimeRangeSelectedListener { timeRange ->
                    val res = "$dateStr de ${timeRange.startHour}:${DateTimeUtils.fixMinuteString(timeRange.startMinute)} " +
                            "a ${timeRange.endHour}:${DateTimeUtils.fixMinuteString(timeRange.endMinute)} "
                    viewBinding?.channelDetailInputFilterText?.text = res
                }
                .build()
                .show(parentFragmentManager)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = setupRecyclerAdapter()

        channelId?.let {
            channelViewModel.getRidesFromChannel(it).observe(viewLifecycleOwner, ridesObserver(adapter))
            channelViewModel.getChannelById(it).observe(viewLifecycleOwner, channelObserver)
        }
        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")?.let { userId ->
            userViewModel.isDriver(userId).observe(viewLifecycleOwner, isDriverObserver)
        }
        setupDialog()
        setupToRideForm()
    }

}