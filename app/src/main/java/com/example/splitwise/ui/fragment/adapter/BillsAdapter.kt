package com.example.splitwise.ui.fragment.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.BillProfileCardBinding

class BillsAdapter(
    val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<BillsViewHolder>() {
    private var uris = listOf<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = BillProfileCardBinding.inflate(view, parent, false)

        return BillsViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: BillsViewHolder, position: Int) {
        val uri = uris[position]

        holder.bind(uri)
    }

    override fun getItemCount(): Int {
        return uris.size
    }

    fun updateBills(uris: List<Uri>) {
        this.uris = uris
        notifyDataSetChanged()
    }
}

class BillsViewHolder(val binding: BillProfileCardBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uri: Uri) {
        binding.billImageView.setImageURI(uri)
    }

}