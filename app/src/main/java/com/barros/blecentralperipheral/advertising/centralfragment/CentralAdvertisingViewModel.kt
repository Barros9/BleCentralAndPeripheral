package com.barros.blecentralperipheral.advertising.centralfragment

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.advertising.ble.BLECentralAdvertising
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CentralAdvertisingViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleCentral = BLECentralAdvertising(context)

    private val _receiving = MutableLiveData("Nothing")
    val receiving: LiveData<String> = _receiving

    private val _scanSwitch = MutableLiveData(false)
    val scanSwitch: LiveData<Boolean> = _scanSwitch

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
                    observeResponse()
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
            bleCentral.getResponseFlow().collect {
                _receiving.value = it
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
    }
}
