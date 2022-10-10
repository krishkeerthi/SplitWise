package com.example.splitwise.ui.fragment.removedmembers

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentRemovedMembersBinding
import com.example.splitwise.ui.fragment.adapter.RemovedMembersAdapter


class RemovedMembersFragment : Fragment() {
    private lateinit var binding: FragmentRemovedMembersBinding
    private val args: RemovedMembersFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.removed_members)
        return inflater.inflate(R.layout.fragment_removed_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "gotoRemovedMembersFragment: ${args.removedMembers.toList()}")
        binding = FragmentRemovedMembersBinding.bind(view)

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RemovedMembersAdapter(args.removedMembers.toList())
        }
    }
}