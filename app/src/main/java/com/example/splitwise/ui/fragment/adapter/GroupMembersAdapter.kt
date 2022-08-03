package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ExpenseMemberCardBinding
import com.example.splitwise.databinding.GroupMemberCardBinding
import com.example.splitwise.util.roundOff

class GroupMembersAdapter: RecyclerView.Adapter<GroupMembersViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupMemberCardBinding.inflate(view, parent, false)

        return GroupMembersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupMembersViewHolder, position: Int) {
        val member = members[position]

        holder.bind(member)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    fun updateMembers(members: List<Member>) {
        this.members = members
        notifyDataSetChanged()
    }

}

class GroupMembersViewHolder(val binding: GroupMemberCardBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(member: Member) {
        binding.memberNameTextView.text = member.name
        binding.memberPhoneTextView.text = member.phone.toString()
    }

}