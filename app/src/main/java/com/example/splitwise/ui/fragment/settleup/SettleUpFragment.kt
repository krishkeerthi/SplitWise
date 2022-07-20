package com.example.splitwise.ui.fragment.settleup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSettleUpBinding
import com.example.splitwise.ui.fragment.adapter.PayeeArrayAdapter

class SettleUpFragment : Fragment() {

    private lateinit var binding: FragmentSettleUpBinding
    private val args: SettleUpFragmentArgs by navArgs()

    private val viewModel: SettleUpViewModel by viewModels {
        SettleUpViewModelFactory(requireContext(), args.payerId, args.groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settle_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSettleUpBinding.bind(view)


        // Group
        viewModel.group.observe(viewLifecycleOwner) { group ->

            binding.groupNameTextView.text = group?.groupName ?: "All Group"

        }

        // Payer
        viewModel.payer.observe(viewLifecycleOwner) { payer ->
            payer?.let {
                binding.payerNameText.setText(it.name)
            }
        }

        // Payees
        viewModel.payees.observe(viewLifecycleOwner) { payees ->
            if (payees != null) {
                binding.payeeSpinner.apply {
                    adapter = PayeeArrayAdapter(requireContext(), R.layout.dropdown, payees)

                    onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.payeeId = payees[position].memberId

                            viewModel.getAmount(
                                viewModel.payerId,
                                viewModel.payeeId,
                                viewModel.groupId
                            )
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                    }
                }
            }
        }

        // Amount
        viewModel.amount.observe(viewLifecycleOwner) { amount ->
            binding.totalAmountTextView.text = (amount ?: 0f).toString()
        }

        binding.selectAllPayeesCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.getAmount(viewModel.payerId, groupId = viewModel.groupId)
                binding.payeeSpinner.isEnabled = false
                binding.payeeSpinner.isClickable = false
            } else {
                if (viewModel.payeeId != null)
                    viewModel.getAmount(viewModel.payerId, viewModel.payeeId, viewModel.groupId)

                binding.payeeSpinner.isEnabled = true
                binding.payeeSpinner.isClickable = true
            }
        }

        binding.settleButton.setOnClickListener {
            if (binding.selectAllPayeesCheckbox.isChecked || (viewModel.payeeId != null))
                viewModel.settle(
                    viewModel.payerId,
                    viewModel.payeeId,
                    viewModel.groupId
                )
            else
                Toast.makeText(requireContext(), "Select Payee to proceed", Toast.LENGTH_SHORT)
                    .show()
        }
    }
}