package com.example.splitwise.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.util.Category
import com.example.splitwise.util.getCategoryDrawableResource
import com.example.splitwise.util.titleCase

class PayerAdapter(
    private val payers: List<Member>,
    val onItemClicked: (Member) -> Unit
) : RecyclerView.Adapter<PayerViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PayerViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = com.example.splitwise.databinding.IconBottomSheetItemBinding.inflate(
            view,
            parent,
            false
        )

        return PayerViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(payers[absoluteAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: PayerViewHolder, position: Int) {
        val payer = payers[position]

        holder.bind(payer)
    }

    override fun getItemCount(): Int {
        return payers.size
    }

}

class PayerViewHolder(val binding: com.example.splitwise.databinding.IconBottomSheetItemBinding) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(payer: Member) {
        binding.itemTextView.text = payer.name
        binding.itemImageView.setImageResource(R.drawable.ic_baseline_person_24)

    }

}
