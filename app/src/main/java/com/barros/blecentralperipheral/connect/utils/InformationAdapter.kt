package com.barros.blecentralperipheral.connect.utils

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barros.blecentralperipheral.connect.model.InformationItem
import com.barros.blecentralperipheral.databinding.InformationItemBinding

class InformationAdapter :
    ListAdapter<InformationItem, InformationAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            InformationItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ItemViewHolder(private val binding: InformationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InformationItem) {
            binding.informationItem = item
            binding.executePendingBindings()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<InformationItem>() {
        override fun areItemsTheSame(oldItem: InformationItem, newItem: InformationItem): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: InformationItem, newItem: InformationItem): Boolean {
            return oldItem == newItem
        }
    }
}
