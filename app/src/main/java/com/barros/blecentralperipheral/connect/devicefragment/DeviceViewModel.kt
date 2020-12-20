package com.barros.blecentralperipheral.connect.devicefragment

import android.bluetooth.*
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.model.BleItem

class DeviceViewModel(val context: Context, item: BleItem) : ViewModel() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter

    private lateinit var bluetoothGatt: BluetoothGatt

    private val _bleItem = MutableLiveData<BleItem>()
    val bleItem: LiveData<BleItem> = _bleItem

    private val _notifySwitch = MutableLiveData(false)
    val notifySwitch: LiveData<Boolean> = _notifySwitch

    private val _connectSwitch = MutableLiveData(false)
    val connectSwitch: LiveData<Boolean> = _connectSwitch

    private val _isShowError = MutableLiveData(false)
    val isShowError: LiveData<Boolean> = _isShowError

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _readValue = MutableLiveData("Nothing")
    val readValue: LiveData<String> = _readValue

    init {
        _bleItem.value = item
    }

    fun setConnectSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                _connectSwitch.value = true
                connect()
            }
            false -> {
                _connectSwitch.value = false
                disconnect()
            }
        }
    }

    private fun connect() {
        val bluetoothDevice = bluetoothAdapter.getRemoteDevice(_bleItem.value!!.address)
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback)
    }

    private fun disconnect() {
        bluetoothGatt.disconnect()
    }

    fun setNotifySwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                _notifySwitch.value = true
            }
            false -> {
                _notifySwitch.value = false
            }
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Connected from GATT server.")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Disconnected from GATT server.")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED")
                }
                else -> Log.d(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(TAG, "ACTION_DATA_AVAILABLE")
                }
            }
        }
    }
}
