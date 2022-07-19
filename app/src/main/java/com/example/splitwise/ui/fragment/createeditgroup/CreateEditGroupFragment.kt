package com.example.splitwise.ui.fragment.createeditgroup

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentCreateEditGroupBinding
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.adapter.MembersAdapter
import com.example.splitwise.ui.fragment.addmember.AddMemberDialog
import com.example.splitwise.util.nameCheck

class CreateEditGroupFragment : Fragment() {

    private lateinit var binding: FragmentCreateEditGroupBinding
    private val args: CreateEditGroupFragmentArgs by navArgs()

    private val viewModel: CreateEditGroupViewModel by activityViewModels{
        CreateEditGroupViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_edit_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

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
            if(members != null){
                membersAdapter.updateMembers(members)
            }
        }

        //Group name
        viewModel.groupName.observe(viewLifecycleOwner){ groupName ->
            groupName?.let {
                binding.groupNameText.setText(it)
            }
        }

        // Button click
        binding.addMemberButton.setOnClickListener{
            AddMemberDialog().show(childFragmentManager, "Add Member Alert Dialog")
            // open dialog to add new member.
            // Choosing existing member has to be implemented
        }


        // if group id is null
        // set button visibility based on button state

        binding.createGroupButton.setOnClickListener{
            viewModel.createGroup(
                binding.groupNameText.text.toString()
            )
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
                    if(nameCheck(binding.groupNameText.text?.trim().toString()))
                        View.VISIBLE
                    else
                        View.GONE
            }

        }

        binding.groupNameText.addTextChangedListener(nameWatcher)

    }
}