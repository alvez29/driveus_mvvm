package com.example.driveus_mvvm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.Payout
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.util.*

private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, QueryDocumentSnapshot>>() {

    override fun areItemsTheSame(oldItem: Pair<String, QueryDocumentSnapshot>, newItem: Pair<String, QueryDocumentSnapshot>): Boolean {
        return oldItem.second.id == newItem.second.id
    }

    override fun areContentsTheSame(oldItem: Pair<String, QueryDocumentSnapshot>, newItem: Pair<String, QueryDocumentSnapshot>): Boolean {
        return oldItem == newItem
    }

}

class PayoutsRideDetailsListAdapter(
        private val listener: RideListAdapterListener
) : ListAdapter<Pair<String, QueryDocumentSnapshot>, PayoutsRideDetailsListAdapter.PayoutViewHolder>(diffCallback) {

    interface RideListAdapterListener {
        fun loadProfilePicture(userId: String?, imageView: ImageView)
        fun pressItem(payout: QueryDocumentSnapshot, checkBox: CheckBox)
        fun showItemPaid(payoutViewHolder: PayoutViewHolder, date: Date?)
        fun showItemUnpaid(payoutViewHolder: PayoutViewHolder)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayoutsRideDetailsListAdapter.PayoutViewHolder {
        val rideView = LayoutInflater.from(parent.context).inflate(R.layout.row_payout_ride_details, parent, false)

        return PayoutViewHolder(rideView)
    }

    override fun onBindViewHolder(holder: PayoutsRideDetailsListAdapter.PayoutViewHolder, position: Int) {
        val payout = getItem(position).second.toObject(Payout::class.java)
        val priceStr = "${payout.price} €"

        listener.loadProfilePicture(payout.passenger?.id, holder.profilePicture)
        holder.price.text = priceStr
        holder.username.text = getItem(position).first
        if (payout.isPaid) {
            listener.showItemPaid(holder, payout.paidDate?.toDate())
        } else {
            listener.showItemUnpaid(holder)
        }

    }

    inner class PayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView by lazy { itemView.findViewById(R.id.payout_row_ride_details__image__profile_picture) }
        val username: TextView by lazy { itemView.findViewById(R.id.payout_row_ride_details__label__username) }
        val price: TextView by lazy { itemView.findViewById(R.id.payout_row_ride_details__label__price) }
        val paidDate: TextView by lazy { itemView.findViewById(R.id.payout_row_ride_details__label__paid_date) }
        val checkbox: CheckBox by lazy { itemView.findViewById(R.id.payout_row_ride_details__button__checkbox) }

        init {
            checkbox.setOnClickListener {
                listener.pressItem(getItem(adapterPosition).second, checkbox)
            }
        }
    }

}