package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.databinding.MemberOweLendCardBinding
import com.example.splitwise.model.MemberPaymentStatsDetail

class SplitWiseAdapter : RecyclerView.Adapter<SplitWiseViewHolder>() {
    private var membersPaymentStatsDetail = listOf<MemberPaymentStatsDetail>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplitWiseViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = MemberOweLendCardBinding.inflate(view, parent, false)

        return SplitWiseViewHolder(binding)
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
        binding.oweTextView.text = memberPaymentStatsDetail.amountOwed.toString()
        binding.lendTextView.text = memberPaymentStatsDetail.amountLend.toString()
    }

}