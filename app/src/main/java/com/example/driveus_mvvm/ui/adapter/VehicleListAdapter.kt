package com.example.driveus_mvvm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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

    override fun areContentsTheSame(oldItem: Pair<String, Vehicle>,
                                    newItem: Pair<String, Vehicle>
    ): Boolean {
        return oldItem == newItem
    }
}

class VehicleListAdapter(private val listener: VehicleListAdapterListener) : ListAdapter<Pair<String, Vehicle>, VehicleListAdapter.VehicleViewHolder>(diffCallback) {

    interface VehicleListAdapterListener {
        fun onDeleteButtonClick(vehicleId: String, model: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val vehicleView = LayoutInflater.from(parent.context).inflate(R.layout.row_car, parent, false)
        return VehicleViewHolder(vehicleView)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val currentVehicle: Pair<String, Vehicle> = getItem(position)

        holder.modelItemView.text = currentVehicle.second.brand + " " + currentVehicle.second.model
        holder.colorItemView.text = currentVehicle.second.color
        holder.seatsItemView.text = currentVehicle.second.seats.toString()
        holder.descriptionItemView.text = currentVehicle.second.description

        if (currentVehicle.second.expanded) {
            holder.expandableLayout.visibility = View.VISIBLE
        } else {
            holder.expandableLayout.visibility = View.GONE
        }

        if (currentVehicle.second.isInRide) {
            holder.deleteButton.visibility = View.GONE
        } else {
            holder.deleteButton.visibility = View.VISIBLE
        }
    }

    inner class VehicleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val modelItemView: TextView by lazy { itemView.findViewById(R.id.car_row_label_brand_model) }
        val colorItemView: TextView by lazy { itemView.findViewById(R.id.car_row__label_color_value) }
        val seatsItemView: TextView by lazy { itemView.findViewById(R.id.car_row__label_seat_value) }
        val descriptionItemView: TextView by lazy { itemView.findViewById(R.id.car_row__label_Description_value) }
        val expandableLayout: ConstraintLayout by lazy { itemView.findViewById(R.id.car_row__layout__expandable) }
        private val expandableImageMore: ImageView by lazy { itemView.findViewById(R.id.car_row__image__expandable_icon_more) }
        private val expandableImageLess: ImageView by lazy { itemView.findViewById(R.id.car_row__image__expandable_icon_less) }
        val deleteButton: Button by lazy { itemView.findViewById(R.id.car_row__button__delete_car) }

        init {
           modelItemView.setOnClickListener {
               if (expandableLayout.visibility == View.VISIBLE) {
                   expandableLayout.visibility = View.GONE
                   expandableImageLess.visibility = View.GONE
                   expandableImageMore.visibility = View.VISIBLE
               } else {
                   expandableLayout.visibility = View.VISIBLE
                   expandableImageLess.visibility = View.VISIBLE
                   expandableImageMore.visibility = View.GONE
               }
           }
            deleteButton.setOnClickListener {
                val model = getItem(adapterPosition).second.brand + " " + getItem(adapterPosition).second.brand
                listener.onDeleteButtonClick(getItem(adapterPosition).first, model)
            }
        }
    }
}