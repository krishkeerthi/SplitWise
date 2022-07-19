package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberCheckboxCardBinding
import com.example.splitwise.databinding.MemberProfileCardBinding

class MembersCheckboxAdapter(private val onItemChecked: (Int, Boolean) -> Unit)
    : RecyclerView.Adapter<MembersCheckboxViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersCheckboxViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberCheckboxCardBinding.inflate(view, parent, false)


        return MembersCheckboxViewHolder(binding, onItemChecked, members)//.apply {
//            binding.paidUnpaidCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
//                if(isChecked){
//                    onItemChecked(members[adapterPosition].memberId, true)
//                }
//                else{
//                    onItemChecked(members[adapterPosition].memberId, false)
//                }
//            }
//        }
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

class MembersCheckboxViewHolder(val binding: MemberCheckboxCardBinding, val onItemChecked: (Int, Boolean) -> Unit,
    members: List<Member>)
    : RecyclerView.ViewHolder(binding.root){

    init {
        binding.paidUnpaidCheckbox.setOnCheckedChangeListener{ buttonView, isChecked ->
            if(isChecked){
                onItemChecked(members[adapterPosition].memberId, true)
            }
            else{
                onItemChecked(members[adapterPosition].memberId, false)
            }
        }
    }
    fun bind(member: Member){
        binding.memberTextView.text = member.name
    }

}