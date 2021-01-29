package com.barros.blecentralperipheral.advertising.peripheralfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.databinding.FragmentPeripheralAdvertisingBinding

class PeripheralAdvertisingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val peripheralAdvertisingViewModel = ViewModelProvider(this).get(PeripheralAdvertisingViewModel::class.java)

        return FragmentPeripheralAdvertisingBinding.inflate(inflater).apply {
            viewModel = peripheralAdvertisingViewModel
            lifecycleOwner = this@PeripheralAdvertisingFragment

            sendingValue.doOnTextChanged { text, _, _, _ ->
                peripheralAdvertisingViewModel.sendingValue.value = text.toString()
                peripheralAdvertisingViewModel.updateSentValue()
            }

            peripheralAdvertisingViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        }.root
    }
}
