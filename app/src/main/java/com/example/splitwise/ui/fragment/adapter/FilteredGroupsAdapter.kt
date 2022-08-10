package com.example.splitwise.ui.fragment.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupCard1Binding
import com.example.splitwise.util.formatDate
import com.example.splitwise.util.roundOff
import java.util.*

class FilteredGroupsAdapter(
    val onExpenseClicked: (Int) -> Unit
) : RecyclerView.Adapter<FilteredGroupsViewHolder>() {
    private var groups = listOf<Group>()
    private var query = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilteredGroupsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupCard1Binding.inflate(view, parent, false)

        return FilteredGroupsViewHolder(binding).apply {
            itemView.setOnClickListener {
                onExpenseClicked(groups[adapterPosition].groupId)
            }
        }
    }

    override fun onBindViewHolder(holder: FilteredGroupsViewHolder, position: Int) {
        val group = groups[position]

        holder.bind(group, query)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    fun updateGroups(groups: List<Group>, query: String) {
        this.groups = groups
        this.query = query
        notifyDataSetChanged()
    }
}

class FilteredGroupsViewHolder(val binding: GroupCard1Binding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(group: Group, query: String) {

        val groupName = if(query != "") getSpannable(group.groupName, query)
            else group.groupName

        binding.groupNameTextView.text = groupName
        binding.groupExpenseTextView.text = "₹" + group.totalExpense.roundOff()
        binding.groupCreationDateTextView.text = formatDate(group.creationDate)
    }

    private fun getSpannable(data: String, query: String): Spannable {
        val startPos = data.lowercase(Locale.getDefault()).indexOf(query.lowercase(Locale.getDefault()))
        val endPos = startPos + query.length

        return if (startPos != -1) {
            val spannable = SpannableString(data)
            val colorStateList = ColorStateList(
                Array(query.length){ IntArray(query.length) },
                IntArray(query.length){ContextCompat.getColor(binding.groupNameTextView.context, R.color.blue)}
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
        }
        else
            data.toSpannable()
    }

}