package com.example.splitwise.ui.fragment.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ExpenseMemberCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.roundOff

class ExpenseMembersAdapter : RecyclerView.Adapter<ExpenseMembersViewHolder>() {
    private var members = listOf<Member>()
    private var total: Float = 0f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ExpenseMemberCardBinding.inflate(view, parent, false)

        return ExpenseMembersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseMembersViewHolder, position: Int) {
        val member = members[position]

        val share = if(members.isEmpty()) 0f
        else (total/members.size)

        holder.bind(member, share)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    fun updateMembers(members: List<Member>) {
        this.members = members
        notifyDataSetChanged()
    }

    fun updateTotal(total: Float){
        this.total = total
        notifyDataSetChanged()
    }
}

class ExpenseMembersViewHolder(val binding: ExpenseMemberCardBinding) : RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources

    fun bind(member: Member, share: Float) {
        binding.memberNameTextView.text = member.name
        binding.memberShareTextView.text = "₹" + share.roundOff()

        if(member.memberProfile != null){
            ///binding.memberImageView.setImageURI(member.memberProfile)
            Handler(Looper.getMainLooper()).postDelayed({
            binding.memberImageView.setImageBitmap(decodeSampledBitmapFromUri(
                binding.root.context, member.memberProfile, 40.dpToPx(resources.displayMetrics), 40.dpToPx(resources.displayMetrics)
            ))
            }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        }
    }

}