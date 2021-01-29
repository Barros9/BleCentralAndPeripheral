package com.barros.blecentralperipheral.connect.centralfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.barros.blecentralperipheral.databinding.FragmentCentralConnectBinding
import com.barros.blecentralperipheral.utils.adapter.BleItemAdapter

class CentralConnectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val centralConnectViewModel = ViewModelProvider(this).get(CentralConnectViewModel::class.java)

        return FragmentCentralConnectBinding.inflate(inflater).apply {
            viewModel = centralConnectViewModel
            lifecycleOwner = this@CentralConnectFragment

            val adapter = BleItemAdapter(
                BleItemAdapter.OnClickListener(
                    clickInformationListener = {
                        findNavController().navigate(CentralConnectFragmentDirections.actionCentralConnectFragmentToCharacteristicFragment(it))
                    },
                    clickConnectListener = {
                        findNavController().navigate(CentralConnectFragmentDirections.actionCentralConnectFragmentToDeviceFragment(it))
                    }
                )
            )

            centralConnectViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })

            itemList.adapter = adapter
        }.root
    }
}
