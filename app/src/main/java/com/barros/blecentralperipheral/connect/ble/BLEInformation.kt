package com.barros.blecentralperipheral.connect.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.util.Log
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.model.InformationItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow

class BLEInformation {
    var bleScanner: BluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner

    private val informationList = mutableListOf<InformationItem>()
    private val informationChannel = Channel<List<InformationItem>>()

    fun getInformationByAddress(address: String) {
        Log.d(TAG, "Start getServiceDataListByAddress")
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

    fun getServiceDataListFlow(): Flow<List<InformationItem>> = informationChannel.consumeAsFlow()

    fun stopGetServiceDataListByAddress() {
        Log.d(TAG, "Stop getServiceDataListByAddress")
        bleScanner.stopScan(leServiceDataCallback)
        informationList.clear()
    }

    private val leServiceDataCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val deviceName = InformationItem("Device Name", result.device.name ?: "N/A")
            val deviceType = InformationItem("Device Type", result.device.type.toString())
            val isConnectable = InformationItem("Connectable", result.isConnectable.toString())
            val advertisingSid = InformationItem("Advertising Sid", result.advertisingSid.toString())
            val dataStatus = InformationItem("Data Status", result.dataStatus.toString())
            val periodicAdvertisingInterval = InformationItem("Advertising Interval", result.periodicAdvertisingInterval.toString())
            val txPower = InformationItem("Tx Power", result.txPower.toString())

            if (!informationList.contains(deviceName))
                informationList.add(deviceName)

            if (!informationList.contains(deviceType))
                informationList.add(deviceType)

            if (!informationList.contains(isConnectable))
                informationList.add(isConnectable)

            if (!informationList.contains(advertisingSid))
                informationList.add(advertisingSid)

            if (!informationList.contains(dataStatus))
                informationList.add(dataStatus)

            if (!informationList.contains(periodicAdvertisingInterval))
                informationList.add(periodicAdvertisingInterval)

            if (!informationList.contains(txPower))
                informationList.add(txPower)

            informationChannel.offer(informationList)
        }
    }
}
