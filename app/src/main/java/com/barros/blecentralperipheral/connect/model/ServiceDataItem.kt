package com.barros.blecentralperipheral.connect.model

import android.os.Parcelable
import java.util.UUID
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceDataItem(val uuid: UUID, val data: String) : Parcelable
