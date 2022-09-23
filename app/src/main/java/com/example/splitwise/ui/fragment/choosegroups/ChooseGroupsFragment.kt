package com.example.splitwise.ui.fragment.choosegroups

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.FragmentChooseGroupsBinding
import com.example.splitwise.ui.fragment.adapter.ChooseGroupAdapter
import com.example.splitwise.util.mergeList
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis


class ChooseGroupsFragment : Fragment() {

    private lateinit var binding: FragmentChooseGroupsBinding
    private val viewModel: ChooseGroupsViewModel by viewModels {
        ChooseGroupsViewModelFactory(requireContext())
    }

    private val args: ChooseGroupsFragmentArgs by navArgs()

    private var contextualActionMode: ActionMode? = null
    private lateinit var groupsAdapter: ChooseGroupAdapter

    private var menuVisible = false

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
            getString(R.string.select_group)
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

        // restore checked groups during screen orientation change

        binding.chooseGroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
//                reverseLayout = true // it reverses but scrolled down to the last item
//                stackFromEnd = true // corrects above problem
            }
            adapter = groupsAdapter
        }

        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null && groups.isNotEmpty()) {

                viewModel.isGroupsEmpty = false

                Log.d(TAG, "onViewCreated: checking ")
                if (args.selectedGroupIds.toList().isNotEmpty() && viewModel.selectedGroupIds()
                        .isNotEmpty()
                ) {
                    Log.d(TAG, "onViewCreated: choose groups both not empty -> args: ${args.selectedGroupIds.toList()} vm selected: ${viewModel.selectedGroupIds()}")
                    // add selected groups in  viewmodel, because in rv only checked items are added, in landscape only few items are visible so few items are added

                    val overallSelected = mergeList(
                        args.selectedGroupIds.toList(),
                        viewModel.selectedGroupIds().toList()
                    )

//                    updateSelectedGroups(groups, overallSelected)
                    updateSelectedGroups(groups, viewModel.selectedGroupIds())

                    groupsAdapter.updateGroups(
                        groups,
                        viewModel.selectedGroupIds() //overallSelected
                    )
                }
                else if (args.selectedGroupIds.toList().isNotEmpty()) {
                    updateSelectedGroups(groups, args.selectedGroupIds.toList())
                    Log.d(TAG, "onViewCreated: choose groups args not empty -> args: ${args.selectedGroupIds.toList()} vm selected: ${viewModel.selectedGroupIds()}")


                    groupsAdapter.updateGroups(groups, args.selectedGroupIds.toList())
                }
                else {
                    Log.d(TAG, "onViewCreated: choose groups both empty -> args: ${args.selectedGroupIds} vm selected: ${viewModel.selectedGroupIds()}")
                    groupsAdapter.updateGroups(groups, viewModel.selectedGroupIds().toList())
                }

                binding.emptyGroupImageView.visibility = View.INVISIBLE
                binding.noGroupsTextView.visibility = View.INVISIBLE
                binding.chooseGroupsRecyclerView.visibility = View.VISIBLE
            } else {
                viewModel.isGroupsEmpty = true
                binding.emptyGroupImageView.visibility = View.VISIBLE
                binding.noGroupsTextView.visibility = View.VISIBLE
                binding.chooseGroupsRecyclerView.visibility = View.INVISIBLE
            }

            requireActivity().invalidateMenu() // initially it may invisible
        }

        // Live observer to update menu
        viewModel.selectedGroupsCount.observe(viewLifecycleOwner) { selectedGroupCount ->
            val selectedCount = viewModel.selectedGroups.size

            viewModel.groups.value?.let {
                viewModel.allSelected = it.size == selectedCount
                requireActivity().invalidateOptionsMenu()
            }

            if(selectedCount > 0){
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    "${viewModel.selectedGroups.size} ${getString(R.string.selected)}"
            }
            else
                (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.select_group)

            menuVisible = selectedCount > 0
            requireActivity().invalidateOptionsMenu()

            // action mode code is commented
//            if (selectedCount > 0) {
//                if (contextualActionMode == null) {
//                    contextualActionMode = requireActivity().startActionMode(actionModeCallback)
//                } else {
//                    contextualActionMode?.title = "$selectedCount ${getString(R.string.selected)}"
//                }
//            } else {
//                contextualActionMode?.finish()
//                contextualActionMode = null
//            }

//            if (selectedGroupCount != null && selectedGroupCount > 0) {
//                if (contextualActionMode == null) {
//                    contextualActionMode = requireActivity().startActionMode(actionModeCallback)
//                }
//                else{
//                    contextualActionMode?.title = "${viewModel.selectedGroups.size} ${getString(R.string.selected)}"
//                }
//            } else {
//                contextualActionMode?.finish()
//                contextualActionMode = null
//            }
        }

        // Options menu
        setHasOptionsMenu(true)
    }

    private fun updateSelectedGroups(groups: List<Group>, selectedGroupsIds: List<Int>) {
        for(group in groups){
            if(group.groupId in selectedGroupsIds){
                viewModel.addGroupToSelected(group)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.choose_menu, menu)

        val selectAllMenu = menu.findItem(R.id.select_all_menu)

        val doneMenu = menu.findItem(R.id.choose_groups_fragment_done)

        val unselectMenu = menu.findItem(R.id.unselect_all_menu)

        doneMenu.isVisible = menuVisible

        unselectMenu.isVisible = viewModel.allSelected

        selectAllMenu.isVisible = !viewModel.allSelected && !viewModel.isGroupsEmpty
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_all_menu -> {
                viewModel.allSelected = true
                requireActivity().invalidateOptionsMenu()
                selectAllGroups()
                true
            }
            R.id.choose_groups_fragment_done -> {
                gotoSplitWiseFragment()
                true
            }
            R.id.unselect_all_menu -> {
                viewModel.allSelected = false
                requireActivity().invalidateOptionsMenu()
                gotoChooseGroupsFragment()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun selectAllGroups() {
//        if (!viewModel.isGroupsEmpty)

        groupsAdapter.selectAllGroups()
//        else
//            Snackbar.make(binding.root, getString(R.string.no_groups), Snackbar.LENGTH_SHORT).show()
        //viewModel.allGroupsSelected() //
    }

    private val actionModeCallback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.apply {
                Log.d(TAG, "onCreateActionMode: action mode create")
                menuInflater?.inflate(
                    R.menu.choose_members_fragment_menu,
                    menu
                ) // Since done is the only menu, I just reused choose_members_fragment_menu
                title =
                    "${viewModel.selectedGroupsCount.value} ${getString(R.string.selected)}" //getString(R.string.select_group)

            }
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            Log.d(TAG, "onPrepareActionMode: action mode prepare")
            mode?.title = "${viewModel.selectedGroupsCount.value} ${getString(R.string.selected)}"
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

    private fun gotoSplitWiseFragment() {
        val action = ChooseGroupsFragmentDirections.actionChooseGroupsFragmentToSplitWiseFragment(
            viewModel.getSelectedGroups().toTypedArray()
        )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoChooseGroupsFragment() {
        val action = ChooseGroupsFragmentDirections.actionChooseGroupsFragmentSelf(IntArray(0))
        view?.findNavController()?.navigate(action)
    }
}