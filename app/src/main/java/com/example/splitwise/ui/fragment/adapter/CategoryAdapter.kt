package com.example.splitwise.ui.fragment.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.util.*

class CategoryAdapter(
    private var categories: List<Category>,
    val onItemClicked: (Category) -> Unit
) : RecyclerView.Adapter<CategoryViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = com.example.splitwise.databinding.IconBottomSheetItemBinding.inflate(
            view,
            parent,
            false
        )

        return CategoryViewHolder(binding).apply {
            itemView.setOnClickListener {
                onItemClicked(categories[absoluteAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]

        holder.bind(category)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

}

class CategoryViewHolder(val binding: com.example.splitwise.databinding.IconBottomSheetItemBinding) :
    RecyclerView.ViewHolder(binding.root) {


    fun bind(category: Category) {
        binding.itemTextView.text = category.name.lowercase().titleCase()
        binding.itemImageView.setImageResource(getCategoryDrawableResource(category.ordinal))
    }

}
