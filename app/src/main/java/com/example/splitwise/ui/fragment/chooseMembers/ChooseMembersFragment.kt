package com.example.splitwise.ui.fragment.chooseMembers

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
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

        binding = FragmentChooseMembersBinding.bind(view)

        // Adapter
        val chooseMembersAdapter = ChooseMembersAdapter{
            member: Member, isChecked: Boolean ->
            if(isChecked){
                viewModel.addMemberToSelected(member)
            }
            else{
                viewModel.removeMemberFromSelected(member)
            }

        }

        // RV
        binding.chooseMembersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chooseMembersAdapter
        }

        viewModel.members.observe(viewLifecycleOwner){ members ->
            if(members != null){
                chooseMembersAdapter.updateMembers(members)
            }
        }

        // Menu


        // Live observer to update menu

        viewModel.selectedMembersCount.observe(viewLifecycleOwner){
            if(it <= 0)
                setHasOptionsMenu(false)
            else
                setHasOptionsMenu(true)
        }
    }

    @SuppressLint("RestrictedApi")
    private fun hideOptionMenu(){
        (activity as AppCompatActivity).supportActionBar?.closeOptionsMenu()
    }

    @SuppressLint("RestrictedApi")
    private fun showOptionMenu(){
        (activity as AppCompatActivity).supportActionBar?.openOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.choose_members_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.choose_member_fragment_done -> {
                gotoCreateEditGroupFragment(viewModel.getSelectedMembers())
                true
            }
            else -> false
        }
    }

    private fun gotoCreateEditGroupFragment(selectedMembers: Array<Member>){
        val action = ChooseMembersFragmentDirections.actionChooseMembersFragmentToCreateEditGroupFragment(
            args.groupId, selectedMembers, args.groupName)
        view?.findNavController()?.navigate(action)
    }
}