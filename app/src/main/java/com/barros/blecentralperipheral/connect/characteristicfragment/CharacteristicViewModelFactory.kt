package com.barros.blecentralperipheral.connect.characteristicfragment

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.connect.model.BleItem

class CharacteristicViewModelFactory(
    private val context: Context,
    private val bleItem: BleItem
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharacteristicViewModel::class.java)) {
            return modelClass.getConstructor(Context::class.java, BleItem::class.java).newInstance(context, bleItem)
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
