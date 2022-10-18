package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.GroupMemberCardBinding
import com.example.splitwise.databinding.NewGroupMemberCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap
import com.example.splitwise.util.ripple

class GroupMembersAdapter(
    val groupId: Int,
    val memberClicked: (Int, View) -> Unit,
    val onDeleteClicked: (Member, Int, View) -> Unit,
    val editNotAllowed: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var members = listOf<Member>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)

        val viewHolder: RecyclerView.ViewHolder = if (groupId == -1) {
            val binding = NewGroupMemberCardBinding.inflate(view, parent, false)
            NewGroupMembersViewHolder(binding).apply {
                binding.deleteMemberImageView.setOnClickListener {
                    //it.ripple(it.context)
                    onDeleteClicked(members[absoluteAdapterPosition], absoluteAdapterPosition, binding.deleteMemberImageView)
                }
            }
        } else {
            val binding = GroupMemberCardBinding.inflate(view, parent, false)

            GroupMembersViewHolder(binding).apply {
                itemView.setOnClickListener {
                    //it.ripple(it.context)
//                    if(members[absoluteAdapterPosition].memberId in 1000..10000) {
//                        editNotAllowed()
//                    }
//                    else
                        memberClicked(members[absoluteAdapterPosition].memberId, itemView)
                }
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val member = members[position]

        if (groupId != -1) {
            val viewHolder = holder as GroupMembersViewHolder
            viewHolder.bind(member)
        } else {
            val viewHolder = holder as NewGroupMembersViewHolder
            viewHolder.bind(member)
        }
    }

    override fun getItemCount(): Int {
        return members.size
    }

    fun updateMembers(members: List<Member>) {
        val diffCallback = DiffCallback(this.members, members)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.members = members
        //notifyDataSetChanged()

        diffResult.dispatchUpdatesTo(this)
    }

}

class GroupMembersViewHolder(val binding: GroupMemberCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val resources = binding.root.resources
    fun bind(member: Member) {

        ViewCompat.setTransitionName(
            binding.root,
            String.format(
                resources.getString(R.string.create_edit_group_members_transition_name),
                member.memberId
            )
        )
        binding.memberNameTextView.text = member.name
        binding.memberPhoneTextView.text = member.phone.toString()

        if (member.memberProfile != null) {
            ///binding.memberImageView.setImageURI(member.memberProfile)

            binding.memberImageView.setImageBitmap(null)
            //    Handler(Looper.getMainLooper()).postDelayed({
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
            //    }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        }
        else{
            binding.memberImageView.visibility = View.INVISIBLE
            binding.memberImageHolder.visibility = View.VISIBLE
            binding.memberImageHolderImage.visibility = View.VISIBLE
        }
    }

}

class NewGroupMembersViewHolder(val binding: NewGroupMemberCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val resources = binding.root.resources
    fun bind(member: Member) {

        //ViewCompat.setTransitionName(binding.root, String.format(resources.getString(R.string.create_edit_group_members_transition_name), member.memberId))
        binding.memberNameTextView.text = member.name
        binding.memberPhoneTextView.text = member.phone.toString()

        if (member.memberProfile != null) {
            ///binding.memberImageView.setImageURI(member.memberProfile)

            binding.memberImageView.setImageBitmap(null)
            //    Handler(Looper.getMainLooper()).postDelayed({
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
            //    }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())


            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        }
    }

}

class DiffCallback(private val oldItems: List<Member>, private val newItems: List<Member>)
    : DiffUtil.Callback(){
    override fun getOldListSize() = oldItems.size

    override fun getNewListSize() = newItems.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].memberId == newItems[newItemPosition].memberId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }
}