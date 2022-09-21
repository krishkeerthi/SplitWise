package com.example.splitwise.ui.fragment.settleup

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentSettleUpBinding
import com.example.splitwise.model.MemberAndAmount
import com.example.splitwise.ui.fragment.adapter.ChoosePayeeAdapter
import com.example.splitwise.ui.fragment.adapter.GroupMembersAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.splitwise.SplitWiseFragmentDirections
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.roundOff
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform

class SettleUpFragment : Fragment() {

    private lateinit var binding: FragmentSettleUpBinding
    private val args: SettleUpFragmentArgs by navArgs()

    private var selectedMembers = listOf<Member>()
    private val viewModel: SettleUpViewModel by viewModels {
        SettleUpViewModelFactory(requireContext(), args.payerId, args.selectedGroups.toList())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.view_color)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.settle_up)
        return inflater.inflate(R.layout.fragment_settle_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Log.d(TAG, "onViewCreated: selected ${args.selectedGroups.toList()}")
        binding = FragmentSettleUpBinding.bind(view)

        // toolbar title
        requireActivity().title = "Settle Up"

        //viewModel.fetchData() // has to be called otherwise latest data is not fetched sometimes, yes it is inefficient

        // Load data based on selected members
        selectedMembers = args.selectedMembers.toList()

        //val payeesAdapter = GroupMembersAdapter() // reusing the adapter here

        val payeesAndAmountsAdapter = ChoosePayeeAdapter { member, isChecked ->
            if (isChecked) {
                viewModel.addPayeeToSelected(member)
            } else {
                viewModel.removePayeeFromSelected(member)
            }
        }

//        if(selectedMembers.isNotEmpty()){
////            var payeesText = ""
////            for(member in selectedMembers){
////                payeesText += "● ${member.name} "
////            }
////            binding.selectedPayeesText.text = payeesText
////            binding.selectedPayeesCard.visibility = View.VISIBLE
//
//            payeesAdapter.updateMembers(selectedMembers)
//            viewModel.getAmount(getMemberIds(selectedMembers))
//
//            binding.clearPayees.visibility = View.VISIBLE
//            binding.payeesRecyclerView.visibility = View.VISIBLE
//            binding.totalTextView.visibility = View.VISIBLE
//            binding.amountTextView.visibility = View.VISIBLE
//            binding.settleButton.visibility = View.VISIBLE
//
//            binding.emptyPayees.visibility = View.GONE
//        }
//        else{
//            binding.clearPayees.visibility = View.INVISIBLE
//            binding.payeesRecyclerView.visibility = View.GONE
//            binding.totalTextView.visibility = View.GONE
//            binding.amountTextView.visibility = View.GONE
//            binding.settleButton.visibility = View.INVISIBLE
//
//            binding.emptyPayees.visibility = View.VISIBLE
//        }


        // payees
        binding.payeesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payeesAndAmountsAdapter
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

                if (it.memberProfile != null) {
                    ///binding.fromMemberImageView.setImageURI(it.memberProfile)

                    Handler(Looper.getMainLooper()).postDelayed({

                        binding.fromMemberImageView.setImageBitmap(
                            decodeSampledBitmapFromUri(
                                binding.root.context,
                                it.memberProfile,
                                48.dpToPx(resources.displayMetrics),
                                48.dpToPx(resources.displayMetrics)
                            )
                        )
                    }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())


                    binding.fromMemberImageView.visibility = View.VISIBLE
                    binding.fromMemberImageHolder.visibility = View.INVISIBLE
                    binding.fromMemberImageHolderImage.visibility = View.INVISIBLE
                }
            }
        }

        // Payees
