package com.example.splitwise.ui.fragment.groupsettleup

import android.content.ContentValues
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentGroupSettleUpBinding
import com.example.splitwise.databinding.FragmentSettleUpBinding
import com.example.splitwise.ui.fragment.adapter.ChoosePayeeAdapter
import com.example.splitwise.ui.fragment.settleup.SettleUpFragmentArgs
import com.example.splitwise.ui.fragment.settleup.SettleUpFragmentDirections
import com.example.splitwise.ui.fragment.settleup.SettleUpViewModel
import com.example.splitwise.ui.fragment.settleup.SettleUpViewModelFactory
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap
import com.example.splitwise.util.roundOff
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis

class GroupSettleUpFragment : Fragment() {

    private lateinit var binding: FragmentGroupSettleUpBinding
    private val args: GroupSettleUpFragmentArgs by navArgs()

    private val viewModel: GroupSettleUpViewModel by viewModels {
        GroupSettleUpViewModelFactory(requireContext(), args.payerId, args.groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor =
                resources.getColor(R.color.background)//Color.TRANSPARENT //R.color.view_color
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
        return inflater.inflate(R.layout.fragment_group_settle_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupSettleUpBinding.bind(view)

        val payeesAndAmountsAdapter = ChoosePayeeAdapter { member, isChecked ->
            if (isChecked) {
                viewModel.addPayeeToSelected(member)
            } else {
                viewModel.removePayeeFromSelected(member)
            }
        }

        // payees
        binding.payeesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payeesAndAmountsAdapter
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
                            getRoundedCroppedBitmap(
                            decodeSampledBitmapFromUri(
                                binding.root.context,
                                it.memberProfile,
                                48.dpToPx(resources.displayMetrics),
                                48.dpToPx(resources.displayMetrics)
                            )!!
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
        viewModel.payeesAndAmounts.observe(viewLifecycleOwner) { payeesAndAmount ->
            if (payeesAndAmount != null) {
                payeesAndAmountsAdapter.updatePayees(payeesAndAmount, viewModel.selectedPayeesIds())
            }
        }

        viewModel.selectedPayeesCount.observe(viewLifecycleOwner) { count ->
            if (count != null && count > 0) {
                Log.d(ContentValues.TAG, "onViewCreated: count ${viewModel.selectedPayeesIds()}")
                viewModel.getAmount(viewModel.selectedPayeesIds())

                //binding.clearPayees.visibility = View.VISIBLE
                binding.clearPayees.isEnabled = true

                binding.totalTextView.visibility = View.VISIBLE
                binding.amountTextView.visibility = View.VISIBLE
                binding.settleButton.visibility = View.VISIBLE

                binding.selectAllPayees.isEnabled = count != viewModel.payeesAndAmounts.value!!.size

            } else {
                ///binding.selectAllPayees.visibility = View.VISIBLE
                binding.selectAllPayees.isEnabled = true
                //binding.clearPayees.visibility = View.GONE
                binding.clearPayees.isEnabled = false
                binding.totalTextView.visibility = View.GONE
                binding.amountTextView.visibility = View.GONE
                binding.settleButton.visibility = View.INVISIBLE
            }
        }
        // clear button
        binding.clearPayees.setOnClickListener {
            gotoSelf()
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

        binding.settleButton.setOnClickListener {
            Log.d(ContentValues.TAG, "onViewCreated: selected ${viewModel.selectedPayeesIds()}")
            viewModel.settle(viewModel.selectedPayeesIds()) {
                Snackbar.make(
                    binding.root,
                    "${getString(R.string.settled_successfully)}",
                    Snackbar.LENGTH_SHORT
                ).show()

                gotoGroupSplitWiseFragment()
            }
        }

    }

    private fun gotoGroupSplitWiseFragment() {
        val action =
            GroupSettleUpFragmentDirections.actionGroupSettleUpFragmentToGroupSplitwiseFragment(args.groupId)
        findNavController().navigate(action)
    }

    private fun gotoSelf() {
        val action =
            GroupSettleUpFragmentDirections.actionGroupSettleUpFragmentSelf(
                args.groupId,
                args.payerId,
            )
       findNavController().navigate(action)
    }

}