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

class DebtListAdapter (
    private val listener: DebtListAdapterListener
): ListAdapter<Pair<String, DocumentSnapshot>, DebtListAdapter.DebtViewHolder>(diffCallback) {

    interface DebtListAdapterListener {
        fun loadProfilePicture(userId: String?, driverId: String?, imageView: ImageView)
        fun pressCheckbox(payoutDocSnap: DocumentSnapshot, checkBox: CheckBox)
        fun amIThePassenger(passengerId: String): Boolean
        fun navigateToRideDetail(payoutDocSnap: DocumentSnapshot)
        fun getUsername(passengerId:String, passengerUsername: String?, driverUsername: String?): String
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DebtListAdapter.DebtViewHolder {
        val debtListView = LayoutInflater.from(parent.context).inflate(R.layout.row_payout, parent, false)

        return DebtViewHolder(debtListView)
    }

    override fun onBindViewHolder(holder: DebtListAdapter.DebtViewHolder, position: Int) {
        val payout = getItem(position).second.toObject(Payout::class.java)
        val priceStr = "${payout?.price} €"

        val pattern = "HH:mm dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val paidDate = payout?.paidDate?.toDate()
        val dateValue = simpleDateFormat.format(paidDate).split(" ")
        val dateStr = "Pagado a las ${dateValue[0]} el día ${dateValue[1]}"

        listener.loadProfilePicture(payout?.passenger?.id, payout?.driver?.id, holder.profilePicture)
        holder.price.text = priceStr
        holder.username.text = payout?.passenger?.id?.let { listener.getUsername(it,getItem(position).first, payout.driverUsername) }
        holder.createDate.text = dateStr

        if (listener.amIThePassenger(payout?.passenger?.id.toString())){
            holder.checkbox.visibility = View.VISIBLE
        } else {
            holder.checkbox.visibility = View.GONE
        }
    }

    inner class DebtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
                listener.navigateToRideDetail(getItem(adapterPosition).second)
            }
        }
    }

}