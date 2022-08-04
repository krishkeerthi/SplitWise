package com.example.splitwise.ui.fragment.createeditgroup

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentCreateEditGroupBinding
import com.example.splitwise.ui.activity.main.MainActivity
import com.example.splitwise.ui.fragment.adapter.GroupMembersAdapter
import com.example.splitwise.ui.fragment.addmember.AddMemberDialog
import com.example.splitwise.util.nameCheck

class CreateEditGroupFragment : Fragment() {

    private lateinit var binding: FragmentCreateEditGroupBinding
    private val args: CreateEditGroupFragmentArgs by navArgs()

    private val viewModel: CreateEditGroupViewModel by viewModels {
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

        Log.d(TAG, "onCreateDialog: membercheck inside create edit grop , ${viewModel.members.value}")

        Log.d(TAG, "onViewCreated: createdit group id : ${args.groupId}")
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCreateEditGroupBinding.bind(view)

        if (args.groupId == -1)
            requireActivity().title = "Create Group"
        else
            requireActivity().title = "Edit Group"

        // Rv
        val membersAdapter = GroupMembersAdapter()

        binding.groupMembersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        // Livedata Members
        viewModel.members.observe(viewLifecycleOwner) { members ->
            Log.d(TAG, "onViewCreated: members livedata $members")
            if (members != null) {
                Log.d(TAG, "onViewCreated: addMember observed $members")
                membersAdapter.updateMembers(members)

            }
        }

        //Group name
        viewModel.groupName.observe(viewLifecycleOwner) { groupName ->
            Log.d(TAG, "onViewCreated: groupName livedata $groupName")
            groupName?.let {
                binding.groupNameText.setText(it)
            }
        }

        // retains group name while returning from choose members screen
        if (args.groupName != null) {
            binding.groupNameText.setText(args.groupName)

//            if (args.groupName.toString().trim() != "")
//                binding.createGroupButton.visibility = View.VISIBLE
        }

        // Button click
//        binding.addMemberButton.setOnClickListener {
//            AddMemberDialog(viewModel).show(childFragmentManager, "Add Member Alert Dialog")
//            // open dialog to add new member.
//            // Choosing existing member has to be implemented
//        }

        binding.chooseMemberButton.setOnClickListener {
            gotoChooseMembersFragment()
        }


        // if group id is not null
        // make sure group name is not editable
        if (args.groupId != -1) {
            binding.groupNameText.isClickable = false
            binding.groupNameText.isFocusable = false
        }

//        binding.createGroupButton.setOnClickListener {
//            createGroup()
//        }

        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (nameCheck(binding.groupNameText.text?.trim().toString())) {
                    binding.outlinedGroupNameTextField.error = null
                    binding.outlinedGroupNameTextField.isErrorEnabled = false
                } else {
                    binding.outlinedGroupNameTextField.error = "Enter valid name"
                }

//                binding.createGroupButton.visibility =
//                    if (nameCheck(
//                            binding.groupNameText.text?.trim().toString()
//                        ) && args.groupId == -1
//                    )
//                        View.VISIBLE
//                    else
//                        View.GONE
            }

        }

        binding.groupNameText.addTextChangedListener(nameWatcher)

        // Menu

        setHasOptionsMenu(true)

    }

    private fun createGroup() {
        if (nameCheck(binding.groupNameText.text?.trim().toString())) {

            if (viewModel.getMembersSize() > 1) {
                val memberId = requireActivity().getSharedPreferences(
                    MainActivity.KEY,
                    Context.MODE_PRIVATE
                )
                    .getInt("MEMBERID", -1)

                viewModel.createGroup(
                    binding.groupNameText.text.toString(),
                    memberId
                ) {
                    gotoGroupsFragment()
                }

                Toast.makeText(
                    requireContext(),
                    "Group created successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // gotoGroupsFragment()
                // calling here goes to groups fragment without completing the group creation process
            } else
                Toast.makeText(
                    requireContext(),
                    "Add atleast 2 members to create group",
                    Toast.LENGTH_SHORT
                ).show()
        } else {
            Toast.makeText(requireContext(), "Group Name Missing", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.create_edit_fragment_menu, menu)

        menu.findItem(R.id.create_group).isVisible = args.groupId == -1

        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_member -> {
                AddMemberDialog(viewModel).show(childFragmentManager, "Add Member Alert Dialog")
                true
            }
            R.id.create_group ->{
                createGroup()
                true
            }
            else ->{
                false
            }
        }
    }

    private fun gotoChooseMembersFragment() {
        Log.d(TAG, "onCreateDialog: membercheck choose member clicked, ${viewModel.members.value}")
        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToChooseMembersFragment(
                args.groupId,
                viewModel.members.value?.toTypedArray(),
                binding.groupNameText.text.toString()
            )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoGroupsFragment() {
        Log.d(TAG, "gotoGroupsFragment: called group created")
        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }


}