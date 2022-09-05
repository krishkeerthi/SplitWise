package com.example.splitwise.ui.fragment.splitwise

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
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
import com.example.splitwise.databinding.FragmentSplitWiseBinding
import com.example.splitwise.ui.fragment.adapter.GroupArrayAdapter
import com.example.splitwise.ui.fragment.adapter.SplitWiseAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SplitWiseFragment : Fragment() {

    private lateinit var binding: FragmentSplitWiseBinding
    private val args: SplitWiseFragmentArgs by navArgs()
    private var selectedGroups = mutableListOf<Group>()
    private val viewModel: SplitWiseViewModel by viewModels {
        SplitWiseViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.splitwise)
        return inflater.inflate(R.layout.fragment_split_wise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSplitWiseBinding.bind(view)

        requireActivity().title = "SplitWise"

        try {
            selectedGroups = args.selectedGroups.toMutableList()
            if (selectedGroups.isNotEmpty()) {
                var groupsText = ""
                for (group in selectedGroups) {
                    groupsText += "● ${group.groupName} "
                }
                binding.selectedGroupsText.text = groupsText
                binding.selectedGroupsCard.visibility = View.VISIBLE
            }

            Log.d(TAG, "onViewCreated: fetchData in try")
            viewModel.fetchData(getGroupIds(selectedGroups))

        } catch (e: Exception) {
            Log.d(TAG, "onViewCreated: fetchData in try")
            viewModel.fetchData()
        }

        // Rv
        val splitWiseAdapter = SplitWiseAdapter { payerId: Int, amountOwed: Float, name: String ->
            if (amountOwed == 0f)
                Snackbar.make(
                    binding.root,
                    "$name ${getString(R.string.owes_nothing)}",
                    Snackbar.LENGTH_SHORT
                ).show()
            else {
                gotoSettleUpFragment(payerId)
            }

        }
        // clear button selected groups
        binding.clearGroups.setOnClickListener {
//            selectedGroups.clear()
//            viewModel.fetchData()
//
//            // update selected groups card
//            binding.selectedGroupsText.text = ""
//            binding.selectedGroupsCard.visibility = View.GONE
            gotoSelf()
        }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = splitWiseAdapter
        }

        // Livedata transactions
        viewModel.membersPaymentStatsDetail.observe(viewLifecycleOwner) { membersPaymentStatsDetail ->
            if (membersPaymentStatsDetail != null) {
                splitWiseAdapter.updateMembersPaymentStatsDetail(membersPaymentStatsDetail)
            }
        }

        // Groups
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null) {
                binding.selectGroupButton.setOnClickListener {
                    // openGroupsBottomSheet(groups)
                    gotoChooseGroupsFragment()
                }
            }
        }

//        // load groups if already selected
//        viewModel.groupName.observe(viewLifecycleOwner) { groupName ->
//            binding.chooseGroupText.text = groupName
//        }

    }

    private fun gotoChooseGroupsFragment() {
        val action = SplitWiseFragmentDirections.actionSplitWiseFragmentToChooseGroupsFragment(
            getGroupIds(selectedGroups).toIntArray()
        )
        view?.findNavController()?.navigate(action)
    }

    private fun getGroupIds(groups: MutableList<Group>): List<Int> {
        val groupIds = mutableListOf<Int>()
        for (group in groups) {
            groupIds.add(group.groupId)
        }
        return groupIds.toList()
    }

    private fun openGroupsBottomSheet(groups: List<Group>) {
        val groupBottomSheetDialog = BottomSheetDialog(requireContext())
        groupBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val groupTitle = groupBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val groupList = groupBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)

        groupTitle?.text = getString(R.string.select_group)
        //Adapter
        val groupAdapter = GroupArrayAdapter(requireContext(), R.layout.dropdown, groups)
        groupList?.apply {
            Log.d(TAG, "openPayerBottomSheet: list adapter set")
            adapter = groupAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    viewModel.getGroupMembersPaymentStats(groups[position].groupId)
                    viewModel.groupId = groups[position].groupId
                    viewModel.loadGroupName()
                    // binding.chooseGroupText.text = groups[position].groupName
                    groupBottomSheetDialog.dismiss()
                }
        }

        groupBottomSheetDialog.show()
    }

    private fun gotoSettleUpFragment(payerId: Int) {
        val action =
            SplitWiseFragmentDirections.actionSplitWiseFragmentToSettleUpFragment(
                payerId, getGroupIds(selectedGroups).toIntArray(),
                listOf<Member>().toTypedArray()
            ) // Passing empty list of members when going to settle up fragment
        view?.findNavController()?.navigate(action)
    }

    private fun gotoSelf() {
        val action =
            SplitWiseFragmentDirections.actionSplitWiseFragmentSelf(listOf<Group>().toTypedArray()) // Passing empty list of members when going to settle up fragment
        view?.findNavController()?.navigate(action)
    }
}