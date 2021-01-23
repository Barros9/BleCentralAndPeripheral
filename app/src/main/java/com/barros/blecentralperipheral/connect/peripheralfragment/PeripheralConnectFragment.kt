package com.barros.blecentralperipheral.connect.peripheralfragment

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.REQUEST_ENABLE_BT
import com.barros.blecentralperipheral.REQUEST_FINE_LOCATION
import com.barros.blecentralperipheral.TAG
import com.barros.blecentralperipheral.databinding.FragmentPeripheralConnectBinding

class PeripheralConnectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val peripheralConnectViewModel = ViewModelProvider(this).get(PeripheralConnectViewModel::class.java)

        return FragmentPeripheralConnectBinding.inflate(inflater).apply {
            this.viewModel = peripheralConnectViewModel
            this.lifecycleOwner = this@PeripheralConnectFragment

            ArrayAdapter.createFromResource(
                    this@PeripheralConnectFragment.requireContext(),
                    R.array.spinner_array,
                    android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                    when (position) {
                        0 -> {
                            cardRead.visibility = View.VISIBLE
                            cardSent.visibility = View.VISIBLE
                            cardNotify.visibility = View.GONE
                            cardWrite.visibility = View.GONE
                        }
                        1 -> {
                            cardRead.visibility = View.GONE
                            cardSent.visibility = View.GONE
                            cardNotify.visibility = View.VISIBLE
                            cardWrite.visibility = View.GONE
                        }
                        2 -> {
                            cardRead.visibility = View.GONE
                            cardSent.visibility = View.GONE
                            cardNotify.visibility = View.GONE
                            cardWrite.visibility = View.VISIBLE
                        }
                    }
                    peripheralConnectViewModel.mode.value = Mode.values()[position]
                }
                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }

            sendingValue.doOnTextChanged { text, _, _, _ ->
                peripheralConnectViewModel.sendingValue.value = text.toString()
                peripheralConnectViewModel.readMode()
            }

            peripheralConnectViewModel.requestBluetooth.observe(viewLifecycleOwner, {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
            })

            peripheralConnectViewModel.requestLocation.observe(viewLifecycleOwner, {
                ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_FINE_LOCATION
                )
            })

            peripheralConnectViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
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
