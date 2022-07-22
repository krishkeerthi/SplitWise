package com.example.splitwise.ui.fragment.bill

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentBillBinding

class BillFragment : Fragment() {
    private lateinit var binding: FragmentBillBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bill, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBillBinding.bind(view)

        arguments?.takeIf { it.containsKey("ARG_URI") }?.apply {
            val uri = Uri.parse(getString("ARG_URI"))
            binding.billImageView.setImageURI(uri)
        }
    }
}