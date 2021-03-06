package com.barros.blecentralperipheral.advertising.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.utils.TAG
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class BLECentralAdvertising(context: Context) {
    private val uuidAdvertisingService: UUID = UUID.fromString(context.getString(R.string.uuid_advertising_service))
    private val bleScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private val channel = Channel<String>()

    fun startScan() {
        Log.i(TAG, "Start Scan")
        val filters: MutableList<ScanFilter> = mutableListOf(
            ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(uuidAdvertisingService))
                .build()
        )
        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        bleScanner.startScan(filters, settings, leScanCallback)
    }

    fun stopScan() {
        Log.i(TAG, "Stop Scan")
        bleScanner.stopScan(leScanCallback)
    }

    fun getResponseFlow(): Flow<String> = channel.consumeAsFlow()

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            result.scanRecord?.serviceData?.entries?.firstOrNull()?.value?.let {
                channel.offer(String(it))
            }
        }
    }
}
