package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
import android.util.Log
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.model.ServiceDataItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class BLECharacteristic {
    var bleScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    private val serviceDataItemList = mutableListOf<ServiceDataItem>()
    private val serviceDataItemChannel = Channel<List<ServiceDataItem>>()

    private val leServiceDataCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            result.scanRecord?.serviceData?.entries?.forEach {
                val serviceDataItem = ServiceDataItem(it.key.uuid, String(it.value))
                if (!serviceDataItemList.contains(serviceDataItem)) {
                    serviceDataItemList.add(serviceDataItem)
                    serviceDataItemChannel.offer(serviceDataItemList)
                }
            }
        }
    }

    fun getServiceDataListByAddress(address: String) {
        Log.d(TAG, "Start Scan")
        val filters: MutableList<ScanFilter> = mutableListOf(
            ScanFilter.Builder()
                .setDeviceAddress(address)
                .build()
        )
        val settings: ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0)
            .build()

        bleScanner.startScan(filters, settings, leServiceDataCallback)
    }

    fun getServiceDataListFlow(): Flow<List<ServiceDataItem>> = serviceDataItemChannel.consumeAsFlow()

    fun stopGetServiceDataListByAddress() {
        Log.d(TAG, "stopGetServiceDataListByAddress")
        bleScanner.stopScan(leServiceDataCallback)
        serviceDataItemList.clear()
        serviceDataItemChannel.close()
    }
}
