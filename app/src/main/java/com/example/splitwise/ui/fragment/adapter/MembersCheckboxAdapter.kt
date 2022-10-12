package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberCheckboxCardBinding
import com.example.splitwise.databinding.MemberSelectBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.checkbox.MaterialCheckBox.OnCheckedStateChangedListener

class MembersCheckboxAdapter(private val onItemChecked: (Int, Boolean) -> Unit) :
    RecyclerView.Adapter<MembersCheckboxViewHolder>() {

    private var canStart: Boolean = true
    private var members = listOf<Member>()
    private var selectedMembers = listOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersCheckboxViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberSelectBinding.inflate(view, parent, false)


        return MembersCheckboxViewHolder(binding).apply {
            itemView.setOnClickListener {
                if(canStart){
                    canStart = false
                    val isChecked = binding.paidUnpaidCheckbox.isChecked
                    if (!isChecked) {
                        binding.paidUnpaidCheckbox.isChecked = true
                        onItemChecked(members[adapterPosition].memberId, true)
                    } else {
                        binding.paidUnpaidCheckbox.isChecked = false
                        onItemChecked(members[adapterPosition].memberId, false)
                    }
                    canStart = true
                }
            }
            binding.paidUnpaidCheckbox.addOnCheckedStateChangedListener { checkBox, state ->
                if(state == MaterialCheckBox.STATE_CHECKED){
                    onItemChecked(members[adapterPosition].memberId, true)
                }
                else{
                    onItemChecked(members[adapterPosition].memberId, false)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: MembersCheckboxViewHolder, position: Int) {
        val member = members[position]

        holder.bind(member, selectedMembers)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    fun updateMembers(members: List<Member>, selectedMembersIds: List<Int>) {
        this.members = members
        this.selectedMembers = selectedMembersIds
        notifyDataSetChanged()
    }
}

class MembersCheckboxViewHolder(val binding: MemberSelectBinding) :
    RecyclerView.ViewHolder(binding.root) {

    //    init {
//        binding.paidUnpaidCheckbox.setOnCheckedChangeListener{ buttonView, isChecked ->
//            if(isChecked){
//                onItemChecked(members[adapterPosition].memberId, true)
//                Log.d(TAG, " checkbox: $adapterPosition: ")
//            }
//            else{
//                onItemChecked(members[adapterPosition].memberId, false)
//            }
//        }
//    }

    private val resources = binding.root.resources
    fun bind(member: Member, selectedMembersIds: List<Int>) {
        binding.memberTextView.text = member.name

        if (member.memberProfile != null) {
            ///binding.memberImageView.setImageURI(member.memberProfile)

            binding.memberImageView.setImageBitmap(null)
            //Handler(Looper.getMainLooper()).postDelayed({
                binding.memberImageView.setImageBitmap(
                    getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        member.memberProfile,
                        48.dpToPx(resources.displayMetrics),
                        48.dpToPx(resources.displayMetrics)
                    )!!
                    )
                )
            //}, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        }

        binding.paidUnpaidCheckbox.isChecked = member.memberId in selectedMembersIds
    }

}