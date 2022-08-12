package com.example.splitwise.ui.fragment.chooseMembers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentChooseMembersBinding
import com.example.splitwise.ui.fragment.adapter.ChooseMembersAdapter

class ChooseMembersFragment : Fragment() {
    private lateinit var binding: FragmentChooseMembersBinding
    private val args: ChooseMembersFragmentArgs by navArgs()
    private var contextualActionMode: ActionMode? = null

    private val viewModel: ChooseMembersViewModel by viewModels {
        ChooseMembersViewModelFactory(requireContext(), args.selectedMembers)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_choose_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchData()

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
                chooseMembersAdapter.updateMembersAndStreaks(membersAndStreaks)
            }
        }

        // Menu


        // Live observer to update menu

        viewModel.selectedMembersCount.observe(viewLifecycleOwner) {
            if (it > 0) {
                if (contextualActionMode == null)
                    contextualActionMode = requireActivity().startActionMode(actionModeCallback)

            } else {
                contextualActionMode?.finish()
                contextualActionMode = null
            }

        }

       // setHasOptionsMenu(true)
    }

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        inflater.inflate(R.menu.add_expense_fragment_menu, menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when(item.itemId){
//            R.id.add_expense -> {
//                Toast.makeText(requireContext(), "working", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else ->{
//                super.onOptionsItemSelected(item)
//            }
//        }
//
//    }

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
                    gotoCreateEditGroupFragment(viewModel.getSelectedMembers())
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
                args.groupId, selectedMembers, args.groupName
            )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoChooseMemberFragment() {
        val action = ChooseMembersFragmentDirections.actionChooseMembersFragmentSelf(
            args.groupId, args.selectedMembers, args.groupName
        )
        view?.findNavController()?.navigate(action)
    }
}