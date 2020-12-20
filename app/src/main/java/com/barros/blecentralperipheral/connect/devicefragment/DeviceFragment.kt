package com.barros.blecentralperipheral.connect.devicefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.connect.model.BleItem
import com.barros.blecentralperipheral.databinding.FragmentDeviceBinding

class DeviceFragment : Fragment() {

    private lateinit var bleItem: BleItem

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.let {
            bleItem = DeviceFragmentArgs.fromBundle(it).bleItem
        }

        val deviceViewModelFactory = DeviceViewModelFactory(requireContext(), bleItem)
        val deviceViewModel = ViewModelProvider(this, deviceViewModelFactory).get(DeviceViewModel::class.java)

        return FragmentDeviceBinding.inflate(inflater).apply {
            viewModel = deviceViewModel
            lifecycleOwner = this@DeviceFragment
        }.root
    }
}
