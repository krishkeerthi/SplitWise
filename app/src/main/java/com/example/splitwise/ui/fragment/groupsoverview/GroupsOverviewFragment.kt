package com.example.splitwise.ui.fragment.groupsoverview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsOverviewBinding
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter

class GroupsOverviewFragment : Fragment() {

    private lateinit var binding: FragmentGroupsOverviewBinding

    private val viewModel: GroupsOverviewViewModel by viewModels {
        GroupsOverviewViewModelFactory(requireContext())
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupsOverviewBinding.bind(view)

        // Rv
        val groupsAdapter = GroupsAdapter()

        binding.groupOverviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        // Livedata
        viewModel.groups.observe(viewLifecycleOwner){ groups ->
            if(groups != null){
                groupsAdapter.updateGroups(groups)
            }
        }
    }
}