package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
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
    }

}