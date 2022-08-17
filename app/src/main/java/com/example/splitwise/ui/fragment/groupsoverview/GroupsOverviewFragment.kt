package com.example.splitwise.ui.fragment.groupsoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsOverviewBinding
import com.example.splitwise.ui.fragment.adapter.GroupsOverviewAdapter

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

        requireActivity().title = "Groups Overview"

        // Rv
        val groupsAdapter = GroupsOverviewAdapter { groupId: Int ->
            gotoExpensesOverviewFragment(groupId)
        }

        binding.groupOverviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        // Livedata Groups overview
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null) {
                groupsAdapter.updateGroups(groups)
            }
        }

        binding.searchButton.setOnClickListener {
           // gotoSearchImageFragment()
        }
    }

    private fun gotoExpensesOverviewFragment(groupId: Int) {
        val action =
            GroupsOverviewFragmentDirections.actionGroupsOverviewFragmentToExpensesOverviewFragment(
                groupId
            )
        view?.findNavController()?.navigate(action)
    }

//    private fun gotoSearchImageFragment() {
//        val action =
//            GroupsOverviewFragmentDirections.actionGroupsOverviewFragmentToSearchImageFragment()
//        view?.findNavController()?.navigate(action)
//    }

}