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
import java.text.SimpleDateFormat


private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, Ride>>() {

    override fun areItemsTheSame(
        oldItem: Pair<String, Ride>,
        newItem: Pair<String, Ride>
    ): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(
        oldItem: Pair<String, Ride>,
        newItem: Pair<String, Ride>
    ): Boolean {
        return oldItem == newItem
    }

}

class MyComingRidesListAdapter(
    private val listener: RideListAdapterListener
) : ListAdapter<Pair<String, Ride>, MyComingRidesListAdapter.RideViewHolder>(diffCallback) {

    interface RideListAdapterListener {
        fun loadProfilePicture(userId: String?, imageView: ImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RideViewHolder {
        val rideView = LayoutInflater.from(parent.context).inflate(R.layout.row_ride, parent, false)

        return RideViewHolder(rideView)
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) {
        val currentRidePair = getItem(position)

        val pattern = "HH:mm dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)

        val capacityPercentage = currentRidePair.second.capacity?.toDouble()
                ?.let { currentRidePair.second.passengers.size.toDouble() / it }
        var capacityDrawable = 0

        currentRidePair.second.date?.let {
            holder.date.text = simpleDateFormat.format(it.toDate())
        }

        holder.meetingPoint.text = LocationUtils.getAddress(
            currentRidePair.second.meetingPoint,
            holder.itemView.context
        )
        holder.price.text = currentRidePair.second.price.toString()

        holder.profilePicture.setImageResource(R.drawable.ic_action_name)

        listener.loadProfilePicture(currentRidePair.second.driver?.id.toString(), holder.profilePicture)

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
                .into(holder.capacityIndicator)


    }

    inner class RideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView by lazy { itemView.findViewById(R.id.ride_row__label__hour_value) }
        val price: TextView by lazy { itemView.findViewById(R.id.ride_row__label__euro_value) }
        val meetingPoint: TextView by lazy { itemView.findViewById(R.id.ride_row__label__meeting_point) }
        val profilePicture: ImageView by lazy { itemView.findViewById(R.id.ride_row__image__profile) }
        val capacityIndicator: ImageView by lazy { itemView.findViewById(R.id.ride_row__image__capacity_indicator) }

    }

}