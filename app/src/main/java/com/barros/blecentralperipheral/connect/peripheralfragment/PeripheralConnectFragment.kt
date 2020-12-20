package com.barros.blecentralperipheral.connect.peripheralfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.barros.blecentralperipheral.databinding.FragmentPeripheralConnectBinding

class PeripheralConnectFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(PeripheralConnectViewModel::class.java)

        return FragmentPeripheralConnectBinding.inflate(inflater).apply {
            this.viewModel = viewModel
            this.lifecycleOwner = this@PeripheralConnectFragment
        }.root
    }
}
