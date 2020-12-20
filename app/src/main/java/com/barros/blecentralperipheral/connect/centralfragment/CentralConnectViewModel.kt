package com.barros.blecentralperipheral.connect.centralfragment

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.PermissionChecker
import androidx.lifecycle.*
import com.barros.blecentralperipheral.connect.ble.BLECentralConnect
import com.barros.blecentralperipheral.connect.model.BleItem
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CentralConnectViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleCentral = BLECentralConnect()

    private val _scanSwitch = MutableLiveData(false)
    val scanSwitch: LiveData<Boolean> = _scanSwitch

    private val _bleItems = MutableLiveData<List<BleItem>>(mutableListOf())
    val bleItems: LiveData<List<BleItem>> = _bleItems

    private val _requestBluetooth = MutableLiveData(false)
    val requestBluetooth: LiveData<Boolean> = _requestBluetooth

    private val _requestLocation = MutableLiveData(false)
    val requestLocation: LiveData<Boolean> = _requestLocation

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    fun setScanSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                if (hasPermission()) {
                    _scanSwitch.value = true
                    bleCentral.startScan()
                    observeBleItemList()
                }
            }
            false -> {
                _scanSwitch.value = false
                _bleItems.value = mutableListOf()
                bleCentral.stopScan()
            }
        }
    }

    private fun observeBleItemList() {
        viewModelScope.launch {
            bleCentral.getBleItemListFlow().collect {
                _bleItems.value = it
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
        bleCentral.stopScan()
        bleCentral.stopGetServiceDataListByAddress()
    }
}
