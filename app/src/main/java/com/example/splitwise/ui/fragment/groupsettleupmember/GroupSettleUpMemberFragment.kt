package com.example.splitwise.ui.fragment.groupsettleupmember

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentGroupSettleUpMemberBinding
import com.example.splitwise.model.MemberAndStreak
import com.example.splitwise.ui.fragment.adapter.GroupSettleUpMemberAdapter
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.hideKeyboard
import com.google.android.material.transition.MaterialSharedAxis

class GroupSettleUpMemberFragment : Fragment() {

    private lateinit var binding: FragmentGroupSettleUpMemberBinding
    private val args: GroupSettleUpMemberFragmentArgs by navArgs()
    private val viewModel: GroupSettleUpMemberViewModel by viewModels()

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
        val title =
            if (args.isPayerSelection) getString(R.string.choose_payer) else getString(R.string.choose_recipient)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title

        return inflater.inflate(R.layout.fragment_group_settle_up_member, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupSettleUpMemberBinding.bind(view)

        val membersAdapter =
            GroupSettleUpMemberAdapter(args.groupMembers.toList(), viewModel.textEntered) { memberId: Int ->
                gotoGroupDirectSettleUpFragment(memberId)
            }

        binding.settleUpMemberRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = membersAdapter
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.group_settle_up_member_fragment_menu, menu)

        // search
        val searchItem = menu.findItem(R.id.search_member_menu)
        val searchView = searchItem?.actionView as SearchView

        searchView.updatePadding(left = (-16).dpToPx(resources.displayMetrics))

        if (viewModel.textEntered != "") {
            Log.d(ContentValues.TAG, "onCreateOptionsMenu: text entered ${viewModel.textEntered}")
            searchItem.expandActionView()
            searchView.requestFocus()
            searchView.setQuery(viewModel.textEntered, true)
            fetchSearchedData(viewModel.textEntered)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.hideKeyboard()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null && query.trim().isNotEmpty()) {
                    Log.d(
                        ContentValues.TAG,
                        "onCreateOptionsMenu: text entered before vm update ${viewModel.textEntered}"
                    )
                    viewModel.textEntered = query.trim()
                    Log.d(
                        ContentValues.TAG,
                        "onCreateOptionsMenu: text entered after vm update ${viewModel.textEntered}"
                    )
                    fetchSearchedData(viewModel.textEntered)
                }
                else{
                    viewModel.textEntered = ""
                    fetchSearchedData(viewModel.textEntered)
                }
                return true
            }
        })
    }

    private fun fetchSearchedData(query: String) {
        val updatedList = mutableSetOf<Member>()

        for (member in args.groupMembers.toList()) {
            if (member.name.lowercase().startsWith(query)) {
                updatedList.add(member)
                Log.d(ContentValues.TAG, "getSearchResult: starts with ${member.name}")
            }
        }

        for (member in args.groupMembers.toList()) {
            if (member.name.lowercase().contains(query)) {
                updatedList.add(member)
                Log.d(ContentValues.TAG, "getSearchResult: starts with ${member.name}")
            }
        }

        updateAdapter(updatedList.toList())
    }

    private fun updateAdapter(memberList: List<Member>){
        val adapter = GroupSettleUpMemberAdapter(memberList, viewModel.textEntered) { memberId: Int ->
            gotoGroupDirectSettleUpFragment(memberId)
        }

        binding.settleUpMemberRecyclerView.adapter = adapter
    }

    private fun gotoGroupDirectSettleUpFragment(memberId: Int) {
        if (args.isPayerSelection) {
            val action =
                GroupSettleUpMemberFragmentDirections.actionGroupSettleUpMemberFragmentToGroupDirectSettleUpFragment(
                    args.groupId, memberId, args.recipientId
                )
            findNavController().navigate(action)
        } else {
            val action =
                GroupSettleUpMemberFragmentDirections.actionGroupSettleUpMemberFragmentToGroupDirectSettleUpFragment(
                    args.groupId, args.payerId, memberId
                )
            findNavController().navigate(action)
        }
    }
}