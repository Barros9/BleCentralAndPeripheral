package com.barros.blecentralperipheral.utils.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barros.blecentralperipheral.databinding.BleItemBinding
import com.barros.blecentralperipheral.utils.model.BleItem

class BleItemAdapter(private val onClickListener: OnClickListener) :
    ListAdapter<BleItem, BleItemAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(BleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onClickListener)
    }

    class ItemViewHolder(private val binding: BleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BleItem, onClickListener: OnClickListener) {
            binding.bleItem = item
            binding.information.setOnClickListener { onClickListener.onInformationClick(item) }
            binding.connect.setOnClickListener { onClickListener.onConnectClick(item) }
            binding.executePendingBindings()
        }
    }

    class OnClickListener(
        val clickInformationListener: (item: BleItem) -> Unit,
        val clickConnectListener: (item: BleItem) -> Unit
    ) {
        fun onInformationClick(item: BleItem) = clickInformationListener(item)
        fun onConnectClick(item: BleItem) = clickConnectListener(item)
    }

    class DiffCallback : DiffUtil.ItemCallback<BleItem>() {
        override fun areItemsTheSame(oldItem: BleItem, newItem: BleItem): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: BleItem, newItem: BleItem): Boolean {
            return oldItem == newItem
        }
    }
}
