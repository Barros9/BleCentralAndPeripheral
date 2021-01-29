package com.barros.blecentralperipheral.mainfragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.databinding.FragmentMainBinding
import com.barros.blecentralperipheral.utils.checkPermissionsAndOpenSettings

class MainFragment : Fragment() {

    private lateinit var mainViewModel: MainViewModel

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            mainViewModel.isEnabled.value = true
        } else {
            mainViewModel.isEnabled.value = false
            Toast.makeText(
                requireContext(),
                getString(R.string.location_permission_description),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private val requestBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            RESULT_OK -> mainViewModel.isEnabled.value = true
            RESULT_CANCELED -> {
                mainViewModel.isEnabled.value = false
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_permission_description),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val requestGPSLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        when (it.resultCode) {
            RESULT_OK -> mainViewModel.isEnabled.value = true
            RESULT_CANCELED -> {
                mainViewModel.isEnabled.value = false
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_permission_description),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        return FragmentMainBinding.inflate(inflater).apply {
            viewModel = mainViewModel
            lifecycleOwner = this@MainFragment

            centralAdvertising.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToCentralAdvertisingFragment())
            }

            peripheralAdvertising.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToPeripheralAdvertisingFragment())
            }

            centralConnect.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToCentralConnectFragment())
            }

            peripheralConnect.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToPeripheralConnectFragment())
            }

            mainViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })

            checkPermissionsAndOpenSettings(
                requestPermissionLauncher,
                requestBluetoothLauncher,
                requestGPSLauncher
            )
        }.root
    }
}
