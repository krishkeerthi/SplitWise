package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberCardBinding

class ChooseMembersAdapter(val onItemClicked: (Int) -> Unit)
    : RecyclerView.Adapter<ChooseMembersViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberCardBinding.inflate(view, parent, false)

        return ChooseMembersViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(members[adapterPosition].memberId)
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

class ChooseMembersViewHolder(val binding: MemberCardBinding)
    : RecyclerView.ViewHolder(binding.root){

    fun bind(member: Member){
        binding.memberNameTextView.text = member.name
    }

}