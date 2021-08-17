package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentPayoutsBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.PayoutListAdapter
import com.example.driveus_mvvm.ui.utils.HelpMenu
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.ui.utils.NetworkUtils
import com.example.driveus_mvvm.view_model.PayoutViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage

class PayoutsFragment : Fragment() {

    private var viewBinding: FragmentPayoutsBinding? = null
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()
    private val payoutViewModel: PayoutViewModel by lazy { ViewModelProvider(this)[PayoutViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val dialogCallback = object: PayoutUsernameFilterDialogFragment.PayoutUserNameFilterDialogFragmentCallback {

        override fun selectedUserName(username: String) {
            viewBinding?.fragmentPayoutLabelFilter?.text = username
            viewBinding?.fragmentPayoutImageClearFilter?.visibility = View.VISIBLE
        }

    }

    private val adapterListener = object : PayoutListAdapter.PayoutListAdapterListener {
        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
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
                    payoutViewModel.checkPayoutAsPaid(channelId, rideId, payoutDocSnap.reference, payoutDocSnap.get("passenger") as DocumentReference)
                }
            }

        }

        override fun navigateToRide(payoutDocSnap: DocumentSnapshot) {
            val channelId = payoutDocSnap.reference.parent.parent?.parent?.parent?.id
            val rideId = payoutDocSnap.reference.parent.parent?.id

            if (channelId != null && rideId != null) {
                val action = PayoutsFragmentDirections.actionPayoutsFragmentToRideDetailFragment()
                        .setChannelId(channelId)
                        .setRideId(rideId)

                findNavController().navigate(action)
            }
        }

