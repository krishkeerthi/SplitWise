package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues.TAG
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ChooseMemberCardBinding
import com.example.splitwise.model.MemberAndStreak
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap
import java.util.*

class ChooseMembersAdapter(val onItemChecked: (Member, Boolean) -> Unit) :
    RecyclerView.Adapter<ChooseMembersViewHolder>() {
    private var membersAndStreaks = listOf<MemberAndStreak>()
    private var checkedMembersId = mutableSetOf<Int>()
    private var query = ""
    // this is added to maintain checked items in the rv while scrolling
    //private var checkedIds = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseMembersViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChooseMemberCardBinding.inflate(view, parent, false)

        return ChooseMembersViewHolder(binding).apply {
            itemView.setOnClickListener {
                val isChecked = binding.selectedCheckBox.isChecked
                binding.selectedCheckBox.isChecked = !isChecked
            }

            binding.selectedCheckBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                if (isChecked) {
                    onItemChecked(membersAndStreaks[adapterPosition].member, true)
                    checkedMembersId.add(membersAndStreaks[adapterPosition].member.memberId)
                } else {
                    onItemChecked(membersAndStreaks[adapterPosition].member, false)
                    checkedMembersId.remove(membersAndStreaks[adapterPosition].member.memberId)
                }

                Log.d(TAG, "onCreateViewHolder: checkedmembers ${checkedMembersId}")
            }
        }
    }

    override fun onBindViewHolder(holder: ChooseMembersViewHolder, position: Int) {
        val member = membersAndStreaks[position]

        holder.bind(member, checkedMembersId.toList(), query) //, checkedIds.toList()
    }

    override fun getItemCount(): Int {
        return membersAndStreaks.size
    }

    override fun onViewRecycled(holder: ChooseMembersViewHolder) {
        super.onViewRecycled(holder)

        holder.resetView()
    }

    fun updateMembersAndStreaks(
        membersAndStreaks: List<MemberAndStreak>,
        checkedMembersId: List<Int>,
        query: String
    ) {
        this.membersAndStreaks = membersAndStreaks
        this.checkedMembersId = checkedMembersId.toMutableSet()
        this.query = query
        Log.d(TAG, "updateMembersAndStreaks checked: ${checkedMembersId.size}")
        notifyDataSetChanged()
    }
}

class ChooseMembersViewHolder(val binding: ChooseMemberCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources
    fun bind(memberAndStreak: MemberAndStreak, checkedMembersId: List<Int>, query: String) { //, checkedIds: List<Int>

        binding.memberNameTextView.text = if(query != "") getSpannable(memberAndStreak.member.name, query)
        else memberAndStreak.member.name

        binding.memberStreakTextView.text = "🔥 " + memberAndStreak.streak.toString()


        binding.selectedCheckBox.isChecked = memberAndStreak.member.memberId in checkedMembersId

        // to retain checks
        //binding.selectedCheckBox.isChecked = memberAndStreak.member.memberId in checkedIds

        if (memberAndStreak.member.memberProfile != null) {
            ///binding.memberImageView.setImageURI(memberAndStreak.member.memberProfile)
            binding.memberImageView.setImageBitmap(null)
            //Handler(Looper.getMainLooper()).postDelayed({
                binding.memberImageView.setImageBitmap(
                    getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        memberAndStreak.member.memberProfile,
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
        else{
            binding.memberImageView.visibility = View.INVISIBLE
            binding.memberImageHolder.visibility = View.VISIBLE
            binding.memberImageHolderImage.visibility = View.VISIBLE
        }

        Log.d(TAG, "bind: rv checking: on bind")
    }

    fun resetView() {
        Log.d(TAG, "resetView rv checking: onviewrecycled")
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