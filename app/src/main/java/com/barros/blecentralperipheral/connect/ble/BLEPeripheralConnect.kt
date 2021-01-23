package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.TAG
import java.util.Arrays
import java.util.UUID

class BLEPeripheralConnect(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    private val uuidConnectService: UUID = UUID.fromString(context.getString(R.string.uuid_connect_service))
    private val uuidCharacteristic: UUID = UUID.fromString(context.getString(R.string.uuid_characteristic))
    private val uuidDescriptor: UUID = UUID.fromString(context.getString(R.string.uuid_descriptor))

    private lateinit var bluetoothGattServer: BluetoothGattServer
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    private val registeredDevices: MutableList<BluetoothDevice> = mutableListOf()

    private lateinit var sentValue: String

    fun start(value: String) {
        sentValue = value
        startAdvertising()
        startServer()
    }

    fun stop() {
        stopServer()
        stopAdvertising()
    }

    fun notifyValue(value: String) {
        sentValue = value
        bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach { device ->
            val bluetoothGattCharacteristic = bluetoothGattServer.getService(uuidConnectService).getCharacteristic(
                uuidCharacteristic
            )
            val updateValue = sentValue.toByteArray(Charsets.UTF_8)
            Log.i(TAG, "notifyValue ${String(updateValue)}")
            bluetoothGattCharacteristic.value = updateValue
            bluetoothGattServer.notifyCharacteristicChanged(
                device,
                bluetoothGattCharacteristic,
                false
            )
        }
    }

    private fun startAdvertising() {
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        val advertiseSettings = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            setConnectable(true)
            setTimeout(0)
        }.build()

        val advertiseData = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false)
            addServiceUuid(ParcelUuid(uuidConnectService))
        }.build()

        bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            advertiseCallback
        )
    }

    private fun stopAdvertising() {
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        bluetoothGattServer.addService(createService())
    }

    private fun stopServer() {
        bluetoothGattServer.close()
    }

    private fun createService(): BluetoothGattService {
        val bluetoothGattService = BluetoothGattService(
            uuidConnectService,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val bluetoothGattCharacteristic = BluetoothGattCharacteristic(
            uuidCharacteristic,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val bluetoothGattDescriptor = BluetoothGattDescriptor(
            uuidDescriptor,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )

        bluetoothGattCharacteristic.addDescriptor(bluetoothGattDescriptor)
        bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic)

        return bluetoothGattService
    }

    private fun notifyRegisteredDevices() {
        if (registeredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered")
            return
        }
        Log.i(TAG, "Sending update to " + registeredDevices.size + " subscribers")
        for (device in registeredDevices) {
            val characteristic: BluetoothGattCharacteristic = bluetoothGattServer
                .getService(uuidConnectService)
                .getCharacteristic(uuidCharacteristic)
            characteristic.value = sentValue.toByteArray(Charsets.UTF_8)
            bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false)
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.i(TAG, "Peripheral advertise started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.i(TAG, "Peripheral advertise failed: $errorCode")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: $device")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED: $device")
                registeredDevices.remove(device)
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (uuidCharacteristic == characteristic.uuid) {
                Log.i(TAG, "Read Characteristic")
                bluetoothGattServer.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, 0, sentValue.toByteArray(
                        Charsets.UTF_8
                    )
                )
            } else {
                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
                bluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null
                )
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            Log.i(TAG, "Write interactor")
            if (uuidCharacteristic == characteristic?.uuid) {
                // listener
                notifyRegisteredDevices()
            } else {
                Log.i(TAG, "Invalid Characteristic Write: " + characteristic!!.uuid)
                bluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null
                )
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            if (uuidDescriptor == descriptor?.uuid) {
                Log.i(TAG, "Configure read descriptor")
                val value = if (registeredDevices.contains(device)) {
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                } else {
                    BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                bluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_SUCCESS,
                    0,
                    value
                )
            } else {
                Log.i(TAG, "Unknown descriptor")
                bluetoothGattServer.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    null
                )
            }
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            if (uuidDescriptor == descriptor?.uuid) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.i(TAG, "Subscribe device to notifications: $device")
                    device?.let {
                        registeredDevices.add(it)
                    }
                } else {
                    Log.i(TAG, "Unsubscribe device from notifications: $device")
                    registeredDevices.remove(device)
                }

                if (responseNeeded) {
                    bluetoothGattServer.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        null
                    )
                }
            } else {
                Log.i(TAG, "Unknown descriptor write request")
                if (responseNeeded) {
                    bluetoothGattServer.sendResponse(
                        device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null
                    )
                }
            }
        }
    }
}