        override fun amITheDriver(passengerId: String): Boolean {
            var res = false
            val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            if (userId != passengerId) {
                res = true
            }
            return res
        }
    }

    private val hasDebtsObserver = Observer<Boolean> {
        if (it) {
            viewBinding?.fragmentPayoutButtonDebtsButton?.visibility = View.VISIBLE
        } else {
            viewBinding?.fragmentPayoutButtonDebtsButton?.visibility = View.GONE
        }
    }

    private fun configurePayoutsFilter(docSnapList: List<Pair<String, DocumentSnapshot>>?, adapter: PayoutListAdapter) {
        val dialog = PayoutUsernameFilterDialogFragment(dialogCallback, getUsernamesList(docSnapList))

        viewBinding?.fragmentPayoutContainerFilter?.setOnClickListener {
            dialog.show(childFragmentManager, "usernameDialog")
        }

        viewBinding?.fragmentPayoutImageClearFilter?.setOnClickListener {
            viewBinding?.fragmentPayoutLabelFilter?.text = ""
            viewBinding?.fragmentPayoutImageClearFilter?.visibility = View.GONE
        }

        viewBinding?.fragmentPayoutLabelFilter?.addTextChangedListener { filterText ->
            if (filterText.toString().isBlank()){
                adapter.submitList(docSnapList)
            } else {
                val filteredList = docSnapList?.filter {
                    it.first == filterText.toString()
                }

                adapter.submitList(filteredList)
            }
        }
    }


    private fun payoutsAsPassengerObserver(adapter: PayoutListAdapter) = Observer<List<Pair<String, DocumentSnapshot>>> { docSnapList ->

        if (docSnapList.isEmpty()) {
            viewBinding?.fragmentPayoutListPayoutsListPassenger?.visibility = View.GONE
            viewBinding?.fragmentPayoutContainerNoPayoutsPassenger?.visibility = View.VISIBLE
        } else {
            viewBinding?.fragmentPayoutListPayoutsListPassenger?.visibility = View.VISIBLE
            viewBinding?.fragmentPayoutContainerNoPayoutsPassenger?.visibility = View.GONE
        }

        adapter.submitList(docSnapList)
    }


    private fun payoutsAsDriverObserver(adapter: PayoutListAdapter) = Observer<List<Pair<String, DocumentSnapshot>>> { docSnapList ->
        adapter.submitList(docSnapList)

        if (docSnapList.isEmpty()) {
            viewBinding?.fragmentPayoutListPayoutsListDriver?.visibility = View.GONE
            viewBinding?.fragmentPayoutContainerNoPayoutsDriver?.visibility = View.VISIBLE
        } else {
            viewBinding?.fragmentPayoutListPayoutsListDriver?.visibility = View.VISIBLE
            viewBinding?.fragmentPayoutContainerNoPayoutsDriver?.visibility = View.GONE
        }

            configurePayoutsFilter(docSnapList, adapter)
    }

    private fun getUsernamesList(docSnapList: List<Pair<String, DocumentSnapshot>>?): Set<String>? {
        return docSnapList?.map { it.first }?.toSet()
    }

    private fun setupRecyclerAdapterPassenger() : PayoutListAdapter {
        val adapter = PayoutListAdapter(adapterListener)

        viewBinding?.fragmentPayoutListPayoutsListPassenger?.adapter = adapter
        viewBinding?.fragmentPayoutListPayoutsListPassenger?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupRecyclerAdapterDriver() : PayoutListAdapter {
        val adapter = PayoutListAdapter(adapterListener)

        viewBinding?.fragmentPayoutListPayoutsListDriver?.adapter = adapter
        viewBinding?.fragmentPayoutListPayoutsListDriver?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    private fun setupButtonRole() {
        val drawableWheel = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_steering_wheel_24) }
        val drawableSeat = context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.ic_round_event_seat_24) }

        viewBinding?.fragmentPayoutButtonRoleButton?.setOnClickListener {
            viewBinding?.fragmentPayoutButtonFabChangeRole?.playAnimation()

            if (viewBinding?.fragmentPayoutContainerPayoutsPassenger?.visibility == View.VISIBLE) {
                viewBinding?.fragmentPayoutContainerFilter?.visibility = View.VISIBLE

                viewBinding?.fragmentPayoutButtonRoleButton?.text = getString(R.string.payouts_fragment_button__label__driver)
                viewBinding?.fragmentPayoutButtonRoleButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableWheel, null, null, null)
                viewBinding?.fragmentPayoutContainerPayoutsPassenger?.visibility = View.GONE
                viewBinding?.fragmentPayoutContainerPayoutsDriver?.visibility = View.VISIBLE
            } else {
                viewBinding?.fragmentPayoutContainerFilter?.visibility = View.GONE

                viewBinding?.fragmentPayoutButtonRoleButton?.text = getString(R.string.payouts_fragment_button__label__passenger)
                viewBinding?.fragmentPayoutButtonRoleButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableSeat, null, null, null)

                viewBinding?.fragmentPayoutContainerPayoutsPassenger?.visibility = View.VISIBLE
                viewBinding?.fragmentPayoutContainerPayoutsDriver?.visibility = View.GONE
            }
        }
    }

    private fun setupButtonDebts() {
        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { payoutViewModel.userHasDebts(it).observe(viewLifecycleOwner, hasDebtsObserver) }
        viewBinding?.fragmentPayoutButtonDebtsButton?.setOnClickListener {
            val action = PayoutsFragmentDirections.actionPayoutsFragmentToPayoutDebtsFragment()
            findNavController().navigate(action)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_bar_payouts, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_top_bar_payouts__item__help -> {
                val text1 = R.string.dialog_help_payouts1
                val text2 = R.string.dialog_help_payouts2
                val text3 = R.string.dialog_help_payouts3
                val textList: List<Int> = listOf(text1, text2, text3)
                context?.let { HelpMenu.displayHelpMenu(it, textList) }
            }
        }
        return true
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentPayoutsBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapterPassenger = setupRecyclerAdapterPassenger()
        val adapterDriver = setupRecyclerAdapterDriver()

        setupButtonRole()
        setupButtonDebts()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key),"")
            ?.let {
                payoutViewModel.getPendingPayoutsAsPassengerFromUser(it).observe(viewLifecycleOwner, payoutsAsPassengerObserver(adapterPassenger))
                payoutViewModel.getPendingPayoutsAsDriverFromUser(it).observe(viewLifecycleOwner, payoutsAsDriverObserver(adapterDriver))
            }
    }

}