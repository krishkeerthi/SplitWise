package com.example.splitwise.ui.fragment.choosegroups

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentChooseGroupsBinding
import com.example.splitwise.ui.fragment.adapter.ChooseGroupAdapter
import com.example.splitwise.ui.fragment.chooseMembers.ChooseMembersFragmentDirections


class ChooseGroupsFragment : Fragment() {

    private lateinit var binding: FragmentChooseGroupsBinding
    private val viewModel: ChooseGroupsViewModel by viewModels {
        ChooseGroupsViewModelFactory(requireContext())
    }

    private val args: ChooseGroupsFragmentArgs by navArgs()

    private var contextualActionMode: ActionMode? = null
    private lateinit var groupsAdapter: ChooseGroupAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChooseGroupsBinding.bind(view)

        // toolbar title
        //requireActivity().title = "Choose Groups"

        groupsAdapter = ChooseGroupAdapter { group, isChecked ->
            if (isChecked) {
                viewModel.addGroupToSelected(group)
            } else {
                viewModel.removeGroupFromSelected(group)
            }
        }

        binding.chooseGroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null) {
                groupsAdapter.updateGroups(groups, args.selectedGroupIds.toList())
            }
        }

        // Live observer to update menu
        viewModel.selectedGroupsCount.observe(viewLifecycleOwner) { selectedGroupCount ->
            if (selectedGroupCount != null && selectedGroupCount > 0) {
                if (contextualActionMode == null)
                    contextualActionMode = requireActivity().startActionMode(actionModeCallback)

            } else {
                contextualActionMode?.finish()
                contextualActionMode = null
            }

        }

        // Options menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.choose_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_all_menu -> {
                selectAllGroups()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun selectAllGroups() {
        groupsAdapter.selectAllGroups()
        //viewModel.allGroupsSelected() //
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.apply {
                menuInflater?.inflate(
                    R.menu.choose_members_fragment_menu,
                    menu
                ) // Since done is the only menu, I just reused choose_members_fragment_menu
                title = getString(R.string.select_group)

            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false // false when no updation performed
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.choose_member_fragment_done -> {
                    mode?.finish()
                    gotoSplitWiseFragment()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
             gotoChooseGroupsFragment()
        }
    }

    private fun gotoSplitWiseFragment(){
        val action = ChooseGroupsFragmentDirections.actionChooseGroupsFragmentToSplitWiseFragment(
            viewModel.getSelectedGroups().toTypedArray()
        )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoChooseGroupsFragment(){
        val action = ChooseGroupsFragmentDirections.actionChooseGroupsFragmentSelf(IntArray(0))
        view?.findNavController()?.navigate(action)
    }
}