package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.MemberOweLendCardBinding
import com.example.splitwise.model.MemberPaymentStatsDetail
import com.example.splitwise.util.ripple
import com.example.splitwise.util.roundOff

class SplitWiseAdapter(
    val onTransactionClicked: (Int, Float, String) -> Unit
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
                    membersPaymentStatsDetail[adapterPosition].memberName
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
        this.membersPaymentStatsDetail = membersPaymentStatsDetail
        notifyDataSetChanged()
    }
}

class SplitWiseViewHolder(val binding: MemberOweLendCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(memberPaymentStatsDetail: MemberPaymentStatsDetail) {
        binding.memberTextView.text = memberPaymentStatsDetail.memberName
        binding.oweTextView.text = "₹" + memberPaymentStatsDetail.amountOwed.roundOff()
        binding.lendTextView.text = "₹" + memberPaymentStatsDetail.amountLend.roundOff()

        if(memberPaymentStatsDetail.memberProfile != null){
            Log.d(TAG, "bind: name ${memberPaymentStatsDetail.memberName} profile ${memberPaymentStatsDetail.memberProfile}")
            binding.memberImageView.setImageURI(memberPaymentStatsDetail.memberProfile)

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