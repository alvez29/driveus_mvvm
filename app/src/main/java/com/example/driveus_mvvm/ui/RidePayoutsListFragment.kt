package com.example.driveus_mvvm.ui

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentRidePayoutsDetailListBinding
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.PayoutsRideDetailsListAdapter
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.ui.utils.NetworkUtils
import com.example.driveus_mvvm.view_model.PayoutViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class RidePayoutsListFragment: Fragment() {

    private var viewBinding: FragmentRidePayoutsDetailListBinding? = null
    private val payoutViewModel : PayoutViewModel by lazy { ViewModelProvider(this)[PayoutViewModel::class.java] }
    private val rideId by lazy { arguments?.getString("rideId") }
    private val channelId by lazy { arguments?.getString("channelId") }
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val adapterListener = object : PayoutsRideDetailsListAdapter.RideListAdapterListener {

        override fun loadProfilePicture(userId: String?, imageView: ImageView) {
            context?.let { ImageUtils.loadProfilePicture(userId, imageView, it, firebaseStorage ) }
        }

        override fun pressItem(payout: QueryDocumentSnapshot, checkBox: CheckBox) {
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()
                checkBox.isChecked = false

            } else {
                if (payout.getBoolean("isPaid") == true) {
                    checkBox.isChecked = true
                    dialogCheckAsUnpaid(payout.reference, checkBox)

                } else {
                    channelId?.let { chnId ->
                        rideId?.let { rdId ->
                            payoutViewModel.checkPayoutAsPaid(chnId, rdId, payout.reference, payout.get("passenger") as DocumentReference)
                        }
                    }
                }
            }

        }

        override fun showItemPaid(payoutViewHolder: PayoutsRideDetailsListAdapter.PayoutViewHolder, date: Date?) {
            val pattern = "HH:mm dd-MM-yyyy"
            val simpleDateFormat = SimpleDateFormat(pattern)

            val priceColor = context?.let { ContextCompat.getColor(it, R.color.paid) }
            val backgroundColor = context?.let { ContextCompat.getColor(it, R.color.white) }

            payoutViewHolder.checkbox.isChecked = true

            date?.let {
                val dateValue = simpleDateFormat.format(date).split(" ")
                val dateStr = "Pagado a las ${dateValue[0]} el día ${dateValue[1]}"
                payoutViewHolder.paidDate.visibility = View.VISIBLE
                payoutViewHolder.paidDate.text = dateStr
            }

            if (priceColor != null) {
                payoutViewHolder.price.setTextColor(priceColor)
            }

            if (backgroundColor != null) {
                payoutViewHolder.itemView.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            }
        }

        override fun showItemUnpaid(payoutViewHolder: PayoutsRideDetailsListAdapter.PayoutViewHolder) {
            val priceColor = context?.let { ContextCompat.getColor(it, R.color.unpaid) }
            val backgroundColor = context?.let { ContextCompat.getColor(it, R.color.disbled_payout_grey) }

            payoutViewHolder.checkbox.isChecked = false

            payoutViewHolder.paidDate.visibility = View.GONE
            payoutViewHolder.paidDate.text = ""

            if (priceColor != null) {
                payoutViewHolder.price.setTextColor(priceColor)
            }

            if (backgroundColor != null) {
                payoutViewHolder.itemView.backgroundTintList = ColorStateList.valueOf(backgroundColor)
            }

        }
    }

    private fun dialogCheckAsUnpaid(payouDocRef: DocumentReference, checkBox: CheckBox) {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_check_payout_as_unpaid, null)
        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)
            .setTitle(getString(R.string.dialog_check_payout_as_unpaid_title))

        val mAlertDialog = mBuilder.show()

        mDialogView.findViewById<View>(R.id.dialog_check_payout_as_unpaid__button__cancel).setOnClickListener{
            mAlertDialog.dismiss()
        }

        mDialogView.findViewById<View>(R.id.dialog_check_payout_as_unpaid__accept).setOnClickListener{
            channelId?.let { chnId ->
                rideId?.let { rideId ->
                    payoutViewModel.checkPayoutAsUnpaid(chnId, rideId, payouDocRef, sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), ""))
                }
            }
            checkBox.isChecked = false
            mAlertDialog.dismiss()
        }

    }

    private fun payoutsObserver(adapter: PayoutsRideDetailsListAdapter) = Observer<Map<String, QueryDocumentSnapshot>> { docSnapList ->
        adapter.submitList(docSnapList.toList())

        val totalPrice = docSnapList.filter { it.component2().getBoolean("isPaid") == true }
                                    .map { it.component2() }.sumOf { it.getDouble("price") as Double }

        val totalPriceStr = "$totalPrice €"
        viewBinding?.fragmentRidePayoutsDetailListLabelTotalPrice?.text = totalPriceStr

    }

    private fun setupRecyclerAdapter() : PayoutsRideDetailsListAdapter {
        val adapter = PayoutsRideDetailsListAdapter(adapterListener)

        viewBinding?.ridePayoutsDetailListPayoutsList?.adapter = adapter
        viewBinding?.ridePayoutsDetailListPayoutsList?.layoutManager = LinearLayoutManager(context)

        return adapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentRidePayoutsDetailListBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = setupRecyclerAdapter()

        rideId?.let { rdId ->
            channelId?.let { chnId ->
                payoutViewModel.getPayoutsListFromRide(chnId, rdId).observe(viewLifecycleOwner, payoutsObserver(adapter))
            }
        }

    }
}