//        viewModel.payees.observe(viewLifecycleOwner) { payees ->
//            if (payees != null) {
//                binding.choosePayeeButton.setOnClickListener {
//                    gotoChoosePayeeFragment(payees)
//                    //openPayeesBottomSheet(payees)
//                }
//            }
//        }


        // Payees
        viewModel.payeesAndAmounts.observe(viewLifecycleOwner) { payeesAndAmount ->
            if (payeesAndAmount != null) {

                payeesAndAmountsAdapter.updatePayees(payeesAndAmount, viewModel.selectedPayeesIds())
                // previously choose button was there
//                binding.choosePayeeButton.setOnClickListener {
//                    Log.d(TAG, "onViewCreated: payeesandamount ${payeesAndAmount.size}")
//                    gotoChoosePayeeFragment(payeesAndAmount)
//                    //openPayeesBottomSheet(payees)
//                }
            }
        }

        viewModel.selectedPayeesCount.observe(viewLifecycleOwner) { count ->
            if (count != null && count > 0) {
                Log.d(TAG, "onViewCreated: count ${viewModel.selectedPayeesIds()}")
                viewModel.getAmount(viewModel.selectedPayeesIds())

                binding.clearPayees.visibility = View.VISIBLE

                binding.totalTextView.visibility = View.VISIBLE
                binding.amountTextView.visibility = View.VISIBLE
                binding.settleButton.visibility = View.VISIBLE

                if (count == viewModel.payeesAndAmounts.value!!.size)
                    binding.selectAllPayees.visibility = View.GONE
                else
                    binding.selectAllPayees.visibility = View.VISIBLE

            } else {
                binding.selectAllPayees.visibility = View.VISIBLE
                binding.clearPayees.visibility = View.GONE
                binding.totalTextView.visibility = View.GONE
                binding.amountTextView.visibility = View.GONE
                binding.settleButton.visibility = View.INVISIBLE
            }
        }
        // clear button
        binding.clearPayees.setOnClickListener {
            gotoSelf()
//            viewModel.clearPayees()
//            payeesAndAmountsAdapter.updatePayees(viewModel.payeesAndAmounts.value!!, viewModel.selectedPayeesIds())
            // Not the best code
            Log.d(TAG, "onViewCreated: cleared count")
        }

        // Select All
        binding.selectAllPayees.setOnClickListener {
            payeesAndAmountsAdapter.selectAllPayees()
            binding.totalTextView.visibility = View.VISIBLE
            binding.amountTextView.visibility = View.VISIBLE
            //viewModel.setSelectedPayeesCount()
        }

        // Amount
        viewModel.amount.observe(viewLifecycleOwner) { amount ->
            if (amount != null) {

                val total = "₹" + amount.roundOff()
                binding.amountTextView.text = total
            }

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
            Log.d(TAG, "onViewCreated: selected ${viewModel.selectedPayeesIds()}")
            viewModel.settle(viewModel.selectedPayeesIds()) {
                Snackbar.make(
                    binding.root,
                    "${getString(R.string.settled_successfully)}",
                    Snackbar.LENGTH_SHORT
                ).show()

                gotoSplitWiseFragment()
            }
        }

//        binding.settleButton.setOnClickListener {
//            viewModel.settle(getMemberIds(selectedMembers)){
//                gotoSplitWiseFragment()
//            }
//        }

//        binding.settleButton.setOnClickListener {
//            if(selectedMembers.isNotEmpty())
//                viewModel.settle(getMemberIds(selectedMembers)){
//                    gotoSplitWiseFragment()
//                }
//            else
//                Snackbar.make(binding.root, getString(R.string.select_payees), Snackbar.LENGTH_SHORT).show()
//        }

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
        for (member in members) {
            memberIds.add(member.memberId)
        }
        return memberIds.toList()
    }

    private fun gotoChoosePayeeFragment(payeesAndAmounts: List<MemberAndAmount>) {
        val action = SettleUpFragmentDirections.actionSettleUpFragmentToChoosePayeesFragment(
            viewModel.payerId,
            viewModel.groupIds().toIntArray(),
            getMemberIds(selectedMembers).toIntArray(),
            payeesAndAmounts.toTypedArray(),
        )

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
        val action =
            SettleUpFragmentDirections.actionSettleUpFragmentToSplitWiseFragment(args.selectedGroups)
        view?.findNavController()?.navigate(action)
    }

    private fun gotoSelf() {
        val action =
            SettleUpFragmentDirections.actionSettleUpFragmentSelf(
                args.payerId,
                listOf<Member>().toTypedArray(),
                args.selectedGroups
            ) // Passing empty list of members when going to settle up fragment
        view?.findNavController()?.navigate(action)
    }

}