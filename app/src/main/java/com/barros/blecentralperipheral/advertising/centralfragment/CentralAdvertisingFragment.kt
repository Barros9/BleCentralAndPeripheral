package com.barros.blecentralperipheral.advertising.centralfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.databinding.FragmentCentralAdvertisingBinding

class CentralAdvertisingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val centralAdvertisingViewModel = ViewModelProvider(this).get(CentralAdvertisingViewModel::class.java)

        return FragmentCentralAdvertisingBinding.inflate(inflater).apply {
            viewModel = centralAdvertisingViewModel
            lifecycleOwner = this@CentralAdvertisingFragment

            centralAdvertisingViewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        }.root
    }
}
