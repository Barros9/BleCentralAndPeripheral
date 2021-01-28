package com.barros.blecentralperipheral.connect.model

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BleItem(
    val device: BluetoothDevice,
    val name: String,
    val address: String,
    val isConnectable: Boolean,
    val isMyUuid: Boolean = false
) : Parcelable
