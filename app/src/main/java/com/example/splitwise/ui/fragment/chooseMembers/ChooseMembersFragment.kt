package com.example.splitwise.ui.fragment.chooseMembers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentChooseMembersBinding
import com.example.splitwise.ui.fragment.adapter.ChooseMembersAdapter

class ChooseMembersFragment : Fragment() {
    private lateinit var binding: FragmentChooseMembersBinding
    private val args: ChooseMembersFragmentArgs by navArgs()

    private val viewModel: ChooseMembersViewModel by viewModels {
        ChooseMembersViewModelFactory(requireContext(), args.existingMembers)
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
            memberId: Int -> gotoCreateEditGroupFragment(memberId)
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
    }

    private fun gotoCreateEditGroupFragment(memberId: Int){
        val action = ChooseMembersFragmentDirections.actionChooseMembersFragmentToCreateEditGroupFragment(args.groupId, memberId)
        view?.findNavController()?.navigate(action)
    }
}