package com.barros.blecentralperipheral.connect.informationfragment

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.connect.ble.BLEInformation
import com.barros.blecentralperipheral.utils.PERMISSION_GRANTED
import com.barros.blecentralperipheral.utils.checkPermissionGranted
import com.barros.blecentralperipheral.utils.model.BleItem
import com.barros.blecentralperipheral.utils.model.InformationItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InformationViewModel(val context: Context, item: BleItem) : ViewModel() {

    private val bleCentral = BLEInformation()

    private val _serviceDataItems = MutableLiveData<List<InformationItem>>(mutableListOf())
    val informationItems: LiveData<List<InformationItem>> = _serviceDataItems

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    init {
        when (val resultCheckPermission = checkPermissionGranted(context)) {
            PERMISSION_GRANTED -> {
                bleCentral.getInformationByAddress(item.address)
                observeInformationList()
            }
            else -> {
                _showToast.value = resultCheckPermission
            }
        }
    }

    private fun observeInformationList() {
        viewModelScope.launch {
            bleCentral.getInformationListFlow().collect {
                _serviceDataItems.value = it
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleCentral.stopGetServiceDataListByAddress()
    }
}
