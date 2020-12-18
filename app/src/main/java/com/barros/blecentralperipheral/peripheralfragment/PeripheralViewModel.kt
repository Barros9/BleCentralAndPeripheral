package com.barros.blecentralperipheral.peripheralfragment

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.barros.blecentralperipheral.ble.advertise.BLEPeripheralAdvertise

class PeripheralViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val blePeripheral = BLEPeripheralAdvertise(context, bluetoothManager)
    private var isAlreadyAdvertising = false

    val sendingValue = MutableLiveData("")

    private val _sentValue = MutableLiveData("Nothing")
    val sentValue: LiveData<String> = _sentValue

    private val _peripheralSwitch = MutableLiveData(false)
    val peripheralSwitch: LiveData<Boolean> = _peripheralSwitch

    private val _requestBluetooth = MutableLiveData(false)
    val requestBluetooth: LiveData<Boolean> = _requestBluetooth

    private val _requestLocation = MutableLiveData(false)
    val requestLocation: LiveData<Boolean> = _requestLocation

    private val _isShowError = MutableLiveData(false)
    val isShowError: LiveData<Boolean> = _isShowError

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    fun setPeripheralSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                if (hasPermission()) {
                    _peripheralSwitch.value = true
                    updateSentValue()
                }
            }
            false -> {
                if (isAlreadyAdvertising) {
                    blePeripheral.stopAdvertise()
                }
                _peripheralSwitch.value = false
                isAlreadyAdvertising = false
                _isShowError.value = false
                _sentValue.value = "Nothing"
            }
        }
    }

    fun updateSentValue() {
        when {
            !_peripheralSwitch.value!! -> {
                _isShowError.value = true
                _sentValue.value = "Nothing"
                _errorMessage.value = "Active peripheral switch"
            }
            sendingValue.value!!.isBlank() || sendingValue.value!!.length < 4 -> {
                _isShowError.value = true
                _sentValue.value = "Nothing"
                _errorMessage.value = "Insert 4 numbers"
            }
            else -> {
                _sentValue.value = sendingValue.value
                _isShowError.value = false
                startAdvertise()
            }
        }
    }

    private fun startAdvertise() {
        if (isAlreadyAdvertising) {
            blePeripheral.stopAdvertise()
        }
        blePeripheral.startAdvertise(_sentValue.value!!)
        isAlreadyAdvertising = true
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
        return checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        if (isAlreadyAdvertising) {
            blePeripheral.stopAdvertise()
        }
    }
}