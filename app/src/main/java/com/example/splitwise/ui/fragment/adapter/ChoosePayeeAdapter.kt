package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.ChoosePayeeCardBinding
import com.example.splitwise.model.MemberAndAmount
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getCategoryDrawableResource
import com.example.splitwise.util.roundOff

class ChoosePayeeAdapter(val onItemChecked: (Member, Boolean) -> Unit) :
    RecyclerView.Adapter<ChoosePayeeViewHolder>() {
    //private var payees = listOf<Member>()
    private var payeesAndAmounts = listOf<MemberAndAmount>()
    private var selectedPayeesId = listOf<Int>()
    private var selectedAllPayees: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePayeeViewHolder{
        val view = LayoutInflater.from(parent.context)
        val binding = ChoosePayeeCardBinding.inflate(view, parent, false)

        return ChoosePayeeViewHolder(binding).apply {
            itemView.setOnClickListener {
                val isChecked = binding.selectedCheckBox.isChecked
                binding.selectedCheckBox.isChecked = !isChecked
            }

            binding.selectedCheckBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                // Log.d(TAG, "onCreateViewHolder: group checkbox checked $isChecked")
                if (isChecked) {
                    //binding.selectedCheckBox.isChecked = true
                    onItemChecked(payeesAndAmounts[adapterPosition].member, true)
                }
                else {
                    //binding.selectedCheckBox.isChecked = false
                    onItemChecked(payeesAndAmounts[adapterPosition].member, false)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ChoosePayeeViewHolder, position: Int) {
        val payee = payeesAndAmounts[position].member
        val amount = payeesAndAmounts[position].amount

        if(!selectedAllPayees)
            holder.bind(payee, amount, selectedPayeesId)
        else
            holder.bindAndCheck(payee)
    }

    override fun getItemCount(): Int {
        return payeesAndAmounts.size
    }

    fun selectAllPayees() {
        selectedAllPayees = true
        notifyDataSetChanged()
    }

    fun updatePayees(payeesAndAmounts: List<MemberAndAmount>, selectedPayeesId: List<Int>) {
        //this.payees= payees
        this.payeesAndAmounts = payeesAndAmounts
        this.selectedPayeesId = selectedPayeesId
        notifyDataSetChanged()
    }

    fun updatePayees(selectedPayeesId: List<Int>){
        this.selectedPayeesId = selectedPayeesId
        notifyDataSetChanged()
    }
}

class ChoosePayeeViewHolder(val binding: ChoosePayeeCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources
    fun bind(payee: Member, amount: Float, selectedPayees: List<Int>) {
        binding.payeeNameTextView.text = payee.name
        binding.amountTextView.text = "₹" + amount.roundOff()

        if(payee.memberProfile != null){
            ///binding.payeeImageView.setImageURI(payee.memberProfile)

            binding.payeeImageView.setImageBitmap(null)
            binding.payeeImageView.setImageBitmap(decodeSampledBitmapFromUri(
                binding.root.context, payee.memberProfile, 40.dpToPx(resources.displayMetrics), 40.dpToPx(resources.displayMetrics)
            ))

            binding.payeeImageView.visibility = View.VISIBLE
            binding.payeeImageHolder.visibility = View.INVISIBLE
            binding.payeeImageHolderImage.visibility = View.INVISIBLE
        }

        binding.selectedCheckBox.isChecked = payee.memberId in selectedPayees

    }

    fun bindAndCheck(payee: Member) {
        binding.payeeNameTextView.text = payee.name
        if(payee.memberProfile != null){
            ///binding.payeeImageView.setImageURI(payee.memberProfile)

            binding.payeeImageView.setImageBitmap(null)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.payeeImageView.setImageBitmap(decodeSampledBitmapFromUri(
                    binding.root.context, payee.memberProfile, 40.dpToPx(resources.displayMetrics), 40.dpToPx(resources.displayMetrics)))
            }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())


            binding.payeeImageView.visibility = View.VISIBLE
            binding.payeeImageHolder.visibility = View.INVISIBLE
            binding.payeeImageHolderImage.visibility = View.INVISIBLE
        }

        binding.selectedCheckBox.isChecked = true


    }
}