package com.barros.blecentralperipheral.connect.centralfragment

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.barros.blecentralperipheral.connect.ble.BLECentralConnect
import com.barros.blecentralperipheral.utils.PERMISSION_GRANTED
import com.barros.blecentralperipheral.utils.TIMEOUT
import com.barros.blecentralperipheral.utils.checkPermissionGranted
import com.barros.blecentralperipheral.utils.model.BleItem
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class CentralConnectViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext
    private val bleCentral = BLECentralConnect(context)
    private lateinit var countDownTimer: CountDownTimer

    private val _scanSwitch = MutableLiveData(false)
    val scanSwitch: LiveData<Boolean> = _scanSwitch

    private val _bleItems = MutableLiveData<List<BleItem>>(mutableListOf())
    val bleItems: LiveData<List<BleItem>> = _bleItems

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    private val _timer = MutableLiveData(TIMEOUT)
    val timer: LiveData<String> = _timer

    fun setScanSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> startScan()
            false -> stopScan()
        }
    }

    private fun startScan() {
        when (val resultCheckPermission = checkPermissionGranted(context)) {
            PERMISSION_GRANTED -> {
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
            else -> {
                _showToast.value = resultCheckPermission
            }
        }
    }

    private fun stopScan() {
        _scanSwitch.value = false
        _bleItems.value = mutableListOf()
        countDownTimer.onFinish()
        bleCentral.stopScan()
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(30_000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timer.value = (millisUntilFinished / 1_000).toString()
            }

            override fun onFinish() {
                _timer.value = TIMEOUT
                countDownTimer.cancel()
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

    override fun onCleared() {
        super.onCleared()
        bleCentral.stopScan()
    }
}
