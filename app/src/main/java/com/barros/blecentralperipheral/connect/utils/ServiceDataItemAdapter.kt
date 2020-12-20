package com.barros.blecentralperipheral.connect.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barros.blecentralperipheral.connect.model.ServiceDataItem
import com.barros.blecentralperipheral.databinding.ServiceDataItemBinding

class ServiceDataItemAdapter :
    ListAdapter<ServiceDataItem, ServiceDataItemAdapter.ItemViewHolder>(DiffCallbackServiceData()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ServiceDataItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ItemViewHolder(private val binding: ServiceDataItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ServiceDataItem) {
            binding.serviceDataItem = item
            binding.executePendingBindings()
        }
    }
}

class DiffCallbackServiceData : DiffUtil.ItemCallback<ServiceDataItem>() {
    override fun areItemsTheSame(oldItem: ServiceDataItem, newItem: ServiceDataItem): Boolean {
        return oldItem.uuid == newItem.uuid
    }

    override fun areContentsTheSame(oldItem: ServiceDataItem, newItem: ServiceDataItem): Boolean {
        return oldItem == newItem
    }
}
