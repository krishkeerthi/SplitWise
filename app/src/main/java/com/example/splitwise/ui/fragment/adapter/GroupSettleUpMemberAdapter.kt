package com.example.splitwise.ui.fragment.adapter

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap
import java.util.*

class GroupSettleUpMemberAdapter(private val members: List<Member>, private val query: String, private val onClicked: (Int) -> Unit)
    : RecyclerView.Adapter<GroupSettleUpMemberViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupSettleUpMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberCardBinding.inflate(view, parent, false)
        return GroupSettleUpMemberViewHolder(binding).apply {
            itemView.setOnClickListener {
                onClicked(members[absoluteAdapterPosition].memberId)
            }
        }
    }

    override fun onBindViewHolder(holder: GroupSettleUpMemberViewHolder, position: Int) {
        holder.bind(members[position], query)
    }

    override fun getItemCount(): Int {
        return members.size
    }
}

class GroupSettleUpMemberViewHolder(private val binding: MemberCardBinding): ViewHolder(binding.root){
    private val resources = binding.root.resources

    fun bind(member: Member, query: String) {

        binding.memberNameTextView.text = if(query != "") getSpannable(member.name, query) else member.name

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

    private fun getSpannable(data: String, query: String): Spannable {
        val startPos =
            data.lowercase(Locale.getDefault()).indexOf(query.lowercase(Locale.getDefault()))
        val endPos = startPos + query.length

        return if (startPos != -1) {
            val spannable = SpannableString(data)
            val colorStateList = ColorStateList(
                Array(query.length) { IntArray(query.length) },
                IntArray(query.length) {
                    ContextCompat.getColor(
                        binding.memberNameTextView.context,
                        R.color.blue
                    )
                }
            )

            val textAppearanceSpan = TextAppearanceSpan(
                null,
                Typeface.BOLD,
                -1,
                colorStateList,
                null
            )
            spannable.setSpan(
                textAppearanceSpan,
                startPos,
                endPos,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable
        } else
            data.toSpannable()
    }
}