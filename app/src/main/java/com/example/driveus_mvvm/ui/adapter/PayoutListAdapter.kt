package com.example.driveus_mvvm.ui.adapter

import android.annotation.SuppressLint
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
import com.google.firebase.firestore.DocumentSnapshot
import java.text.SimpleDateFormat

private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, DocumentSnapshot>>() {

    override fun areItemsTheSame(oldItem: Pair<String, DocumentSnapshot>, newItem: Pair<String, DocumentSnapshot>): Boolean {
        return oldItem.second.id == newItem.second.id
    }

    override fun areContentsTheSame(oldItem: Pair<String, DocumentSnapshot>, newItem: Pair<String, DocumentSnapshot>): Boolean {
        return oldItem == newItem
    }

}

class PayoutListAdapter(
    private val listener: PayoutListAdapterListener
) : ListAdapter<Pair<String, DocumentSnapshot>, PayoutListAdapter.PayoutViewHolder>(diffCallback) {

    interface PayoutListAdapterListener {
        fun loadProfilePicture(userId: String?, imageView: ImageView)
        fun pressCheckbox(payoutDocSnap: DocumentSnapshot, checkBox: CheckBox)
        fun navigateToRide(payoutDocSnap: DocumentSnapshot)
        fun amITheDriver(passengerId: String): Boolean
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayoutListAdapter.PayoutViewHolder {
        val payoutListView = LayoutInflater.from(parent.context).inflate(R.layout.row_payout, parent, false)

        return PayoutViewHolder(payoutListView)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: PayoutListAdapter.PayoutViewHolder, position: Int) {
        val payout = getItem(position).second.toObject(Payout::class.java)
        val priceStr = "${payout?.price} €"

        val pattern = "HH:mm dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val creationDate = payout?.creationDate?.toDate()
        val dateValue = simpleDateFormat.format(creationDate).split(" ")
        val dateStr = "Creado a las ${dateValue[0]} el día ${dateValue[1]}"

        listener.loadProfilePicture(payout?.passenger?.id, holder.profilePicture)
        holder.price.text = priceStr
        holder.username.text = getItem(position).first
        holder.createDate.text = dateStr

        if (listener.amITheDriver(payout?.passenger?.id.toString())){
            holder.checkbox.visibility = View.VISIBLE
        } else {
            holder.checkbox.visibility = View.GONE
        }
    }

    inner class PayoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicture: ImageView by lazy { itemView.findViewById(R.id.payout_row__image__profile_picture) }
        val username: TextView by lazy { itemView.findViewById(R.id.payout_row__label__username) }
        val price: TextView by lazy { itemView.findViewById(R.id.payout_row__label__price) }
        val createDate: TextView by lazy { itemView.findViewById(R.id.payout_row__label__paid_date) }
        val checkbox: CheckBox by lazy { itemView.findViewById(R.id.payout_row__button__checkbox) }

        init {
            checkbox.setOnClickListener {
                listener.pressCheckbox(getItem(adapterPosition).second, checkbox)
            }

            itemView.setOnClickListener {
                listener.navigateToRide(getItem(adapterPosition).second)
            }
        }
    }

}
