package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
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
    private val uuidConnect: UUID = UUID.fromString(context.getString(R.string.uuid_connect))
    private val uuidCharacteristic: UUID = UUID.fromString(context.getString(R.string.uuid_characteristic))
    inner class GattServerCallback : BluetoothGattServerCallback()
    var gattServerCallback: GattServerCallback = GattServerCallback()
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser

    lateinit var bluetoothGattServer: BluetoothGattServer
    lateinit var bluetoothGattService: BluetoothGattService

    fun startAdvertise(sendingMessage: String) {
        Log.d(TAG, "Start Advertise")
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        bluetoothGattService = BluetoothGattService(
            uuidConnect,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )

        val characteristic = BluetoothGattCharacteristic(
            uuidCharacteristic,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        characteristic.value = sendingMessage.toByteArray(Charsets.UTF_8)

        bluetoothGattService.addCharacteristic(characteristic)
        bluetoothGattServer.addService(bluetoothGattService)

        val parcelUuid = ParcelUuid(uuidConnect)

        val advertiseSettings = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            setConnectable(true)
            setTimeout(0)
        }.build()

        val advertiseData = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false)
            addServiceUuid(parcelUuid)
        }.build()

        bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            advertiseCallback
        )
    }

    fun tryNotify() {
        val characteristic = bluetoothGattService.getCharacteristic(uuidCharacteristic)
        characteristic.setValue("Prova")
        val device = bluetoothGattServer.connectedDevices[0] // TODO check this
        bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false)
    }

    fun stopAdvertise() {
        Log.d(TAG, "Stop Advertise")
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
        bluetoothGattServer.close()
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Peripheral advertise started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.d(TAG, "Peripheral advertise failed: $errorCode")
        }
    }
}
