package com.barros.blecentralperipheral.connect.characteristicfragment

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.connect.ble.BLECharacteristic
import com.barros.blecentralperipheral.connect.model.BleItem
import com.barros.blecentralperipheral.connect.model.ServiceDataItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CharacteristicViewModel(val context: Context, item: BleItem) : ViewModel() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleCentral = BLECharacteristic()

    private val _serviceDataItems = MutableLiveData<List<ServiceDataItem>>(mutableListOf())
    val serviceDataItems: LiveData<List<ServiceDataItem>> = _serviceDataItems

    private val _requestBluetooth = MutableLiveData(false)
    val requestBluetooth: LiveData<Boolean> = _requestBluetooth

    private val _requestLocation = MutableLiveData(false)
    val requestLocation: LiveData<Boolean> = _requestLocation

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    init {
        if (hasPermission()) {
            bleCentral.getServiceDataListByAddress(item.address)
            observeServiceDataList()
        }
    }

    private fun observeServiceDataList() {
        viewModelScope.launch {
            bleCentral.getServiceDataListFlow().collect {
                _serviceDataItems.value = it
            }
        }
    }

    private fun hasPermission(): Boolean {
        if (!bluetoothAdapter.isEnabled) {
            _showToast.value = "Bluetooth not enabled"
            _requestBluetooth.value = true
            return false
        } else if (!hasLocationPermission()) {
            _showToast.value = "No location permission"
            _requestLocation.value = true
            return false
        }
        return true
    }

    private fun hasLocationPermission(): Boolean {
        return PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        bleCentral.stopGetServiceDataListByAddress()
    }
}
