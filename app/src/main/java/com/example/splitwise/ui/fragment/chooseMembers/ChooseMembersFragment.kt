package com.example.splitwise.ui.fragment.chooseMembers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentChooseMembersBinding
import com.example.splitwise.ui.fragment.adapter.ChooseMembersAdapter
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialSharedAxis

class ChooseMembersFragment : Fragment() {
    private lateinit var binding: FragmentChooseMembersBinding
    private val args: ChooseMembersFragmentArgs by navArgs()
    private var contextualActionMode: ActionMode? = null
    private var menuVisible = false
    private var selectedMemberCount = 0

    private val viewModel: ChooseMembersViewModel by viewModels {
        ChooseMembersViewModelFactory(requireContext(), args.selectedMembers)
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
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.choose_members)
        return inflater.inflate(R.layout.fragment_choose_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchData(){
            binding.chooseMembersRecyclerView.adapter?.notifyDataSetChanged()
        }

        Log.d(TAG, "onViewCreated: group Icon choose member${args.groupIcon}")

        Log.d(ContentValues.TAG, "onCreateDialog: membercheck inside  choose member onviewcreated, ${viewModel.getSelectedMembers().toList()}")
        //requireActivity().title = "Choose Members"

        binding = FragmentChooseMembersBinding.bind(view)

        // Adapter
        val chooseMembersAdapter = ChooseMembersAdapter { member: Member, isChecked: Boolean ->
            if (isChecked) {
                viewModel.addMemberToSelected(member)
            } else {
                viewModel.removeMemberFromSelected(member)
            }

        }

        // RV
        binding.chooseMembersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chooseMembersAdapter
        }

        viewModel.membersAndStreaks.observe(viewLifecycleOwner) { membersAndStreaks ->
            if (membersAndStreaks != null) {
                if(membersAndStreaks.isNotEmpty()) {
                    chooseMembersAdapter.updateMembersAndStreaks(membersAndStreaks, viewModel.checkedMembersIds(), viewModel.textEntered)

                    Log.d(TAG, "onViewCreated: mcount${viewModel.checkedMembersIds().size}")
                    binding.chooseMembersRecyclerView.visibility = View.VISIBLE
                    binding.emptyText.visibility = View.INVISIBLE
                }
                else{
                    binding.chooseMembersRecyclerView.visibility = View.INVISIBLE
                    binding.emptyText.visibility = View.VISIBLE
                }
            }
        }

        // Menu


        // Live observer to update menu

        viewModel.selectedMembersCount.observe(viewLifecycleOwner) {
//            if (it > 0) {
//                if (contextualActionMode == null)
//                    contextualActionMode = requireActivity().startActionMode(actionModeCallback)
//
//            } else {
//                contextualActionMode?.finish()
//                contextualActionMode = null
//            }


            val selectedCount = viewModel.checkedMembers.size
            if(selectedCount > 0){
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    "${viewModel.checkedMembers.size} ${getString(R.string.selected)}"
            }
            else
                (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.choose_members)

            menuVisible = selectedCount > 0
            selectedMemberCount = selectedCount

            viewModel.previouslyEnteredText = viewModel.textEntered
            requireActivity().invalidateOptionsMenu()

//            if(it > 0) {
//                (requireActivity() as AppCompatActivity).supportActionBar?.title =
//                    "${viewModel.checkedMembers.size} ${getString(R.string.selected)}"
//            }
//            else{
//
//            }
//                (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.choose_members)
//            menuVisible = it > 0
//            requireActivity().invalidateOptionsMenu()

        }


        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.choose_members_fragment_menu, menu)

        val doneMenu = menu.findItem(R.id.choose_member_fragment_done)

        //doneMenu.isVisible = menuVisible

        // search
        val searchItem = menu.findItem(R.id.search_member_menu)
        val searchView = searchItem?.actionView as SearchView

        searchView.updatePadding(left = (-16).dpToPx(resources.displayMetrics))

        if(viewModel.previouslyEnteredText != "")
            viewModel.textEntered = viewModel.previouslyEnteredText

        if(viewModel.textEntered != "") {
           Log.d(TAG, "onCreateOptionsMenu: text entered ${viewModel.textEntered}")
            searchItem.expandActionView()
            searchView.requestFocus()
            searchView.setQuery(viewModel.textEntered, true)
            viewModel.fetchSearchedData()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.hideKeyboard()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    Log.d(TAG, "onCreateOptionsMenu: text entered before vm update ${viewModel.textEntered}")
                    viewModel.textEntered = query
                    Log.d(TAG, "onCreateOptionsMenu: text entered after vm update ${viewModel.textEntered}")
                    viewModel.fetchSearchedData()
                }
                return true
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.choose_member_fragment_done -> {
                if(selectedMemberCount == 0){
                    Snackbar.make(binding.root, getString(R.string.no_members_selected), Snackbar.LENGTH_SHORT).show()
                }
                else
                    gotoCreateEditGroupFragment(viewModel.getCheckedMembers())
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }

    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.apply {
                menuInflater?.inflate(R.menu.choose_members_fragment_menu, menu)
                title = getString(R.string.choose_members)
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
                    gotoCreateEditGroupFragment(viewModel.getCheckedMembers()) //viewModel.getSelectedMembers()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            gotoChooseMemberFragment()
        }

    }

    @SuppressLint("RestrictedApi")
    private fun hideOptionMenu() {
        (activity as AppCompatActivity).supportActionBar?.closeOptionsMenu()
    }

    @SuppressLint("RestrictedApi")
    private fun showOptionMenu() {
        (activity as AppCompatActivity).supportActionBar?.openOptionsMenu()
    }

    private fun gotoCreateEditGroupFragment(selectedMembers: Array<Member>) {
        val action =
            ChooseMembersFragmentDirections.actionChooseMembersFragmentToCreateEditGroupFragment(
                args.groupId, selectedMembers, args.groupName, args.groupIcon // need to check
            )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoChooseMemberFragment() {
        val action = ChooseMembersFragmentDirections.actionChooseMembersFragmentSelf(
            args.groupId, args.selectedMembers, args.groupName, args.groupIcon
        )
        view?.findNavController()?.navigate(action)
    }
}