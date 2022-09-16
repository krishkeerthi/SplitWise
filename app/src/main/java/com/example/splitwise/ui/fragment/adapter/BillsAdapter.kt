package com.example.splitwise.ui.fragment.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.BillProfileCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx

class BillsAdapter(
    val onItemClicked: (Int, View) -> Unit
) : RecyclerView.Adapter<BillsViewHolder>() {
    private var uris = listOf<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = BillProfileCardBinding.inflate(view, parent, false)

        return BillsViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(absoluteAdapterPosition, itemView)
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

    private val resources = binding.root.resources

    fun bind(uri: Uri) {
        ViewCompat.setTransitionName(binding.root, String.format(resources.getString(R.string.bill_image_transition_name), uri.toString()))
        ///binding.billImageView.setImageURI(uri)
        binding.billImageView.setImageBitmap(
            decodeSampledBitmapFromUri(
            binding.root.context, uri, 60.dpToPx(resources.displayMetrics), 100.dpToPx(resources.displayMetrics)
        )
        )
    }

}