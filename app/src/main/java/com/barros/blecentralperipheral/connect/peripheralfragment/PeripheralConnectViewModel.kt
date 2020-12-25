package com.barros.blecentralperipheral.connect.peripheralfragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PeripheralConnectViewModel(application: Application) : AndroidViewModel(application) {

    private val _peripheralSwitch = MutableLiveData(false)
    val peripheralSwitch: LiveData<Boolean> = _peripheralSwitch

    private val _isShowError = MutableLiveData(false)
    val isShowError: LiveData<Boolean> = _isShowError

    private val _errorMessage = MutableLiveData("")
    val errorMessage: LiveData<String> = _errorMessage

    private val _sentValue = MutableLiveData("Nothing")
    val sentValue: LiveData<String> = _sentValue

    fun setPeripheralSwitch(isChecked: Boolean) {
        when (isChecked) {
            true -> {
                _peripheralSwitch.value = true
            }
            false -> {
                _peripheralSwitch.value = false
            }
        }
    }
}
