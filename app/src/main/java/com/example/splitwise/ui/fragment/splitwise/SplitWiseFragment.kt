package com.example.splitwise.ui.fragment.splitwise

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSplitWiseBinding
import com.example.splitwise.ui.fragment.adapter.GroupArrayAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.adapter.SplitWiseAdapter

class SplitWiseFragment : Fragment() {

    private lateinit var binding: FragmentSplitWiseBinding

    private val viewModel: SplitWiseViewModel by viewModels {
        SplitWiseViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_split_wise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchData()

        binding = FragmentSplitWiseBinding.bind(view)

        // Rv
        val splitWiseAdapter = SplitWiseAdapter{ payerId: Int, amountOwed: Float, name: String ->
            if(amountOwed == 0f)
                Toast.makeText(requireContext(), "$name owes nothing", Toast.LENGTH_SHORT).show()
            else{
                if(binding.allGroupsCheckbox.isChecked)
                    gotoSettleUpFragment(payerId, -1)
                else
                    gotoSettleUpFragment(payerId, viewModel.groupId)
            }

        }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = splitWiseAdapter
        }

        // Livedata transactions
        viewModel.membersPaymentStatsDetail.observe(viewLifecycleOwner){ membersPaymentStatsDetail ->
            if(membersPaymentStatsDetail != null){
                splitWiseAdapter.updateMembersPaymentStatsDetail(membersPaymentStatsDetail)
            }
        }

        // Groups
        viewModel.groups.observe(viewLifecycleOwner){ groups ->
            if(groups != null){
                binding.groupsSpinner.apply {
                    adapter = GroupArrayAdapter(requireContext(), R.layout.dropdown, groups)


                    onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(
                            parent: AdapterView<*>?,
                            view: View?,
                            position: Int,
                            id: Long
                        ) {
                            viewModel.getGroupMembersPaymentStats(groups[position].groupId)
                            viewModel.groupId = groups[position].groupId

                            Log.d(TAG, "onItemSelected: dropdown selected")
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {
                        }

                    }
                }
            }
        }

        binding.allGroupsCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                viewModel.getGroupMembersPaymentStats()
                binding.groupsSpinner.isEnabled = false
                binding.groupsSpinner.isClickable = false
            }
            else{
                viewModel.getGroupMembersPaymentStats(viewModel.groupId)
                binding.groupsSpinner.isEnabled = true
                binding.groupsSpinner.isClickable = true
            }
        }

    }

    private fun gotoSettleUpFragment(payerId: Int, groupId: Int){
        val action = SplitWiseFragmentDirections.actionSplitWiseFragmentToSettleUpFragment(payerId, groupId)
        view?.findNavController()?.navigate(action)
    }
}