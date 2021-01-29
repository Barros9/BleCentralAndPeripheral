package com.barros.blecentralperipheral.connect.devicefragment

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.utils.TAG
import com.barros.blecentralperipheral.utils.model.BleItem
import com.barros.blecentralperipheral.utils.model.Mode
import java.util.UUID

class DeviceViewModel(val context: Context, item: BleItem) : ViewModel() {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private var isAlreadyConnected = false
    private var bleDiscoveredServices: List<BluetoothGattService> = listOf()
    private lateinit var bluetoothGatt: BluetoothGatt

    private val uuidConnect: UUID = UUID.fromString(context.getString(R.string.uuid_connect_service))
    private val uuidCharacteristic: UUID = UUID.fromString(context.getString(R.string.uuid_characteristic))
    private val uuidDescriptor: UUID = UUID.fromString(context.getString(R.string.uuid_descriptor))

    val mode = MutableLiveData(Mode.READ)
    val sendingValue = MutableLiveData("")

    private val _bleItem = MutableLiveData<BleItem>()
    val bleItem: LiveData<BleItem> = _bleItem

    private val _connectSwitch = MutableLiveData(false)
    val connectSwitch: LiveData<Boolean> = _connectSwitch

    private val _isShowError = MutableLiveData(false)
    val isShowError: LiveData<Boolean> = _isShowError

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _readValue = MutableLiveData(context.getString(R.string.read_nothing))
    val readValue: LiveData<String> = _readValue

    private val _notifyValue = MutableLiveData(context.getString(R.string.notify_nothing))
    val notifyValue: LiveData<String> = _notifyValue

    private val _writeValue = MutableLiveData(context.getString(R.string.write_nothing))
    val writeValue: LiveData<String> = _writeValue

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
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        isAlreadyConnected = true
    }

    private fun disconnect() {
        bluetoothGatt.disconnect()
        isAlreadyConnected = false
    }

    fun changeMode() {
        if (isAlreadyConnected) {
            bluetoothGatt.disconnect()

            when (mode.value) {
                Mode.READ -> _readValue.value = context.getString(R.string.read_nothing)
                Mode.NOTIFY -> _notifyValue.value = context.getString(R.string.notify_nothing)
                Mode.WRITE -> _writeValue.value = context.getString(R.string.write_nothing)
            }

            bluetoothGatt.connect()
        }
    }

    fun read() {
        if (_connectSwitch.value == true) {
            _isShowError.value = false
            if (bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).isNotEmpty() && bleDiscoveredServices.isNotEmpty()) {
                bleDiscoveredServices.first { it.uuid == uuidConnect }.characteristics.first { it.uuid == uuidCharacteristic }?.let {
                    bluetoothGatt.readCharacteristic(it)
                }
            }
        } else {
            _showToast.value = context.getString(R.string.active_connect_switch)
        }
    }

    fun write() {
        when {
            !_connectSwitch.value!! -> {
                _isShowError.value = true
                _writeValue.value = context.getString(R.string.nothing)
                _errorMessage.value = context.getString(R.string.active_connect_switch)
            }
            sendingValue.value!!.isBlank() || sendingValue.value!!.length < 4 -> {
                _isShowError.value = true
                _writeValue.value = context.getString(R.string.nothing)
                _errorMessage.value = context.getString(R.string.insert_four_numbers)
            }
            else -> {
                _writeValue.value = sendingValue.value
                _isShowError.value = false

                val characteristic = bluetoothGatt.getService(uuidConnect).getCharacteristic(uuidCharacteristic)
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                characteristic.value = _writeValue.value?.toByteArray(Charsets.UTF_8)
                bluetoothGatt.writeCharacteristic(characteristic)
            }
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Connected GATT server")
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Disconnected GATT server")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.i(TAG, "onServicesDiscovered gattSuccess: Services discovered")
                    bleDiscoveredServices = gatt.services

                    bleDiscoveredServices.first { it.uuid == uuidConnect }.characteristics.first { it.uuid == uuidCharacteristic }?.let { characteristic ->
                        bluetoothGatt.setCharacteristicNotification(characteristic, true)

                        characteristic.descriptors.first { it.uuid == uuidDescriptor }?.let {
                            it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            gatt.writeDescriptor(it)
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "onServicesDiscovered status received:: $status")
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    val value = String(characteristic.value)
                    Log.i(TAG, "onCharacteristicRead gattSuccess: $value")
                    _readValue.postValue(value)
                    _notifyValue.postValue(value)
                }
                else -> {
                    Log.i(TAG, "onCharacteristicRead status received: $status")
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            Log.i(TAG, "onCharacteristicChanged")
            if (characteristic != null && characteristic.uuid == uuidCharacteristic) {
                _notifyValue.postValue(String(characteristic.value))
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            Log.i(TAG, "onDescriptorWrite")
            if (uuidDescriptor == descriptor?.uuid) {
                val characteristic = gatt?.services?.first { it.uuid == uuidConnect }?.getCharacteristic(uuidCharacteristic)
                gatt?.readCharacteristic(characteristic)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
