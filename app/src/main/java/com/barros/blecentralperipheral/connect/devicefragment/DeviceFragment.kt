package com.barros.blecentralperipheral.connect.devicefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.connect.model.BleItem
import com.barros.blecentralperipheral.connect.peripheralfragment.Mode
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

            ArrayAdapter.createFromResource(
                    this@DeviceFragment.requireContext(),
                    R.array.spinner_array,
                    android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
            }

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View, position: Int, id: Long) {
                    when (position) {
                        0 -> {
                            cardRead.visibility = View.VISIBLE
                            cardNotify.visibility = View.GONE
                            cardWrite.visibility = View.GONE
                            cardSent.visibility = View.GONE
                        }
                        1 -> {
                            cardRead.visibility = View.GONE
                            cardNotify.visibility = View.VISIBLE
                            cardWrite.visibility = View.GONE
                            cardSent.visibility = View.GONE
                        }
                        2 -> {
                            cardRead.visibility = View.GONE
                            cardNotify.visibility = View.GONE
                            cardWrite.visibility = View.VISIBLE
                            cardSent.visibility = View.VISIBLE
                        }
                    }
                    deviceViewModel.mode.value = Mode.values()[position]
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }

            sendingValue.doOnTextChanged { text, _, _, _ ->
                deviceViewModel.sendingValue.value = text.toString()
            }
        }.root
    }
}
