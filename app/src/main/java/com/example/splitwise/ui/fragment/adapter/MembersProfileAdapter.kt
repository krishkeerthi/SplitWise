package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberProfileCardBinding

class MembersProfileAdapter : RecyclerView.Adapter<MembersProfileViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberProfileCardBinding.inflate(view, parent, false)

        return MembersProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MembersProfileViewHolder, position: Int) {
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

class MembersProfileViewHolder(val binding: MemberProfileCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(member: Member) {
        binding.memberNameTextView.text = member.name
    }

}