package com.barros.blecentralperipheral.advertising.centralfragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.advertising.ble.BLECentralAdvertising
import com.barros.blecentralperipheral.utils.PERMISSION_GRANTED
import com.barros.blecentralperipheral.utils.checkPermissionGranted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class CentralAdvertisingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val bleCentral = BLECentralAdvertising(context)

    private val _receivedValue = MutableLiveData(context.getString(R.string.nothing))
    val receivedValue: LiveData<String> = _receivedValue

    private val _scanSwitch = MutableLiveData(false)
    val scanSwitch: LiveData<Boolean> = _scanSwitch

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    fun setScanSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                when (val resultCheckPermission = checkPermissionGranted(context)) {
                    PERMISSION_GRANTED -> {
                        _scanSwitch.value = true
                        bleCentral.startScan()
                        observeResponse()
                    }
                    else -> {
                        _showToast.value = resultCheckPermission
                    }
                }
            }
            false -> {
                _scanSwitch.value = false
                bleCentral.stopScan()
            }
        }
    }

    private fun observeResponse() {
        viewModelScope.launch {
            bleCentral.getResponseFlow()
                .conflate()
                .collect {
                    _receivedValue.value = it
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleCentral.stopScan()
    }
}
