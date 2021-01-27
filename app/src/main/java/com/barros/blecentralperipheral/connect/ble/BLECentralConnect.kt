package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.model.BleItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class BLECentralConnect {
    private val bleScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private var bleItemList = mutableListOf<BleItem>()
    private var bleItemListChannel = Channel<List<BleItem>>()

    fun startScan() {
        Log.d(TAG, "Start Scan")
        val filters: MutableList<ScanFilter> = mutableListOf(
            ScanFilter.Builder().build()
        )
        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0)
            .build()

        bleScanner.startScan(filters, settings, leScanCallback)
    }

    fun stopScan() {
        Log.d(TAG, "Stop Scan")
        bleScanner.stopScan(leScanCallback)
        bleItemList.clear()
    }

    fun getBleItemListFlow(): Flow<List<BleItem>> = bleItemListChannel.consumeAsFlow()

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val bleItem = if (result.device.name != null) {
                BleItem(result.device, result.device.name, result.device.address, result.isConnectable)
            } else {
                BleItem(result.device, "N/A", result.device.address, result.isConnectable)
            }

            if (bleItemList.firstOrNull { it.device == result.device } == null) {
                bleItemList.add(bleItem)
                Log.d(TAG, "Offer response $bleItemList")
                bleItemListChannel.offer(bleItemList)
            }
        }
    }
}
