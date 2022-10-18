package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.MemberOweLendCardBinding
import com.example.splitwise.model.MemberPaymentStatsDetail
import com.example.splitwise.util.*

class SplitWiseAdapter(
    val onTransactionClicked: (Int, Float, String, View) -> Unit
) : RecyclerView.Adapter<SplitWiseViewHolder>() {
    private var membersPaymentStatsDetail = listOf<MemberPaymentStatsDetail>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplitWiseViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberOweLendCardBinding.inflate(view, parent, false)

        return SplitWiseViewHolder(binding).apply {
            itemView.setOnClickListener {
                //itemView.ripple(itemView.context)
                onTransactionClicked(
                    membersPaymentStatsDetail[adapterPosition].memberId,
                    membersPaymentStatsDetail[adapterPosition].amountOwed,
                    membersPaymentStatsDetail[adapterPosition].memberName,
                    itemView
                )
            }
        }
    }

    override fun onBindViewHolder(holder: SplitWiseViewHolder, position: Int) {
        val memberPaymentStatsDetail = membersPaymentStatsDetail[position]

        holder.bind(memberPaymentStatsDetail)
    }

    override fun getItemCount(): Int {
        return membersPaymentStatsDetail.size
    }

    fun updateMembersPaymentStatsDetail(membersPaymentStatsDetail: List<MemberPaymentStatsDetail>) {
        Log.d(TAG, "updateMembersPaymentStatsDetail: ${membersPaymentStatsDetail}")

        val diffCallback = PaymentStatsDiffCallback(this.membersPaymentStatsDetail, membersPaymentStatsDetail)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        this.membersPaymentStatsDetail = membersPaymentStatsDetail
        //notifyDataSetChanged()

        diffResult.dispatchUpdatesTo(this)
    }
}

class SplitWiseViewHolder(val binding: MemberOweLendCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    val resources = binding.root.resources
    fun bind(memberPaymentStatsDetail: MemberPaymentStatsDetail) {
        // transition name
        ViewCompat.setTransitionName(
            binding.root,
            String.format(
                resources.getString(R.string.member_payment_stat_transition_name),
                memberPaymentStatsDetail.memberId
            )
        )

        binding.memberTextView.text = memberPaymentStatsDetail.memberName
        binding.oweTextView.text = "₹" + memberPaymentStatsDetail.amountOwed.roundOff()
        binding.lendTextView.text = "₹" + memberPaymentStatsDetail.amountLend.roundOff()

        if (memberPaymentStatsDetail.memberProfile != null) {
            Log.d(
                TAG,
                "bind: name ${memberPaymentStatsDetail.memberName} profile ${memberPaymentStatsDetail.memberProfile}"
            )
            ///binding.memberImageView.setImageURI(memberPaymentStatsDetail.memberProfile)

            binding.memberImageView.setImageBitmap(null)
            // delaying image loading so that rendering speed wont affect
            //Handler(Looper.getMainLooper()).postDelayed({
            binding.memberImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        memberPaymentStatsDetail.memberProfile,
                        40.dpToPx(resources.displayMetrics),
                        40.dpToPx(
                            resources.displayMetrics
                        )
                    )!!
                )
            )

            //}, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.memberImageView.visibility = View.VISIBLE
            binding.memberImageHolder.visibility = View.INVISIBLE
            binding.memberImageHolderImage.visibility = View.INVISIBLE
        } else {
            binding.memberImageView.visibility = View.INVISIBLE
            binding.memberImageHolder.visibility = View.VISIBLE
            binding.memberImageHolderImage.visibility = View.VISIBLE
        }
    }

}

class PaymentStatsDiffCallback(private val oldItems: List<MemberPaymentStatsDetail>, private val newItems: List<MemberPaymentStatsDetail>)
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