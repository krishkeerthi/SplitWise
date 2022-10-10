package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap

class RemovedMembersAdapter(private val members: List<Member>) :
    RecyclerView.Adapter<RemovedMembersViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RemovedMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberCardBinding.inflate(view, parent, false)
        return RemovedMembersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RemovedMembersViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int {
        return members.size
    }
}

class RemovedMembersViewHolder(private val binding: MemberCardBinding) : ViewHolder(binding.root) {
    private val resources = binding.root.resources

    fun bind(member: Member) {
        binding.memberNameTextView.text = member.name

        if (member.memberProfile != null) {
            ///binding.memberImageView.setImageURI(member.memberProfile)
            // Handler(Looper.getMainLooper()).postDelayed({
            binding.memberImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        member.memberProfile,
                        40.dpToPx(resources.displayMetrics),
                        40.dpToPx(resources.displayMetrics)
                    )!!
                )
            )
            //}, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        }
    }
}