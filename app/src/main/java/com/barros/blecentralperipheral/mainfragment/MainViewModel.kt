package com.barros.blecentralperipheral.mainfragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.barros.blecentralperipheral.utils.PERMISSION_GRANTED
import com.barros.blecentralperipheral.utils.checkPermissionGranted

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = getApplication<Application>().applicationContext
    val isEnabled = MutableLiveData(false)

    private val _showToast = MutableLiveData("")
    val showToast: LiveData<String> = _showToast

    init {
        when (val resultCheckPermission = checkPermissionGranted(context)) {
            PERMISSION_GRANTED -> {
                isEnabled.value = true
            }
            else -> {
                isEnabled.value = false
                _showToast.value = resultCheckPermission
            }
        }
    }
}
