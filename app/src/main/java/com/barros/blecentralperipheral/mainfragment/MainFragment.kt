package com.barros.blecentralperipheral.mainfragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.barros.blecentralperipheral.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        return FragmentMainBinding.inflate(inflater).apply {
            this.viewModel = viewModel
            this.lifecycleOwner = this@MainFragment

            central.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToCentralFragment())
            }

            peripheral.setOnClickListener {
                findNavController().navigate(MainFragmentDirections.actionMainFragmentToPeripheralFragment())
            }

            viewModel.showToast.observe(viewLifecycleOwner, { message ->
                if (message.isNotBlank()) {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            })
        }.root
    }
}
