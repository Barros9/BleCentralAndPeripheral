package com.barros.blecentralperipheral.connect.peripheralfragment

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.ble.BLEPeripheralConnect
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class PeripheralConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val blePeripheral = BLEPeripheralConnect(context, bluetoothManager)
    private var isAlreadyStarted = false

    val sendingValue = MutableLiveData("")
    val mode = MutableLiveData(Mode.READ)

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
                    when (mode.value) {
                        Mode.READ -> readMode()
                        Mode.NOTIFY -> notifyMode()
                        Mode.WRITE -> writeMode()
                        null -> Unit
                    }
                }
            }
            false -> {
                if (isAlreadyStarted) {
                    blePeripheral.stop()
                }
                isAlreadyStarted = false
                _peripheralSwitch.value = false
                _isShowError.value = false
                _sentValue.value = "Nothing"
            }
        }
    }

    fun readMode() {
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
                startBlePeripheral()
            }
        }
    }

    private fun startBlePeripheral() {
        if (isAlreadyStarted) {
            blePeripheral.stop()
        }
        blePeripheral.start(_sentValue.value!!)
        isAlreadyStarted = true
    }

    private fun notifyMode() {
        _sentValue.value = "Nothing"
        startBlePeripheral()
    }

    fun startNotify() {
        val devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        if (devices.isNotEmpty()) {
            viewModelScope.launch {
                repeat(10) {
                    _sentValue.value = it.toString()
                    Log.d(TAG, "Notify ${_sentValue.value}")
                    delay(3_000)
                    blePeripheral.notifyValue(it.toString())
                }
            }
        } else {
            _showToast.value = "No device connected"
        }
    }

    private fun writeMode() {
        _sentValue.value = "Nothing"
        startBlePeripheral()
        observeWrite()
    }

    private fun observeWrite() {
        viewModelScope.launch {
            blePeripheral.getWriteResponseFlow()
                .conflate()
                .collect {
                    _sentValue.value = it
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
        if (isAlreadyStarted) {
            blePeripheral.stop()
        }
    }
}
