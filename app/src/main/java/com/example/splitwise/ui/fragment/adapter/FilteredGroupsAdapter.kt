package com.example.splitwise.ui.fragment.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.GroupCard1Binding
import com.example.splitwise.databinding.GroupCard2Binding
import com.example.splitwise.util.*
import java.util.*

class FilteredGroupsAdapter(
    val onGroupClicked: (Int, View) -> Unit,
    val onImageClicked: (Int, String?, String, View) -> Unit
) : RecyclerView.Adapter<FilteredGroupsViewHolder>() {
    private var groups = listOf<Group>()
    private var query = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilteredGroupsViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = GroupCard2Binding.inflate(view, parent, false)

        return FilteredGroupsViewHolder(binding).apply {
//            itemView.setOnClickListener {
//                onGroupClicked(groups[adapterPosition].groupId)
//            }

            binding.groupIconCard.setOnClickListener {
                itemView.ripple(itemView.context)
                onImageClicked(
                    groups[absoluteAdapterPosition].groupId,
                    groups[absoluteAdapterPosition].groupIcon?.toString(),
                    groups[absoluteAdapterPosition].groupName,

                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
                    else binding.groupImageHolderImage
                )
            }

            itemView.setOnClickListener {
                it.ripple(it.context)
                onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
            }

//            binding.groupImageView.setOnClickListener {
//                it.ripple(it.context)
//                onImageClicked(
//                    groups[absoluteAdapterPosition].groupId,
//                    groups[absoluteAdapterPosition].groupIcon?.toString(),
//                    groups[absoluteAdapterPosition].groupName,
//                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
//                    else binding.groupImageHolderImage
//                )
//            }
//            binding.groupImageHolder.setOnClickListener {
//                it.ripple(it.context)
//                onImageClicked(
//                    groups[absoluteAdapterPosition].groupId,
//                    groups[absoluteAdapterPosition].groupIcon?.toString(),
//                    groups[absoluteAdapterPosition].groupName,
//                    if (groups[absoluteAdapterPosition].groupIcon != null) binding.groupImageView
//                    else binding.groupImageHolderImage
//                )
//            }
//            binding.textLayout.setOnClickListener {
//                it.ripple(it.context)
//                onGroupClicked(groups[absoluteAdapterPosition].groupId, itemView)
//            }
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

class FilteredGroupsViewHolder(val binding: GroupCard2Binding) :
    RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources
    fun bind(group: Group, query: String) {

        ViewCompat.setTransitionName(
            binding.root,
            String.format(
                resources.getString(R.string.search_group_card_transition_name),
                group.groupId
            )
        )
        ViewCompat.setTransitionName(
            binding.groupImageView,
            String.format(
                resources.getString(R.string.search_group_image_transition_name),
                group.groupId
            )
        )
        ViewCompat.setTransitionName(
            binding.groupImageHolderImage,
            String.format(
                resources.getString(R.string.search_group_empty_image_transition_name),
                group.groupId
            )
        )

        val groupName = if (query != "") getSpannable(group.groupName, query)
        else group.groupName

        binding.groupNameTextView.text = groupName
        binding.groupExpenseTextView.text = "₹" + group.totalExpense.roundOff()
        binding.groupCreationDateTextView.text = formatDate(group.creationDate)

        if (group.groupIcon != null) {
            ///binding.groupImageView.setImageURI(group.groupIcon)
            binding.groupImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        group.groupIcon,
                        48.dpToPx(resources.displayMetrics),
                        48.dpToPx(resources.displayMetrics)
                    )!!
                )
            )
            binding.groupImageHolder.visibility = View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE
        } else {
            binding.groupImageView.setImageResource(R.drawable.ic_baseline_people_24)
            binding.groupImageHolder.visibility = View.VISIBLE
            binding.groupImageHolderImage.visibility = View.VISIBLE
            binding.groupImageView.visibility = View.INVISIBLE
        }
    }

    private fun getSpannable(data: String, query: String): Spannable {
        val startPos =
            data.lowercase(Locale.getDefault()).indexOf(query.lowercase(Locale.getDefault()))
        val endPos = startPos + query.length

        return if (startPos != -1) {
            val spannable = SpannableString(data)
            val colorStateList = ColorStateList(
                Array(query.length) { IntArray(query.length) },
                IntArray(query.length) {
                    ContextCompat.getColor(
                        binding.groupNameTextView.context,
                        R.color.blue
                    )
                }
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
        } else
            data.toSpannable()
    }

}