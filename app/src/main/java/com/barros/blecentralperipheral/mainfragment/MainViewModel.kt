package com.barros.blecentralperipheral.mainfragment

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    private val _isEnabled = MutableLiveData(true)
    val isEnabled: LiveData<Boolean> = _isEnabled

    init {
        checkBLE()
    }

    private fun checkBLE() {
        val bluetoothAdapter = (getApplication<Application>().applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
        val packageManager = getApplication<Application>().applicationContext.packageManager

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            _showToast.value = "This device cannot use BLE functions"
            _isEnabled.value = false
        }

        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            _showToast.value = "This device cannot use MultipleAdvertisement functions"
            _isEnabled.value = false
        }
    }
}
