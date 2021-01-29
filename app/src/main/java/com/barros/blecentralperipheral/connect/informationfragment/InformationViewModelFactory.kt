package com.barros.blecentralperipheral.connect.informationfragment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.utils.model.BleItem

class InformationViewModelFactory(
    private val context: Context,
    private val bleItem: BleItem
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InformationViewModel::class.java)) {
            return modelClass.getConstructor(Context::class.java, BleItem::class.java).newInstance(context, bleItem)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
