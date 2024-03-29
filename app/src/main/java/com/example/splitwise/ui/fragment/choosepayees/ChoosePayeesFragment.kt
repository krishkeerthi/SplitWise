package com.example.splitwise.ui.fragment.choosepayees

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentChooseGroupsBinding
import com.example.splitwise.databinding.FragmentChoosePayeesBinding
import com.example.splitwise.ui.fragment.adapter.ChooseGroupAdapter
import com.example.splitwise.ui.fragment.adapter.ChoosePayeeAdapter
import com.example.splitwise.ui.fragment.choosegroups.ChooseGroupsFragmentArgs
import com.example.splitwise.ui.fragment.choosegroups.ChooseGroupsFragmentDirections
import com.example.splitwise.ui.fragment.choosegroups.ChooseGroupsViewModel
import com.example.splitwise.ui.fragment.choosegroups.ChooseGroupsViewModelFactory
import com.example.splitwise.util.mergeList


class ChoosePayeesFragment : Fragment() {

    private lateinit var binding: FragmentChoosePayeesBinding

    private val args: ChoosePayeesFragmentArgs by navArgs()


    private val viewModel: ChoosePayeesViewModel by viewModels {
        ChoosePayeesViewModelFactory(args.payeesAndAmounts.toMutableList())
    }

    private var contextualActionMode: ActionMode? = null
    private lateinit var payeesAdapter: ChoosePayeeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.select_payee)
        return inflater.inflate(R.layout.fragment_choose_payees, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentChoosePayeesBinding.bind(view)

        // toolbar title
        //requireActivity().title = "Choose Payee"

        payeesAdapter = ChoosePayeeAdapter { member, isChecked ->
            if (isChecked) {
                viewModel.addMemberToSelected(member)
            } else {
                viewModel.removeMemberFromSelected(member)
            }
        }

        binding.choosePayeesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payeesAdapter
        }

//        viewModel.members.observe(viewLifecycleOwner) { members ->
//            if (members != null) {
//                payeesAdapter.updatePayees(members, args.selectedPayeeIds.toList())
//            }
//        }

        viewModel.membersAndAmounts.observe(viewLifecycleOwner) { membersAndAmounts ->
            if (membersAndAmounts != null) {
                //payeesAdapter.updatePayees(membersAndAmounts, args.selectedPayeeIds.toList())
                if(args.selectedPayeeIds.toList().isNotEmpty() && viewModel.selectedPayeesIds().isNotEmpty())
                    payeesAdapter.updatePayees(membersAndAmounts, mergeList(args.selectedPayeeIds.toList() ,viewModel.selectedPayeesIds().toList()))
                else if(args.selectedPayeeIds.toList().isNotEmpty())
                    payeesAdapter.updatePayees(membersAndAmounts, args.selectedPayeeIds.toList())
                else
                    payeesAdapter.updatePayees(membersAndAmounts, viewModel.selectedPayeesIds().toList())
            }
        }

        // Live observer to update menu
        viewModel.selectedMembersCount.observe(viewLifecycleOwner) { selectedMembersCount ->
            if (selectedMembersCount != null && selectedMembersCount > 0) {
                if (contextualActionMode == null)
                    contextualActionMode = requireActivity().startActionMode(actionModeCallback)

            } else {
                contextualActionMode?.finish()
                contextualActionMode = null
            }

        }


        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.choose_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_all_menu -> {
                selectAllPayees()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun selectAllPayees() {
        payeesAdapter.selectAllPayees()
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.apply {
                menuInflater?.inflate(
                    R.menu.choose_members_fragment_menu,
                    menu
                ) // Since done is the only menu, I just reused choose_members_fragment_menu
                title = getString(R.string.choose_payee)

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
                    gotoSettleUpFragment()
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            gotoChoosePayeesFragment()
        }
    }

    private fun gotoSettleUpFragment(){
        // commenting because this fragment won't be used, other this needs to be handled properly
//        val action = ChoosePayeesFragmentDirections.actionChoosePayeesFragmentToSettleUpFragment(
//            args.payerId, args.groupIds, viewModel.getSelectedMembers().toTypedArray()
//        )
//        view?.findNavController()?.navigate(action)
    }

    private fun gotoChoosePayeesFragment(){
        val action = ChoosePayeesFragmentDirections.actionChoosePayeesFragmentSelf(args.payerId, args.groupIds, listOf<Int>().toIntArray(),
            args.payeesAndAmounts)
        view?.findNavController()?.navigate(action)
    }
}