package com.example.splitwise.ui.fragment.splitwise

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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

        binding = FragmentSplitWiseBinding.bind(view)

        // Rv
        val splitWiseAdapter = SplitWiseAdapter()

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = splitWiseAdapter
        }

        // Livedata
        viewModel.membersPaymentStatsDetail.observe(viewLifecycleOwner){ membersPaymentStatsDetail ->
            if(membersPaymentStatsDetail != null){
                splitWiseAdapter.updateMembersPaymentStatsDetail(membersPaymentStatsDetail)
            }
        }


        viewModel.groups.observe(viewLifecycleOwner){ groups ->
            if(groups != null){
                binding.groupsDropdown.setAdapter(
                    GroupArrayAdapter(requireContext(), R.layout.dropdown, groups)
                )
            }
        }

    }
}