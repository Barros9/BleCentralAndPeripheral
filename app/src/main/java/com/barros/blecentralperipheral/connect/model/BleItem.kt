package com.barros.blecentralperipheral.connect.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BleItem(val name: String, val address: String) : Parcelable
