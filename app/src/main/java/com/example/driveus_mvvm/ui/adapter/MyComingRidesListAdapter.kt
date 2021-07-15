package com.example.driveus_mvvm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.ui.utils.LocationUtils
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat


private val diffCallback = object : DiffUtil.ItemCallback<DocumentSnapshot>() {

    override fun areItemsTheSame(
        oldItem: DocumentSnapshot,
        newItem: DocumentSnapshot
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: DocumentSnapshot,
        newItem: DocumentSnapshot
    ): Boolean {
        return oldItem == newItem
    }

}

class MyComingRidesListAdapter(
        private val listener: MyComingRideListAdapterListener
) : ListAdapter<DocumentSnapshot, MyComingRidesListAdapter.RideViewHolder>(diffCallback) {

    interface MyComingRideListAdapterListener {
        fun loadProfilePicture(userId: String?, imageView: ImageView)
        fun navigateToRideDetail(rideId: String, channelId: String?)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val rideView = LayoutInflater.from(parent.context).inflate(R.layout.row_ride, parent, false)

        return RideViewHolder(rideView)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val currentRide = getItem(position).toObject(Ride::class.java)

        val pattern = "HH:mm dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)

        val capacityPercentage = currentRide?.capacity?.toDouble()
                ?.let { currentRide.passengers.size.toDouble() / it }
        var capacityDrawable = 0

        currentRide?.date?.let {
            holder.date.text = simpleDateFormat.format(it.toDate())
        }

        holder.meetingPoint.text = LocationUtils.getAddress(
            currentRide?.meetingPoint,
            holder.itemView.context
        )
        holder.price.text = currentRide?.price.toString()

        listener.loadProfilePicture(currentRide?.driver?.id, holder.profilePicture)

        capacityPercentage?.let {
            capacityDrawable = if (capacityPercentage == 1.0) {
                R.drawable.ic_baseline_red_circle_24

            } else if (capacityPercentage < 1.0 && capacityPercentage >= 0.75) {
                R.drawable.ic_baseline_orange_circle_24

            } else {
                R.drawable.ic_baseline_green_circle_24

            }
        }

        Glide.with(holder.itemView.context)
                .load(capacityDrawable)
                .circleCrop()
                .into(holder.capacityIndicator)


    }

    inner class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView by lazy { itemView.findViewById(R.id.ride_row__label__hour_value) }
        val price: TextView by lazy { itemView.findViewById(R.id.ride_row__label__euro_value) }
        val meetingPoint: TextView by lazy { itemView.findViewById(R.id.ride_row__label__meeting_point) }
        val profilePicture: ImageView by lazy { itemView.findViewById(R.id.ride_row__image__profile) }
        val capacityIndicator: ImageView by lazy { itemView.findViewById(R.id.ride_row__image__capacity_indicator) }

        init {
            itemView.setOnClickListener {
                getItem(adapterPosition).let { ride -> listener.navigateToRideDetail(ride.id, ride.reference.parent.parent?.id) }
            }
        }

    }

}