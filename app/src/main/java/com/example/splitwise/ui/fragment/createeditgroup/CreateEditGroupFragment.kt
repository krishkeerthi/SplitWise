package com.example.splitwise.ui.fragment.createeditgroup

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentCreateEditGroupBinding
import com.example.splitwise.ui.activity.main.MainActivity
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.adapter.MembersAdapter
import com.example.splitwise.ui.fragment.addmember.AddMemberDialog
import com.example.splitwise.util.nameCheck

class CreateEditGroupFragment : Fragment() {

    private lateinit var binding: FragmentCreateEditGroupBinding
    private val args: CreateEditGroupFragmentArgs by navArgs()

    private val viewModel: CreateEditGroupViewModel by viewModels{
        CreateEditGroupViewModelFactory(requireContext(), args.groupId, args.selectedMembers)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_edit_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        viewModel.fetchData()
        Log.d(TAG, "onViewCreated: createdit group id : ${args.groupId}")
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCreateEditGroupBinding.bind(view)

        // Rv
        val membersAdapter = MembersAdapter()

        binding.groupMembersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        // Livedata Members
        viewModel.members.observe(viewLifecycleOwner){ members ->
            Log.d(TAG, "onViewCreated: members livedata ${members}")
            if(members != null){
                Log.d(TAG, "onViewCreated: addMember observed ${members}")
                membersAdapter.updateMembers(members)

                binding.constraintTextView.visibility = if(members.size > 1)
                     View.GONE
                else
                    View.VISIBLE

            }
        }

        //Group name
        viewModel.groupName.observe(viewLifecycleOwner){ groupName ->
            Log.d(TAG, "onViewCreated: groupName livedata ${groupName}")
            groupName?.let {
                binding.groupNameText.setText(it)
            }
        }

        // Button click
        binding.addMemberButton.setOnClickListener{
            AddMemberDialog(viewModel).show(childFragmentManager, "Add Member Alert Dialog")
            // open dialog to add new member.
            // Choosing existing member has to be implemented
        }

        binding.chooseMembersButton.setOnClickListener{
            gotoChooseMembersFragment()
        }



        // if group id is not null
        // make sure group name is not editable
        if(args.groupId != -1){
            binding.groupNameText.isClickable = false
            binding.groupNameText.isFocusable = false
        }

        binding.createGroupButton.setOnClickListener{
            if(args.groupId == -1){

                if(viewModel.getMembersSize() > 1) {
                    val memberId = requireActivity().getSharedPreferences(
                        MainActivity.KEY,
                        Context.MODE_PRIVATE
                    )
                        .getInt("MEMBERID", -1)

                    viewModel.createGroup(
                        binding.groupNameText.text.toString(),
                        memberId
                    ){
                        gotoGroupsFragment()
                    }

                    Toast.makeText(
                        requireContext(),
                        "Group created successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                   // gotoGroupsFragment()
                    // calling here goes to groups fragment without completing the group creation process
                }
                else
                    Toast.makeText(requireContext(), "Add atleast 2 members to create group", Toast.LENGTH_SHORT).show()
            }
            else{
                Toast.makeText(requireContext(), "Group already created", Toast.LENGTH_SHORT).show()
            }

        }

        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (nameCheck(binding.groupNameText.text?.trim().toString())){
                    binding.outlinedGroupNameTextField.error = null
                    binding.outlinedGroupNameTextField.isErrorEnabled = false
                }
                else {
                    binding.outlinedGroupNameTextField.error = "Enter valid name"
                }

                binding.createGroupButton.visibility =
                    if(nameCheck(binding.groupNameText.text?.trim().toString()) && args.groupId == -1)
                        View.VISIBLE
                    else
                        View.GONE
            }

        }

        binding.groupNameText.addTextChangedListener(nameWatcher)

    }

    private fun gotoChooseMembersFragment() {
        val action = CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToChooseMembersFragment(
            args.groupId, viewModel.members.value?.toTypedArray())
        view?.findNavController()?.navigate(action)
    }

    private fun gotoGroupsFragment() {
        Log.d(TAG, "gotoGroupsFragment: called group created")
        val action = CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }


}