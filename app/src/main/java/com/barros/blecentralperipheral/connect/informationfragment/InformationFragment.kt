package com.barros.blecentralperipheral.connect.informationfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.connect.devicefragment.DeviceFragmentArgs
import com.barros.blecentralperipheral.databinding.FragmentInformationBinding
import com.barros.blecentralperipheral.utils.adapter.InformationAdapter
import com.barros.blecentralperipheral.utils.model.BleItem
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

            characteristicViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })

            itemList.adapter = InformationAdapter()
        }.root
    }
}
