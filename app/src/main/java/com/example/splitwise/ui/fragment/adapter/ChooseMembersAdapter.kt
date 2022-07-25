package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ChooseMemberCardBinding
import com.example.splitwise.databinding.MemberCardBinding

class ChooseMembersAdapter(val onItemChecked: (Member, Boolean) -> Unit)
    : RecyclerView.Adapter<ChooseMembersViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChooseMemberCardBinding.inflate(view, parent, false)

        return ChooseMembersViewHolder(binding).apply {
            binding.selectedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked)
                    onItemChecked(members[adapterPosition], true)
                else
                    onItemChecked(members[adapterPosition], false)
            }
        }
    }

    override fun onBindViewHolder(holder: ChooseMembersViewHolder, position: Int) {
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

class ChooseMembersViewHolder(val binding: ChooseMemberCardBinding)
    : RecyclerView.ViewHolder(binding.root){

    fun bind(member: Member){
        binding.memberNameTextView.text = member.name
    }

}