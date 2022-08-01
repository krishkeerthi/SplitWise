package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ChooseMemberCardBinding
import com.example.splitwise.model.MemberAndStreak

class ChooseMembersAdapter(val onItemChecked: (Member, Boolean) -> Unit) :
    RecyclerView.Adapter<ChooseMembersViewHolder>() {
    private var membersAndStreaks = listOf<MemberAndStreak>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChooseMemberCardBinding.inflate(view, parent, false)

        return ChooseMembersViewHolder(binding).apply {
            binding.selectedCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked)
                    onItemChecked(membersAndStreaks[adapterPosition].member, true)
                else
                    onItemChecked(membersAndStreaks[adapterPosition].member, false)
            }
        }
    }

    override fun onBindViewHolder(holder: ChooseMembersViewHolder, position: Int) {
        val member = membersAndStreaks[position]

        holder.bind(member)
    }

    override fun getItemCount(): Int {
        return membersAndStreaks.size
    }

    fun updateMembersAndStreaks(membersAndStreaks: List<MemberAndStreak>) {
        this.membersAndStreaks = membersAndStreaks
        notifyDataSetChanged()
    }
}

class ChooseMembersViewHolder(val binding: ChooseMemberCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(memberAndStreak: MemberAndStreak) {
        binding.memberNameTextView.text = memberAndStreak.member.name
        binding.memberStreakTextView.text =  "🔥" + memberAndStreak.streak.toString()
    }

}