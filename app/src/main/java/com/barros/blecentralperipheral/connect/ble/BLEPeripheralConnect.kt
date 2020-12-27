package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
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
import java.util.UUID

class BLEPeripheralConnect(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    private val uuidConnectService: UUID = UUID.fromString(context.getString(R.string.uuid_connect_service))
    private val uuidCharacteristic: UUID = UUID.fromString(context.getString(R.string.uuid_characteristic))

    private lateinit var bluetoothGattServer: BluetoothGattServer
    private lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser

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

    private fun startAdvertising() {
        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "Failed to create advertiser")
            return
        }

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
        if (bluetoothLeAdvertiser == null) {
            return
        }
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        if (bluetoothGattServer == null) {
            Log.e(TAG, "Failed to create GATT server")
            return
        }

        bluetoothGattServer.addService(createService())
    }

    private fun stopServer() {
        if (bluetoothGattServer == null) {
            return
        }
        bluetoothGattServer.close()
    }

    private fun createService(): BluetoothGattService {
        val bluetoothGattService = BluetoothGattService(uuidConnectService, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val bluetoothGattCharacteristic = BluetoothGattCharacteristic(
                uuidCharacteristic,
                BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        // TODO descriptor?

        bluetoothGattService.addCharacteristic(bluetoothGattCharacteristic)

        return bluetoothGattService
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
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic) {
            if (uuidCharacteristic == characteristic.uuid) {
                Log.i(TAG, "Read Characteristic")
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, sentValue.toByteArray(Charsets.UTF_8))
            } else {
                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.uuid)
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null)
            }
        }
    }
}
