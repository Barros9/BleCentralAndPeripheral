package com.barros.blecentralperipheral.utils.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class InformationItem(val title: String, val value: String) : Parcelable
