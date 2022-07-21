package com.example.splitwise.ui.fragment.groups

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsBinding
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter

class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private val viewModel: GroupsViewModel by viewModels {
        GroupsViewModelFactory(requireContext())
    }

    //private lateinit var viewModel: GroupsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchData() // fetching data in viewmodel init{} does not get called while returning from back stack
        //viewModel = ViewModelProvider(this, GroupsViewModelFactory(requireContext()))[GroupsViewModel::class.java]
        binding = FragmentGroupsBinding.bind(view)

        // Rv
        val groupsAdapter = GroupsAdapter(
            { groupId: Int ->
                goToCreateEditGroupFragment(groupId)
            },
            { groupId: Int ->
                goToExpenseFragment(groupId)
            }
        )

        binding.groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        // Livedata
        viewModel.groups.observe(viewLifecycleOwner){ groups ->
            Log.d(TAG, "onViewCreated: groups livedata ${groups}")
            if(groups != null){
                groupsAdapter.updateGroups(groups)
            }
        }

        // Button click
        binding.addGroupFab.setOnClickListener {
            goToCreateEditGroupFragment()
        }

    }

    private fun goToCreateEditGroupFragment(groupId: Int = -1){
        val action = GroupsFragmentDirections.actionGroupsFragmentToCreateEditGroupFragment(groupId, -1)
        view?.findNavController()?.navigate(action)
    }

    private fun goToExpenseFragment(groupId: Int){
        val action = GroupsFragmentDirections.actionGroupsFragmentToExpensesFragment(groupId)
        view?.findNavController()?.navigate(action)
    }
}