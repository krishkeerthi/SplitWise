package com.example.splitwise.ui.fragment.settleup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentSettleUpBinding
import com.example.splitwise.ui.fragment.adapter.GroupMembersAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.splitwise.SplitWiseFragmentDirections
import com.example.splitwise.util.roundOff

class SettleUpFragment : Fragment() {

    private lateinit var binding: FragmentSettleUpBinding
    private val args: SettleUpFragmentArgs by navArgs()

    private var selectedMembers = listOf<Member>()
    private val viewModel: SettleUpViewModel by viewModels {
        SettleUpViewModelFactory(requireContext(), args.payerId, args.groupIds.toList())
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

        // toolbar title
        requireActivity().title = "Settle Up"

        viewModel.fetchData()

        // Load data based on selected members
        selectedMembers = args.selectedMembers.toList()

        val payeesAdapter = GroupMembersAdapter() // reusing the adapter here

        if(selectedMembers.isNotEmpty()){
//            var payeesText = ""
//            for(member in selectedMembers){
//                payeesText += "● ${member.name} "
//            }
//            binding.selectedPayeesText.text = payeesText
//            binding.selectedPayeesCard.visibility = View.VISIBLE

            payeesAdapter.updateMembers(selectedMembers)
            viewModel.getAmount(getMemberIds(selectedMembers))

            binding.clearPayees.visibility = View.VISIBLE
            binding.payeesRecyclerView.visibility = View.VISIBLE
            binding.totalTextView.visibility = View.VISIBLE
            binding.amountTextView.visibility = View.VISIBLE
            binding.settleButton.visibility = View.VISIBLE

            binding.emptyPayees.visibility = View.GONE
        }
        else{
            binding.clearPayees.visibility = View.INVISIBLE
            binding.payeesRecyclerView.visibility = View.GONE
            binding.totalTextView.visibility = View.GONE
            binding.amountTextView.visibility = View.GONE
            binding.settleButton.visibility = View.INVISIBLE

            binding.emptyPayees.visibility = View.VISIBLE
        }

        // payees
        binding.payeesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payeesAdapter
        }

        // Group
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            //binding.groupNameTextView.text = group?.groupName //?: "All Group"
//            if(groups != null) {
//                var groupsText = ""
//                for (group in groups) {
//                    groupsText += "● ${group.groupName} "
//                }
//                binding.totalTextView.text = groupsText
//                binding.totalTextView.visibility = View.VISIBLE
//            }
        }

        // Payer
        viewModel.payer.observe(viewLifecycleOwner) { payer ->
            payer?.let {
                binding.fromMemberNameTextView.text = it.name
                binding.fromMemberPhoneTextView.text = it.phone.toString()
            }
        }

        // Payees
        viewModel.payees.observe(viewLifecycleOwner) { payees ->
            if (payees != null) {
                binding.choosePayeeButton.setOnClickListener {
                    gotoChoosePayeeFragment(payees)
                    //openPayeesBottomSheet(payees)
                }
            }
        }

        // clear button
        binding.clearPayees.setOnClickListener {
            gotoSelf()
        }

        // Amount
        viewModel.amount.observe(viewLifecycleOwner) { amount ->
            binding.amountTextView.text = "₹" + (amount?.roundOff() ?: "")
        }

//        binding.selectAllPayeesCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                viewModel.getAmount(viewModel.payerId, groupId = viewModel.groupId)
//            } else {
//                if (viewModel.payeeId != null)
//                    viewModel.getAmount(viewModel.payerId, viewModel.payeeId, viewModel.groupId)
//            }
//        }

        binding.settleButton.setOnClickListener {
            if(selectedMembers.isNotEmpty())
            viewModel.settle(getMemberIds(selectedMembers)){
                gotoSplitWiseFragment()
            }
            else
                Toast.makeText(requireContext(), getString(R.string.select_payees), Toast.LENGTH_SHORT).show()
        }

//        binding.settleButton.setOnClickListener {
//            if (binding.selectAllPayeesCheckbox.isChecked)
//                viewModel.settle(
//                    viewModel.payerId,
//                    null,
//                    viewModel.groupId
//                ) {
//                    gotoSplitWiseFragment()
//                }
//            else if (viewModel.payeeId != null) {
//                viewModel.settle(
//                    viewModel.payerId,
//                    viewModel.payeeId,
//                    viewModel.groupId
//                ) {
//                    gotoSplitWiseFragment()
//                }
//            } else
//                Toast.makeText(requireContext(), "Select Payee to proceed", Toast.LENGTH_SHORT)
//                    .show()
//        }
    }

    private fun getMemberIds(members: List<Member>): List<Int> {
        var memberIds = mutableListOf<Int>()
        for(member in members){
            memberIds.add(member.memberId)
        }
        return memberIds.toList()
    }

    private fun gotoChoosePayeeFragment(payees: List<Member>){
        val action = SettleUpFragmentDirections.actionSettleUpFragmentToChoosePayeesFragment(
            payees.toTypedArray(), viewModel.payerId, viewModel.groupIds().toIntArray(), getMemberIds(selectedMembers).toIntArray())

        view?.findNavController()?.navigate(action)
    }

//    private fun openPayeesBottomSheet(payees: List<Member>) {
//        val payeeBottomSheetDialog = BottomSheetDialog(requireContext())
//        payeeBottomSheetDialog.setContentView(R.layout.bottom_sheet)
//
//        val payeeTitle = payeeBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
//        val payeeList = payeeBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)
//
//        payeeTitle?.text = getString(R.string.select_payee)
//        //Adapter
//        val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.dropdown, payees)
//        payeeList?.apply {
//            Log.d(ContentValues.TAG, "openPayerBottomSheet: list adapter set")
//            adapter = payerAdapter
//            onItemClickListener =
//                AdapterView.OnItemClickListener { parent, view, position, id ->
//                    viewModel.payeeId = payees[position].memberId
//
//                    // Make payee visible
//                    binding.toMemberLayout.visibility = View.VISIBLE
//                    binding.totalTextView.visibility = View.VISIBLE
//
//                    viewModel.getAmount(
//                        viewModel.payerId,
//                        viewModel.payeeId,
//                        viewModel.groupId
//                    )
//                    binding.toMemberNameTextView.text = payees[position].name
//                    binding.toMemberPhoneTextView.text = payees[position].phone.toString()
//
//                    payeeBottomSheetDialog.dismiss()
//                }
//        }
//
//        payeeBottomSheetDialog.show()
//    }

    private fun gotoSplitWiseFragment() {
        val action = SettleUpFragmentDirections.actionSettleUpFragmentToSplitWiseFragment(listOf<Group>().toTypedArray())
        view?.findNavController()?.navigate(action)
    }

    private fun gotoSelf() {
        val action =
            SettleUpFragmentDirections.actionSettleUpFragmentSelf(args.payerId, args.groupIds, listOf<Member>().toTypedArray()) // Passing empty list of members when going to settle up fragment
        view?.findNavController()?.navigate(action)
    }

}