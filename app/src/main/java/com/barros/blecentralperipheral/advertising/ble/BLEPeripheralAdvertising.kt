package com.barros.blecentralperipheral.advertising.ble

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
import com.barros.blecentralperipheral.utils.TAG
import java.util.UUID

class BLEPeripheralAdvertising(
    private val context: Context,
    private val bluetoothManager: BluetoothManager
) {
    private val uuidAdvertisingService: UUID = UUID.fromString(context.getString(R.string.uuid_advertising_service))
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
        Log.i(TAG, "Start Advertising")

        val bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        val parcelUuid = ParcelUuid(uuidAdvertisingService)

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
            addServiceData(parcelUuid, sentValue.toByteArray(Charsets.UTF_8))
        }.build()

        bluetoothLeAdvertiser.startAdvertising(
            advertiseSettings,
            advertiseData,
            scanResponse,
            advertiseCallback
        )
    }

    private fun stopAdvertising() {
        Log.i(TAG, "Stop Advertising")
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

    private fun startServer() {
        Log.i(TAG, "Start Server")
        bluetoothGattServer = bluetoothManager.openGattServer(context, gattServerCallback)
        bluetoothGattServer.addService(createService())
    }

    private fun stopServer() {
        Log.i(TAG, "Stop Server")
        bluetoothGattServer.close()
    }

    private fun createService(): BluetoothGattService {
        Log.i(TAG, "Create Service")
        val bluetoothGattService = BluetoothGattService(uuidAdvertisingService, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        bluetoothGattServer.addService(bluetoothGattService)
        return bluetoothGattService
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Peripheral advertise started")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Peripheral advertise failed: $errorCode")
        }
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {}
}
