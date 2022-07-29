package com.example.splitwise.ui.fragment.settleup

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentSettleUpBinding
import com.example.splitwise.ui.fragment.adapter.PayerArrayAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog

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

                binding.choosePayeeCard.setOnClickListener {
                    openPayeesBottomSheet(payees)
                }
            }
        }

        // make payer unchangeable
        binding.payerNameText.isFocusable = false
        binding.payerNameText.isClickable = false

        // Amount
        viewModel.amount.observe(viewLifecycleOwner) { amount ->
            binding.amountTextView.text = (amount ?: 0f).toString()
        }

        binding.selectAllPayeesCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                viewModel.getAmount(viewModel.payerId, groupId = viewModel.groupId)
                binding.choosePayeeCard.isFocusable = false
                binding.choosePayeeCard.isClickable = false
            } else {
                if (viewModel.payeeId != null)
                    viewModel.getAmount(viewModel.payerId, viewModel.payeeId, viewModel.groupId)

                binding.choosePayeeCard.isFocusable = true
                binding.choosePayeeCard.isClickable = true
            }
        }

        binding.settleButton.setOnClickListener {
            if (binding.selectAllPayeesCheckbox.isChecked)
                viewModel.settle(
                    viewModel.payerId,
                    null,
                    viewModel.groupId
                ){
                    gotoSplitWiseFragment()
                }
            else if(viewModel.payeeId != null){
                viewModel.settle(
                    viewModel.payerId,
                    viewModel.payeeId,
                    viewModel.groupId
                ){
                    gotoSplitWiseFragment()
                }
            }
            else
                Toast.makeText(requireContext(), "Select Payee to proceed", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun openPayeesBottomSheet(payees: List<Member>) {
        val payeeBottomSheetDialog = BottomSheetDialog(requireContext())
        payeeBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val payeeTitle = payeeBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val payeeList = payeeBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)

        payeeTitle?.text = "Select Payee"
        //Adapter
        val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.dropdown, payees)
        payeeList?.apply {
            Log.d(ContentValues.TAG, "openPayerBottomSheet: list adapter set")
            adapter = payerAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    viewModel.payeeId = payees[position].memberId

                    viewModel.getAmount(
                                viewModel.payerId,
                                viewModel.payeeId,
                                viewModel.groupId
                            )
                    binding.choosePayeeText.text = payees[position].name
                    payeeBottomSheetDialog.dismiss()
                }
        }

        payeeBottomSheetDialog.show()
    }

    private fun gotoSplitWiseFragment(){
        val action = SettleUpFragmentDirections.actionSettleUpFragmentToSplitWiseFragment()
        view?.findNavController()?.navigate(action)
    }
}