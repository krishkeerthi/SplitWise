package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ExpenseMemberCardBinding
import com.example.splitwise.databinding.GroupMemberCardBinding
import com.example.splitwise.util.ripple
import com.example.splitwise.util.roundOff

class GroupMembersAdapter(
    val memberClicked: (Int, View) -> Unit
): RecyclerView.Adapter<GroupMembersViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupMemberCardBinding.inflate(view, parent, false)

        return GroupMembersViewHolder(binding).apply {
            itemView.setOnClickListener {
                it.ripple(it.context)
                memberClicked(members[absoluteAdapterPosition].memberId, itemView)
            }
        }
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

    val resources = binding.root.resources
    fun bind(member: Member) {

        ViewCompat.setTransitionName(binding.root, String.format(resources.getString(R.string.create_edit_group_members_transition_name), member.memberId))
        binding.memberNameTextView.text = member.name
        binding.memberPhoneTextView.text = member.phone.toString()

        if(member.memberProfile != null){
            binding.memberImageView.setImageURI(member.memberProfile)

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        }
    }

}