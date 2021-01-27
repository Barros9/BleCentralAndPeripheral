package com.barros.blecentralperipheral.connect.centralfragment

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.os.CountDownTimer
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.ble.BLECentralConnect
import com.barros.blecentralperipheral.connect.model.BleItem
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class CentralConnectViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter = bluetoothManager.adapter
    private val bleCentral = BLECentralConnect()

    private val _scanSwitch = MutableLiveData(false)
    val scanSwitch: LiveData<Boolean> = _scanSwitch

    private val _bleItems = MutableLiveData<List<BleItem>>(mutableListOf())
    val bleItems: LiveData<List<BleItem>> = _bleItems

    private val _requestBluetooth = MutableLiveData(false)
    val requestBluetooth: LiveData<Boolean> = _requestBluetooth

    private val _requestLocation = MutableLiveData(false)
    val requestLocation: LiveData<Boolean> = _requestLocation

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    private val _timer = MutableLiveData("30")
    val timer: LiveData<String> = _timer

    fun setScanSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> startScan()
            false -> stopScan()
        }
    }

    private fun startScan() {
        if (hasPermission()) {
            viewModelScope.launch {
                try {
                    withTimeout(30_000) {
                        startTimer()
                        _scanSwitch.value = true
                        bleCentral.startScan()
                        observeBleItemList()
                    }
                } catch (e: TimeoutCancellationException) {
                    stopScan()
                }
            }
        }
    }

    private fun stopScan() {
        _scanSwitch.value = false
        _bleItems.value = mutableListOf()
        bleCentral.stopScan()
    }

    private fun startTimer() {
        val countDownTimer = object : CountDownTimer(30_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1_000
                Log.d(TAG, seconds.toString())
                _timer.value = seconds.toString()
            }

            override fun onFinish() {
                _timer.value = "30"
            }
        }
        countDownTimer.start()
    }

    private suspend fun observeBleItemList() {
        bleCentral.getBleItemListFlow()
            .conflate()
            .collect {
                _bleItems.value = it
            }
    }

    private fun hasPermission(): Boolean {
        if (!bluetoothAdapter.isEnabled) {
            _showToast.value = "Bluetooth not enabled"
            _requestBluetooth.value = true
            return false
        } else if (!hasLocationPermission()) {
            _showToast.value = "No location permission"
            _requestLocation.value = true
            return false
        } else if (!isGPSEnabled()) {
            _showToast.value = "GPS not enabled"
            return false
        }

        return true
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun hasLocationPermission(): Boolean {
        return PermissionChecker.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PermissionChecker.PERMISSION_GRANTED
    }

    override fun onCleared() {
        super.onCleared()
        bleCentral.stopScan()
    }
}
