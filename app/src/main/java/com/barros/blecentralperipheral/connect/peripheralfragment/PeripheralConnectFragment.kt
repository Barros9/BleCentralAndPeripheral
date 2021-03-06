package com.barros.blecentralperipheral.connect.peripheralfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.R
import com.barros.blecentralperipheral.databinding.FragmentPeripheralConnectBinding
import com.barros.blecentralperipheral.utils.model.Mode

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
                    peripheralConnectViewModel.changeMode()
                }
                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }

            sendingValue.doOnTextChanged { text, _, _, _ ->
                peripheralConnectViewModel.sendingValue.value = text.toString()
                peripheralConnectViewModel.readMode()
            }

            peripheralConnectViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        }.root
    }
}
