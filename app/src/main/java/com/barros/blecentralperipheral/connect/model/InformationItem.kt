package com.barros.blecentralperipheral.connect.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InformationItem(val title: String, val value: String) : Parcelable
