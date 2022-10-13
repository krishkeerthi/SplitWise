package com.example.splitwise.ui.fragment.groupsplitwise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentGroupSplitwiseBinding
import com.example.splitwise.ui.fragment.adapter.SplitWiseAdapter
import com.example.splitwise.ui.fragment.splitwise.SplitWiseFragmentDirections
import com.example.splitwise.util.ripple
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

class GroupSplitwiseFragment : Fragment() {
    private lateinit var binding: FragmentGroupSplitwiseBinding
    private val args: GroupSplitwiseFragmentArgs by navArgs()
    private val viewModel: GroupSplitWiseViewModel by viewModels {
        GroupSplitWiseViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.splitwise)
        return inflater.inflate(R.layout.fragment_group_splitwise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //


        binding = FragmentGroupSplitwiseBinding.bind(view)

        // Rv
        val splitWiseAdapter =
            SplitWiseAdapter { payerId: Int, amountOwed: Float, name: String, paymentStatView: View ->
                if (amountOwed == 0f)
                    Snackbar.make(
                        binding.root,
                        "$name ${getString(R.string.owes_nothing)}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                else {
                    paymentStatView.ripple(paymentStatView.context)
                    gotoGroupSettleUpFragment(payerId, paymentStatView)
                }

            }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = splitWiseAdapter
        }

        viewModel.membersPaymentStatsDetail.observe(viewLifecycleOwner) { membersPaymentStatsDetail ->
            if (membersPaymentStatsDetail != null && membersPaymentStatsDetail.isNotEmpty()) {
                splitWiseAdapter.updateMembersPaymentStatsDetail(membersPaymentStatsDetail)

                binding.membersRecyclerView.visibility = View.VISIBLE
                binding.noPendingImageView.visibility = View.GONE
                binding.noPendingTextView.visibility = View.GONE
                binding.directSettleUpButton.visibility = View.VISIBLE
            } else {
                binding.membersRecyclerView.visibility = View.GONE
                binding.noPendingImageView.visibility = View.VISIBLE
                binding.noPendingTextView.visibility = View.VISIBLE
                binding.directSettleUpButton.visibility = View.GONE
            }
        }

        binding.directSettleUpButton.setOnClickListener{
            gotoGroupDirectSettleUpFragment()
        }
    }

    private fun gotoGroupSettleUpFragment(payerId: Int, paymentStatView: View) {

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val transitionName = getString(R.string.group_settle_up_transition_name)
        val extras = FragmentNavigatorExtras(paymentStatView to transitionName)
        val action =
            GroupSplitwiseFragmentDirections.actionGroupSplitwiseFragmentToGroupSettleUpFragment(
                args.groupId, payerId)
        findNavController().navigate(action, extras)
    }

    private fun gotoGroupDirectSettleUpFragment(){
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val action =
            GroupSplitwiseFragmentDirections.actionGroupSplitwiseFragmentToGroupDirectSettleUpFragment(
                args.groupId, -1, -1)
        findNavController().navigate(action)
    }

}