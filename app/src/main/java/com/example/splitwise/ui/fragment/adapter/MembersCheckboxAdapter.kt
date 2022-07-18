package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberCheckboxCardBinding
import com.example.splitwise.databinding.MemberProfileCardBinding

class MembersCheckboxAdapter: RecyclerView.Adapter<MembersCheckboxViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersCheckboxViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberCheckboxCardBinding.inflate(view, parent, false)

        return MembersCheckboxViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MembersCheckboxViewHolder, position: Int) {
        val member = members[position]

        holder.bind(member)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    fun updateMembers(members: List<Member>){
        this.members = members
        notifyDataSetChanged()
    }
}

class MembersCheckboxViewHolder(val binding: MemberCheckboxCardBinding)
    : RecyclerView.ViewHolder(binding.root){

    fun bind(member: Member){
        binding.memberTextView.text = member.name
    }

}