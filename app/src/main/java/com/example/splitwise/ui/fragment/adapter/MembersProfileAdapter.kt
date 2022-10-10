package com.example.splitwise.ui.fragment.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberProfileCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap

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

    override fun onViewRecycled(holder: MembersProfileViewHolder) {
        super.onViewRecycled(holder)

        holder.resetViews()
    }

    fun updateMembers(members: List<Member>) {
        this.members = members
        notifyDataSetChanged()
    }
}

class MembersProfileViewHolder(val binding: MemberProfileCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources
    fun bind(member: Member) {
        binding.memberNameTextView.text = member.name

        if (member.memberProfile != null) {
            ///binding.memberImageView.setImageURI(member.memberProfile)

            // this avoids loading old views.
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
            //  }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        } else { // may be we can use on view recycled.
            binding.memberImageHolder.visibility = View.VISIBLE
            binding.memberImageHolderImage.visibility = View.VISIBLE
            binding.memberImageView.visibility = View.INVISIBLE
        }
    }

    fun resetViews() {
        binding.memberImageHolder.visibility = View.VISIBLE
        binding.memberImageHolderImage.visibility = View.VISIBLE
        binding.memberImageView.visibility = View.INVISIBLE
    }

}