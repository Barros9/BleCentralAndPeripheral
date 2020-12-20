package com.barros.blecentralperipheral.connect.utils

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barros.blecentralperipheral.connect.model.BleItem
import com.barros.blecentralperipheral.connect.model.ServiceDataItem

@BindingAdapter("listBleItem")
fun bindBleItemRecyclerView(recyclerView: RecyclerView, data: List<BleItem>) {
    val adapter = recyclerView.adapter as BleItemAdapter
    adapter.submitList(data)
    adapter.notifyDataSetChanged()
}

@BindingAdapter("listServiceDataItem")
fun bindServiceDataRecyclerView(recyclerView: RecyclerView, data: List<ServiceDataItem>) {
    val adapter = recyclerView.adapter as ServiceDataItemAdapter
    adapter.submitList(data)
    adapter.notifyDataSetChanged()
}
