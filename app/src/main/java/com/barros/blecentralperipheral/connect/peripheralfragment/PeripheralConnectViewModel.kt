package com.barros.blecentralperipheral.connect.peripheralfragment

import android.app.Application
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.connect.ble.BLEPeripheralConnect
import com.barros.blecentralperipheral.utils.PERMISSION_GRANTED
import com.barros.blecentralperipheral.utils.checkPermissionGranted
import com.barros.blecentralperipheral.utils.model.Mode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch

class PeripheralConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val blePeripheral = BLEPeripheralConnect(context, bluetoothManager)
    private var isAlreadyStarted = false
    private lateinit var job: Job

    val sendingValue = MutableLiveData("")
    val mode = MutableLiveData(Mode.READ)

    private val _value = MutableLiveData(context.getString(R.string.nothing))
    val value: LiveData<String> = _value

    private val _peripheralSwitch = MutableLiveData(false)
    val peripheralSwitch: LiveData<Boolean> = _peripheralSwitch

    private val _isShowError = MutableLiveData(false)
    val isShowError: LiveData<Boolean> = _isShowError

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    fun setPeripheralSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                when (val resultCheckPermission = checkPermissionGranted(context)) {
                    PERMISSION_GRANTED -> {
                        _peripheralSwitch.value = true
                        when (mode.value) {
                            Mode.READ -> readMode()
                            Mode.NOTIFY -> notifyMode()
                            Mode.WRITE -> writeMode()
                            null -> Unit
                        }
                    }
                    else -> {
                        _showToast.value = resultCheckPermission
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
                _value.value = context.getString(R.string.nothing)
            }
        }
    }

    private fun startBlePeripheral() {
        if (isAlreadyStarted) {
            blePeripheral.stop()
        }
        blePeripheral.start(_value.value!!)
        isAlreadyStarted = true
    }

    fun changeMode() {
        if (isAlreadyStarted) {
            blePeripheral.stop()
            _value.value = context.getString(R.string.nothing)
            cancelJob()
            blePeripheral.start(_value.value!!)
        }
    }

    fun readMode() {
        when {
            !_peripheralSwitch.value!! -> {
                _isShowError.value = true
                _value.value = context.getString(R.string.nothing)
                _errorMessage.value = context.getString(R.string.active_peripheral_switch)
            }
            sendingValue.value!!.isBlank() || sendingValue.value!!.length < 4 -> {
                _isShowError.value = true
                _value.value = context.getString(R.string.nothing)
                _errorMessage.value = context.getString(R.string.insert_four_numbers)
            }
            else -> {
                _value.value = sendingValue.value
                _isShowError.value = false
                startBlePeripheral()
            }
        }
    }

    private fun notifyMode() {
        _value.value = context.getString(R.string.nothing)
        startBlePeripheral()
    }

    fun startNotify() {
        val devices = bluetoothManager.getConnectedDevices(BluetoothProfile.GATT)
        if (devices.isNotEmpty()) {
            job = viewModelScope.launch {
                repeat(10) {
                    _value.value = it.toString()
                    delay(3_000)
                    blePeripheral.notifyValue(it.toString())
                }
            }
        } else {
            _showToast.value = context.getString(R.string.no_device_connected)
        }
    }

    private fun writeMode() {
        _value.value = context.getString(R.string.nothing)
        startBlePeripheral()
        observeWrite()
    }

    private fun observeWrite() {
        viewModelScope.launch {
            blePeripheral.getWriteResponseFlow()
                .conflate()
                .collect {
                    _value.value = it
                }
        }
    }

    private fun cancelJob() {
        if (this::job.isInitialized && job.isActive) {
            job.cancel()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (isAlreadyStarted) {
            blePeripheral.stop()
        }
        cancelJob()
    }
}
