package com.barros.blecentralperipheral.utils

import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.utils.adapter.BleItemAdapter
import com.barros.blecentralperipheral.utils.adapter.InformationAdapter
import com.barros.blecentralperipheral.utils.model.BleItem
import com.barros.blecentralperipheral.utils.model.InformationItem

@BindingAdapter("listBleItem")
fun bindBleItemRecyclerView(recyclerView: RecyclerView, data: List<BleItem>) {
    val adapter = recyclerView.adapter as BleItemAdapter
    adapter.submitList(data)
    adapter.notifyDataSetChanged()
}

@BindingAdapter("listInformationItem")
fun bindInformationItemRecyclerView(recyclerView: RecyclerView, data: List<InformationItem>) {
    val adapter = recyclerView.adapter as InformationAdapter
    adapter.submitList(data)
    adapter.notifyDataSetChanged()
}

@BindingAdapter("customBackgroundTint")
fun backgroundTint(cardView: CardView, isMyUuid: Boolean) {
    if (isMyUuid) {
        cardView.setCardBackgroundColor(cardView.resources.getColor(R.color.colorSelect, null))
    } else {
        cardView.setCardBackgroundColor(cardView.resources.getColor(R.color.colorPrimaryDark, null))
    }
}
