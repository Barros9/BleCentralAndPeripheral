package com.barros.blecentralperipheral.connect.informationfragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.REQUEST_ENABLE_BT
import com.barros.blecentralperipheral.REQUEST_FINE_LOCATION
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.devicefragment.DeviceFragmentArgs
import com.barros.blecentralperipheral.connect.model.BleItem
import com.barros.blecentralperipheral.connect.utils.InformationAdapter
import com.barros.blecentralperipheral.databinding.FragmentInformationBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InformationFragment : BottomSheetDialogFragment() {

    private lateinit var bleItem: BleItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            bleItem = DeviceFragmentArgs.fromBundle(it).bleItem
        }

        val characteristicViewModelFactory = InformationViewModelFactory(requireContext(), bleItem)
        val characteristicViewModel = ViewModelProvider(this, characteristicViewModelFactory).get(InformationViewModel::class.java)

        return FragmentInformationBinding.inflate(inflater).apply {
            viewModel = characteristicViewModel
            lifecycleOwner = this@InformationFragment

            characteristicViewModel.requestBluetooth.observe(viewLifecycleOwner, {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
            })

            characteristicViewModel.requestLocation.observe(viewLifecycleOwner, {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION
                )
            })

            characteristicViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })

            itemList.adapter = InformationAdapter()
        }.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_FINE_LOCATION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "ACCESS_FINE_LOCATION Permission Denied")
                }
            }
        }
    }
}
