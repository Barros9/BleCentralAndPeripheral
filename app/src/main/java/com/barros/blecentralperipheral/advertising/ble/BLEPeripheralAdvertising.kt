package com.barros.blecentralperipheral.advertising.ble

import android.bluetooth.BluetoothAdapter
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

class BLEPeripheralAdvertising(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    private val uuid: UUID = UUID.fromString(context.getString(R.string.uuid))
    inner class GattServerCallback : BluetoothGattServerCallback()
    var gattServerCallback: GattServerCallback = GattServerCallback()
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
    lateinit var bluetoothGattServer: BluetoothGattServer

    fun startAdvertise(sendingMessage: String) {
        Log.d(TAG, "Start Advertise")
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)

        val bluetoothGattService = BluetoothGattService(uuid, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        bluetoothGattServer.addService(bluetoothGattService)

        val parcelUuid = ParcelUuid(uuid)

        val advertiseSettings = AdvertiseSettings.Builder().apply {
            setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            setConnectable(true)
            setTimeout(0)
        }.build()

        val advertiseData = AdvertiseData.Builder().apply {
            setIncludeDeviceName(false)
            addServiceUuid(parcelUuid)
        }.build()

        val scanResponse = AdvertiseData.Builder().apply {
            addServiceData(parcelUuid, sendingMessage.toByteArray(Charsets.UTF_8))
        }.build()

        bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            scanResponse,
            advertiseCallback
        )
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
