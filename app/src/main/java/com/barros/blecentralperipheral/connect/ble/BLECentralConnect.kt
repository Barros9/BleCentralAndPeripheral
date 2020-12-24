package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.model.BleItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class BLECentralConnect {
    var bleScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private lateinit var bleItemList: MutableList<BleItem>
    private lateinit var bleItemListChannel: Channel<List<BleItem>>

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val bleItem = if (result.device.name != null) {
                BleItem(result.device.name, result.device.address, result.isConnectable)
            } else {
                BleItem("N/A", result.device.address, result.isConnectable)
            }

            if (!bleItemList.contains(bleItem)) {
                bleItemList.add(bleItem)
                bleItemListChannel.offer(bleItemList)
            }
        }
    }

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

        bleItemList = mutableListOf()
        bleItemListChannel = Channel()
        bleScanner.startScan(filters, settings, leScanCallback)
    }

    fun getBleItemListFlow(): Flow<List<BleItem>> = bleItemListChannel.consumeAsFlow()

    fun stopScan() {
        Log.d(TAG, "Stop Scan")
        bleScanner.stopScan(leScanCallback)
        bleItemList.clear()
        bleItemListChannel.close()
    }
}
