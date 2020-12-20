package com.barros.blecentralperipheral.connect.centralfragment

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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.barros.blecentralperipheral.REQUEST_ENABLE_BT
import com.barros.blecentralperipheral.REQUEST_FINE_LOCATION
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.connect.utils.BleItemAdapter
import com.barros.blecentralperipheral.databinding.FragmentCentralConnectBinding

class CentralConnectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(CentralConnectViewModel::class.java)

        return FragmentCentralConnectBinding.inflate(inflater).apply {
            this.viewModel = viewModel
            this.lifecycleOwner = this@CentralConnectFragment

            val adapter = BleItemAdapter(
                BleItemAdapter.OnClickListener(
                    clickCharacteristicListener = {
                        findNavController().navigate(CentralConnectFragmentDirections.actionCentralConnectFragmentToCharacteristicFragment(it))
                    },
                    clickConnectListener = {
                        findNavController().navigate(CentralConnectFragmentDirections.actionCentralConnectFragmentToDeviceFragment(it))
                    }
                )
            )

            viewModel.requestBluetooth.observe(viewLifecycleOwner, {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
            })

            viewModel.requestLocation.observe(viewLifecycleOwner, {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FINE_LOCATION
                )
            })

            viewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })

            itemList.adapter = adapter
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