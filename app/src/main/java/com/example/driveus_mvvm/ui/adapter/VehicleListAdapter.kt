package com.example.driveus_mvvm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.Vehicle

private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, Vehicle>>() {

    override fun areItemsTheSame(oldItem: Pair<String, Vehicle>,
                                 newItem: Pair<String, Vehicle>
    ): Boolean {
        return  oldItem.first == newItem.first
    }

    override fun areContentsTheSame(oldItem: Pair<String, Vehicle>
                                    , newItem: Pair<String, Vehicle>
    ): Boolean {
        return oldItem == newItem
    }
}

class VehicleListAdapter() : ListAdapter<Pair<String, Vehicle>, VehicleListAdapter.VehicleViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val vehicleView = LayoutInflater.from(parent.context).inflate(R.layout.car_row, parent, false)
        return VehicleViewHolder(vehicleView)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val currentVehicle: Pair<String, Vehicle> = getItem(position)

        holder.modelItemView.text = currentVehicle.second.brand + " " + currentVehicle.second.model
        holder.colorItemView.text = currentVehicle.second.color
        holder.seatsItemView.text = currentVehicle.second.seats.toString()
        holder.descriptionlItemView.text = currentVehicle.second.description

        if (currentVehicle.second.expanded) {
            holder.expandableLayout.visibility = View.VISIBLE
        } else {
            holder.expandableLayout.visibility = View.GONE
        }
    }

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelItemView: TextView by lazy { itemView.findViewById(R.id.car_row_label_brand_model) }
        val colorItemView: TextView by lazy { itemView.findViewById(R.id.car_row__label_color_value) }
        val seatsItemView: TextView by lazy { itemView.findViewById(R.id.car_row__label_seat_value) }
        val descriptionlItemView: TextView by lazy { itemView.findViewById(R.id.car_row__label_Description_value) }
        val expandableLayout : ConstraintLayout by lazy { itemView.findViewById(R.id.car_row__layout__expandable) }
        init {
           modelItemView.setOnClickListener {
               if (expandableLayout.visibility == View.VISIBLE) {
                   expandableLayout.visibility = View.GONE
               } else {
                   expandableLayout.visibility = View.VISIBLE
               }
           }
        }
    }

}