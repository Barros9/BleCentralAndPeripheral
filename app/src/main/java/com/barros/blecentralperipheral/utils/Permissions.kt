package com.barros.blecentralperipheral.utils

import android.Manifest
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.barros.blecentralperipheral.R

fun Fragment.checkPermissionsAndOpenSettings(
    requestLocationLauncher: ActivityResultLauncher<String>,
    requestBluetoothLauncher: ActivityResultLauncher<Intent>,
    requestGPSLauncher: ActivityResultLauncher<Intent>,
    permissionGranted: () -> Unit = {}
) {
    checkBleFunctionality(
        success = {
            checkLocationPermission(
                requestLocationLauncher = requestLocationLauncher,
                locationGranted = {
                    checkBluetoothPermission(
                        requestBluetoothLauncher = requestBluetoothLauncher,
                        bluetoothEnabled = {
                            checkGPSPermission(
                                requestGPSLauncher = requestGPSLauncher,
                                gpsEnabled = {
                                    permissionGranted.invoke()
                                }
                            )
                        }
                    )
                }
            )
        }
    )
}

fun checkPermissionGranted(
    context: Context
): String {
    return when {
        PermissionChecker.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED ->
            context.getString(R.string.location_permission_not_granted)
        !BluetoothAdapter.getDefaultAdapter().isEnabled ->
            context.getString(R.string.bluetooth_not_enabled)
        !((context.getSystemService(Context.LOCATION_SERVICE)) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER) ->
            context.getString(R.string.gps_not_enabled)
        else -> PERMISSION_GRANTED
    }
}

fun Fragment.checkBleFunctionality(
    success: () -> Unit
) {
    val bluetoothAdapter = (requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    val packageManager = requireContext().packageManager

    when {
        !packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) ->
            Toast.makeText(requireContext(), getString(R.string.no_ble_feature), Toast.LENGTH_SHORT).show()
        !bluetoothAdapter.isMultipleAdvertisementSupported ->
            Toast.makeText(requireContext(), getString(R.string.no_multiple_advertisement), Toast.LENGTH_SHORT).show()
        else ->
            success.invoke()
    }
}

fun Fragment.checkLocationPermission(
    requestLocationLauncher: ActivityResultLauncher<String>,
    locationGranted: () -> Unit
) {
    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) -> {
            locationGranted.invoke()
        }
        else -> {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.location_permission))
                .setMessage(getString(R.string.location_permission_description))
                .setPositiveButton(R.string.ok) { _, _ ->
                    requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
                .setNegativeButton(R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }
}

fun Fragment.checkBluetoothPermission(
    requestBluetoothLauncher: ActivityResultLauncher<Intent>,
    bluetoothEnabled: () -> Unit
) {
    if (BluetoothAdapter.getDefaultAdapter().isEnabled) {
        bluetoothEnabled.invoke()
    } else {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.bluetooth_permission))
            .setMessage(getString(R.string.bluetooth_permission_description))
            .setPositiveButton(R.string.ok) { _, _ ->
                requestBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }
            .setNegativeButton(R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}

fun Fragment.checkGPSPermission(
    requestGPSLauncher: ActivityResultLauncher<Intent>,
    gpsEnabled: () -> Unit
) {
    val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        gpsEnabled.invoke()
    } else {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.gps_permission))
            .setMessage(getString(R.string.gps_permission_description))
            .setPositiveButton(R.string.ok) { _, _ ->
                requestGPSLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}
