package com.barros.blecentralperipheral.advertising.peripheralfragment

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.advertising.ble.BLEPeripheralAdvertising
import com.barros.blecentralperipheral.utils.PERMISSION_GRANTED
import com.barros.blecentralperipheral.utils.checkPermissionGranted

class PeripheralAdvertisingViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val blePeripheral = BLEPeripheralAdvertising(context, bluetoothManager)
    private var isAlreadyAdvertising = false

    val sendingValue = MutableLiveData("")

    private val _sentValue = MutableLiveData(context.getString(R.string.nothing))
    val sentValue: LiveData<String> = _sentValue

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
                        updateSentValue()
                    }
                    else -> {
                        _showToast.value = resultCheckPermission
                    }
                }
            }
            false -> {
                if (isAlreadyAdvertising) {
                    blePeripheral.stop()
                }
                isAlreadyAdvertising = false
                _peripheralSwitch.value = false
                _isShowError.value = false
                _sentValue.value = context.getString(R.string.nothing)
            }
        }
    }

    fun updateSentValue() {
        when {
            !_peripheralSwitch.value!! -> {
                _isShowError.value = true
                _sentValue.value = context.getString(R.string.nothing)
                _errorMessage.value = context.getString(R.string.active_peripheral_switch)
            }
            sendingValue.value!!.isBlank() || sendingValue.value!!.length < 4 -> {
                _isShowError.value = true
                _sentValue.value = context.getString(R.string.nothing)
                _errorMessage.value = context.getString(R.string.insert_four_numbers)
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
            blePeripheral.stop()
        }
        blePeripheral.start(_sentValue.value!!)
        isAlreadyAdvertising = true
    }

    override fun onCleared() {
        super.onCleared()
        if (isAlreadyAdvertising) {
            blePeripheral.stop()
        }
    }
}
