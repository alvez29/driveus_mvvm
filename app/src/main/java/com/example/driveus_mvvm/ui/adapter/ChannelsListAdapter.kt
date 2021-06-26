package com.example.driveus_mvvm.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.model.entities.Channel


private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, Channel>>() {

    override fun areItemsTheSame(
            oldItem: Pair<String, Channel>,
            newItem: Pair<String, Channel>
    ): Boolean {
        return oldItem.first == newItem.first
    }

    override fun areContentsTheSame(
            oldItem: Pair<String, Channel>,
            newItem: Pair<String, Channel>
    ): Boolean {
        return oldItem == newItem
    }

}

class AllChannelsListAdapter(
        private val listener: AllChannelsListAdapterListener
) : ListAdapter<Pair<String, Channel>, AllChannelsListAdapter.ChannelViewHolder>(diffCallback) {

    interface AllChannelsListAdapterListener {
        fun onItemClick(channelDocId: String)
        fun onSubscribeClick(channelDocId: String)
        fun onUnsubscribeClick(channelDocId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val channelView = LayoutInflater.from(parent.context).inflate(R.layout.row_channel, parent, false)

        return ChannelViewHolder(channelView)
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        val currentChannelPair = getItem(position)

        holder.originZoneText.text = currentChannelPair.second.originZone
        holder.destinationZoneText.text = currentChannelPair.second.destinationZone

    }

    inner class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val originZoneText: TextView by lazy { itemView.findViewById(R.id.channel_row__label__channel_origin_zone) }
        val destinationZoneText: TextView by lazy { itemView.findViewById(R.id.channel_row__label__channel_destination_zone) }
        val subscribeButton: ImageButton by lazy { itemView.findViewById(R.id.channel_row__button__subscribe) }

        init {
            itemView.setOnClickListener {
                listener.onItemClick(getItem(adapterPosition).first)
            }

            subscribeButton.setOnClickListener {
                //TODO
            }
        }
    }

}